package cn.ae2bc.mixin;

import appeng.api.stacks.AEKey;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import cn.ae2bc.pattern.InputDirectionData;
import cn.ae2bc.registry.ModContent;
import cn.ae2bc.extension.PatternEncodingLogicExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Objects;

@Mixin(PatternEncodingLogic.class)
public abstract class PatternEncodingLogicMixin implements PatternEncodingLogicExtension {
    @Unique
    private static final String AE2BC_INPUT_DIRECTIONS_NBT = "InputDirections";

    @Shadow
    @Final
    private ConfigInventory encodedInputInv;

    @Unique
    private InputDirectionData ae2bc$inputDirections = InputDirectionData.EMPTY;
    @Unique
    private final AEKey[] ae2bc$inputKeys = new AEKey[AEProcessingPattern.MAX_INPUT_SLOTS];

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ae2bc$initializeInputSnapshot(CallbackInfo ci) {
        ae2bc$snapshotInputKeys();
    }

    @Inject(method = "onEncodedInputChanged", at = @At("TAIL"))
    private void ae2bc$clearDirectionsForReplacedInputs(CallbackInfo ci) {
        InputDirectionData updated = ae2bc$inputDirections;
        for (int slot = 0; slot < ae2bc$inputKeys.length; slot++) {
            AEKey current = encodedInputInv.getKey(slot);
            if (!Objects.equals(ae2bc$inputKeys[slot], current)) {
                updated = updated.withDirection(slot, null);
                ae2bc$inputKeys[slot] = current;
            }
        }
        ae2bc$inputDirections = updated;
    }

    @Inject(method = "loadEncodedPattern", at = @At("HEAD"))
    private void ae2bc$resetBeforeLoadingPattern(ItemStack pattern, CallbackInfo ci) {
        if (!pattern.isEmpty()) {
            ae2bc$inputDirections = InputDirectionData.EMPTY;
        }
    }

    @Inject(method = "loadProcessingPattern", at = @At("TAIL"))
    private void ae2bc$loadDirectionsFromPattern(AEProcessingPattern pattern, CallbackInfo ci) {
        InputDirectionData directions = pattern.getDefinition().get(ModContent.INPUT_DIRECTIONS.get());
        ae2bc$inputDirections = directions == null ? InputDirectionData.EMPTY : directions;
        ae2bc$snapshotInputKeys();
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void ae2bc$readDirections(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        ae2bc$inputDirections = InputDirectionData.fromPacked(data.getLongArray(AE2BC_INPUT_DIRECTIONS_NBT));
        ae2bc$snapshotInputKeys();
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void ae2bc$writeDirections(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        if (ae2bc$inputDirections.isEmpty()) {
            data.remove(AE2BC_INPUT_DIRECTIONS_NBT);
        } else {
            data.putLongArray(AE2BC_INPUT_DIRECTIONS_NBT, ae2bc$inputDirections.toPacked());
        }
    }

    @Override
    public InputDirectionData ae2bc$getInputDirections() {
        return ae2bc$inputDirections;
    }

    @Override
    public void ae2bc$setInputDirections(InputDirectionData directions) {
        ae2bc$inputDirections = Objects.requireNonNullElse(directions, InputDirectionData.EMPTY);
        ((PatternEncodingLogic) (Object) this).saveChanges();
    }

    @Unique
    private void ae2bc$snapshotInputKeys() {
        Arrays.setAll(ae2bc$inputKeys, encodedInputInv::getKey);
    }
}
