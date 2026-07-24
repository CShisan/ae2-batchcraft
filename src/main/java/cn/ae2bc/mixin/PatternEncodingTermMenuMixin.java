package cn.ae2bc.mixin;

import appeng.menu.guisync.GuiSync;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import cn.ae2bc.pattern.InputDirectionData;
import cn.ae2bc.registry.ModContent;
import cn.ae2bc.extension.PatternEncodingLogicExtension;
import cn.ae2bc.extension.PatternEncodingTermMenuExtension;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternEncodingTermMenu.class)
public abstract class PatternEncodingTermMenuMixin implements PatternEncodingTermMenuExtension {
    @Unique
    private static final String AE2BC_SET_INPUT_DIRECTION = "ae2bcSetInputDirection";

    @Shadow
    @Final
    private PatternEncodingLogic encodingLogic;

    @Unique
    @GuiSync(80)
    private long ae2bc$inputDirections0;
    @Unique
    @GuiSync(81)
    private long ae2bc$inputDirections1;
    @Unique
    @GuiSync(82)
    private long ae2bc$inputDirections2;
    @Unique
    @GuiSync(83)
    private long ae2bc$inputDirections3;
    @Unique
    private InputDirectionData ae2bc$cachedInputDirections = InputDirectionData.EMPTY;
    @Unique
    private long ae2bc$cachedInputDirections0;
    @Unique
    private long ae2bc$cachedInputDirections1;
    @Unique
    private long ae2bc$cachedInputDirections2;
    @Unique
    private long ae2bc$cachedInputDirections3;

    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("RETURN"))
    private void ae2bc$registerDirectionAction(CallbackInfo ci) {
        ((AEBaseMenuInvoker) this).ae2bc$registerClientAction(
                AE2BC_SET_INPUT_DIRECTION, int[].class, this::ae2bc$handleSetInputDirection);
    }

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void ae2bc$syncDirections(CallbackInfo ci) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        if (!menu.isClientSide()) {
            ae2bc$setSyncedDirections(((PatternEncodingLogicExtension) encodingLogic).ae2bc$getInputDirections());
        }
    }

    @Inject(method = "encodeProcessingPattern", at = @At("RETURN"))
    private void ae2bc$writeDirectionsToPattern(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack pattern = cir.getReturnValue();
        if (pattern == null || pattern.isEmpty()) {
            return;
        }
        InputDirectionData directions = ((PatternEncodingLogicExtension) encodingLogic).ae2bc$getInputDirections();
        if (directions.isEmpty()) {
            pattern.remove(ModContent.INPUT_DIRECTIONS.get());
        } else {
            pattern.set(ModContent.INPUT_DIRECTIONS.get(), directions);
        }
    }

    @Override
    public InputDirectionData ae2bc$getInputDirections() {
        if (ae2bc$cachedInputDirections0 != ae2bc$inputDirections0
                || ae2bc$cachedInputDirections1 != ae2bc$inputDirections1
                || ae2bc$cachedInputDirections2 != ae2bc$inputDirections2
                || ae2bc$cachedInputDirections3 != ae2bc$inputDirections3) {
            ae2bc$cachedInputDirections = InputDirectionData.fromPacked(new long[]{
                    ae2bc$inputDirections0, ae2bc$inputDirections1,
                    ae2bc$inputDirections2, ae2bc$inputDirections3
            });
            ae2bc$cacheSyncedWords();
        }
        return ae2bc$cachedInputDirections;
    }

    @Override
    public void ae2bc$setInputDirection(int slot, Direction direction) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        if (!InputDirectionData.isValidSlot(slot) || menu.getMode() != EncodingMode.PROCESSING) {
            return;
        }
        InputDirectionData updated = ae2bc$getInputDirections().withDirection(slot, direction);
        ae2bc$setSyncedDirections(updated);
        ((AEBaseMenuInvoker) this).ae2bc$sendClientAction(
                AE2BC_SET_INPUT_DIRECTION, new int[]{slot, direction == null ? 0 : direction.ordinal() + 1});
    }

    @Unique
    private void ae2bc$handleSetInputDirection(int[] action) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        if (menu.isClientSide() || action == null || action.length != 2
                || menu.getMode() != EncodingMode.PROCESSING
                || !InputDirectionData.isValidSlot(action[0])
                || !menu.getProcessingInputSlots()[action[0]].hasItem()) {
            return;
        }
        InputDirectionData current = ((PatternEncodingLogicExtension) encodingLogic).ae2bc$getInputDirections();
        ((PatternEncodingLogicExtension) encodingLogic).ae2bc$setInputDirections(
                current.withCode(action[0], action[1]));
    }

    @Unique
    private void ae2bc$setSyncedDirections(InputDirectionData directions) {
        long[] words = directions.toPacked();
        ae2bc$inputDirections0 = words[0];
        ae2bc$inputDirections1 = words[1];
        ae2bc$inputDirections2 = words[2];
        ae2bc$inputDirections3 = words[3];
        ae2bc$cachedInputDirections = directions;
        ae2bc$cacheSyncedWords();
    }

    @Unique
    private void ae2bc$cacheSyncedWords() {
        ae2bc$cachedInputDirections0 = ae2bc$inputDirections0;
        ae2bc$cachedInputDirections1 = ae2bc$inputDirections1;
        ae2bc$cachedInputDirections2 = ae2bc$inputDirections2;
        ae2bc$cachedInputDirections3 = ae2bc$inputDirections3;
    }
}
