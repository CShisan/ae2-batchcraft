package cn.ae2bc.pattern;

import appeng.crafting.pattern.AEProcessingPattern;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** One per-slot processing-pattern configuration containing both input face and output form. */
public final class MaterialInputConfigData {
    private static final int DIRECTION_WORDS = InputDirectionCodec.WORD_COUNT;
    private static final int FORM_BITS = 2;
    private static final int FORM_SLOTS_PER_WORD = Long.SIZE / FORM_BITS;
    private static final int FORM_WORDS =
            (AEProcessingPattern.MAX_INPUT_SLOTS + FORM_SLOTS_PER_WORD - 1) / FORM_SLOTS_PER_WORD;
    private static final int WORD_COUNT = DIRECTION_WORDS + FORM_WORDS;
    private static final long FORM_MASK = 0b11L;

    public static final MaterialInputConfigData EMPTY = new MaterialInputConfigData(
            InputDirectionData.EMPTY, new long[FORM_WORDS]);
    public static final Codec<MaterialInputConfigData> CODEC = Codec.LONG.listOf(0, WORD_COUNT)
            .xmap(MaterialInputConfigData::fromWords, MaterialInputConfigData::words);
    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialInputConfigData> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                for (long word : value.toPacked()) {
                    buffer.writeVarLong(word);
                }
            },
            buffer -> {
                long[] words = new long[WORD_COUNT];
                for (int i = 0; i < words.length; i++) {
                    words[i] = buffer.readVarLong();
                }
                return fromPacked(words);
            });

    private final InputDirectionData directions;
    private final long[] forms;
    private final boolean empty;

    private MaterialInputConfigData(InputDirectionData directions, long[] forms) {
        this.directions = directions == null ? InputDirectionData.EMPTY : directions;
        this.forms = Arrays.copyOf(forms, FORM_WORDS);
        this.empty = this.directions.isEmpty() && !containsExplicitForm(this.forms);
    }

    public static MaterialInputConfigData fromLegacy(InputDirectionData directions) {
        if (directions == null || directions.isEmpty()) {
            return EMPTY;
        }
        return new MaterialInputConfigData(directions, new long[FORM_WORDS]);
    }

    public static MaterialInputConfigData fromPacked(long[] packed) {
        if (packed == null || packed.length == 0) {
            return EMPTY;
        }
        long[] directionWords = new long[DIRECTION_WORDS];
        long[] formWords = new long[FORM_WORDS];
        System.arraycopy(packed, 0, directionWords, 0, Math.min(DIRECTION_WORDS, packed.length));
        if (packed.length > DIRECTION_WORDS) {
            System.arraycopy(packed, DIRECTION_WORDS, formWords, 0,
                    Math.min(FORM_WORDS, packed.length - DIRECTION_WORDS));
        }
        var result = new MaterialInputConfigData(InputDirectionData.fromPacked(directionWords), formWords);
        return result.empty ? EMPTY : result;
    }

    private static MaterialInputConfigData fromWords(List<Long> values) {
        long[] words = new long[WORD_COUNT];
        for (int i = 0; i < Math.min(words.length, values.size()); i++) {
            Long value = values.get(i);
            words[i] = value == null ? 0 : value;
        }
        return fromPacked(words);
    }

    private List<Long> words() {
        long[] packed = toPacked();
        List<Long> result = new ArrayList<>(packed.length);
        for (long word : packed) {
            result.add(word);
        }
        return result;
    }

    public long[] toPacked() {
        long[] packed = new long[WORD_COUNT];
        long[] directionWords = directions.toPacked();
        System.arraycopy(directionWords, 0, packed, 0, Math.min(directionWords.length, DIRECTION_WORDS));
        System.arraycopy(forms, 0, packed, DIRECTION_WORDS, forms.length);
        return packed;
    }

    public InputDirectionData directions() {
        return directions;
    }

    public @Nullable Direction getDirection(int slot) {
        return directions.getDirection(slot);
    }

    public MaterialOutputForm getOutputForm(int slot) {
        if (!InputDirectionData.isValidSlot(slot)) {
            return MaterialOutputForm.NORMAL;
        }
        int word = slot / FORM_SLOTS_PER_WORD;
        int shift = slot % FORM_SLOTS_PER_WORD * FORM_BITS;
        return MaterialOutputForm.fromId((int) ((forms[word] >>> shift) & FORM_MASK));
    }

    public MaterialInputConfigData withDirection(int slot, @Nullable Direction direction) {
        if (!InputDirectionData.isValidSlot(slot)) {
            return this;
        }
        return compact(new MaterialInputConfigData(directions.withDirection(slot, direction), forms));
    }

    public MaterialInputConfigData withOutputForm(int slot, MaterialOutputForm form) {
        if (!InputDirectionData.isValidSlot(slot)) {
            return this;
        }
        MaterialOutputForm safeForm = form == null ? MaterialOutputForm.NORMAL : form;
        if (getOutputForm(slot) == safeForm) {
            return this;
        }
        long[] copy = forms.clone();
        int word = slot / FORM_SLOTS_PER_WORD;
        int shift = slot % FORM_SLOTS_PER_WORD * FORM_BITS;
        copy[word] = copy[word] & ~(FORM_MASK << shift) | (long) safeForm.getId() << shift;
        return compact(new MaterialInputConfigData(directions, copy));
    }

    public MaterialInputConfigData clearSlot(int slot) {
        return withDirection(slot, null).withOutputForm(slot, MaterialOutputForm.NORMAL);
    }

    public boolean isEmpty() {
        return empty;
    }

    private static MaterialInputConfigData compact(MaterialInputConfigData value) {
        return value.empty ? EMPTY : value;
    }

    private static boolean containsExplicitForm(long[] forms) {
        for (long word : forms) {
            if (word != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MaterialInputConfigData other
                && directions.equals(other.directions) && Arrays.equals(forms, other.forms);
    }

    @Override
    public int hashCode() {
        return 31 * directions.hashCode() + Arrays.hashCode(forms);
    }
}
