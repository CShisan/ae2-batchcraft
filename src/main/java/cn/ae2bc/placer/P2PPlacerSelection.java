package cn.ae2bc.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record P2PPlacerSelection(
        ResourceLocation dimension,
        BlockPos first,
        @Nullable BlockPos second) {
    public static final Codec<P2PPlacerSelection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(P2PPlacerSelection::dimension),
            BlockPos.CODEC.fieldOf("first").forGetter(P2PPlacerSelection::first),
            BlockPos.CODEC.optionalFieldOf("second")
                    .forGetter(selection -> Optional.ofNullable(selection.second))
    ).apply(instance, (dimension, first, second) ->
            new P2PPlacerSelection(dimension, first, second.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, P2PPlacerSelection> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeResourceLocation(value.dimension);
                buffer.writeBlockPos(value.first);
                buffer.writeBoolean(value.second != null);
                if (value.second != null) {
                    buffer.writeBlockPos(value.second);
                }
            },
            buffer -> new P2PPlacerSelection(
                    buffer.readResourceLocation(),
                    buffer.readBlockPos(),
                    buffer.readBoolean() ? buffer.readBlockPos() : null));

    public P2PPlacerSelection {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(first, "first");
        first = first.immutable();
        second = second == null ? null : second.immutable();
    }

    public static P2PPlacerSelection start(ResourceLocation dimension, BlockPos first) {
        return new P2PPlacerSelection(dimension, first, null);
    }

    public P2PPlacerSelection complete(BlockPos oppositeCorner) {
        return new P2PPlacerSelection(dimension, first, oppositeCorner);
    }

    public Validation validate() {
        if (second == null) {
            return Validation.INCOMPLETE;
        }

        return switch (geometry().validate()) {
            case VALID -> Validation.VALID;
            case VOLUME_NOT_ALLOWED -> Validation.VOLUME_NOT_ALLOWED;
            case TOO_LARGE -> Validation.TOO_LARGE;
        };
    }

    public int sizeX() {
        return second == null ? 0 : Math.abs(second.getX() - first.getX()) + 1;
    }

    public int sizeY() {
        return second == null ? 0 : Math.abs(second.getY() - first.getY()) + 1;
    }

    public int sizeZ() {
        return second == null ? 0 : Math.abs(second.getZ() - first.getZ()) + 1;
    }

    public List<BlockPos> positions(P2PPlacerSettings settings) {
        if (second == null) {
            return List.of();
        }
        P2PSelectionGeometry geometry = geometry();
        return geometry.positions(settings.offsetX(), settings.offsetY(), settings.offsetZ()).stream()
                .map(position -> new BlockPos(position.x(), position.y(), position.z()))
                .toList();
    }

    private P2PSelectionGeometry geometry() {
        if (second == null) {
            throw new IllegalStateException("Selection is incomplete");
        }
        return new P2PSelectionGeometry(
                first.getX(), first.getY(), first.getZ(),
                second.getX(), second.getY(), second.getZ());
    }

    public enum Validation {
        VALID,
        INCOMPLETE,
        VOLUME_NOT_ALLOWED,
        TOO_LARGE
    }
}
