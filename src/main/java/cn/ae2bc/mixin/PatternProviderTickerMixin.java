package cn.ae2bc.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.helpers.patternprovider.PatternProviderLogic;
import cn.ae2bc.extension.PatternProviderExtractionExtension;
import cn.ae2bc.logic.ProductExtractionTickState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.helpers.patternprovider.PatternProviderLogic$Ticker")
public abstract class PatternProviderTickerMixin {
    @Shadow @Final private PatternProviderLogic this$0;

    @Inject(method = "getTickingRequest", at = @At("RETURN"), cancellable = true)
    private void ae2bc$configureProductExtractionTickRange(IGridNode node,
                                                            CallbackInfoReturnable<TickingRequest> cir) {
        TickingRequest original = cir.getReturnValue();
        // Tick requests are immutable after node registration. A minimum of one supports a one-tick interval,
        // while preserving AE2's maximum lets idle extraction checks back off normally.
        boolean sleeping = original.isSleeping()
                && !((PatternProviderExtractionExtension) this$0).ae2bc$hasProductExtractionCard();
        cir.setReturnValue(new TickingRequest(1, original.maxTickRate(), sleeping,
                original.initialTickRate()));
    }

    @Inject(method = "tickingRequest", at = @At("RETURN"), cancellable = true)
    private void ae2bc$extractProducts(IGridNode node, int ticksSinceLastCall,
                                        CallbackInfoReturnable<TickRateModulation> cir) {
        var extension = (PatternProviderExtractionExtension) this$0;
        ProductExtractionTickState extraction = extension.ae2bc$tickProductExtraction();
        if (extraction == ProductExtractionTickState.DISABLED) {
            return;
        }

        TickRateModulation original = cir.getReturnValue();
        if (original == TickRateModulation.URGENT || original == TickRateModulation.FASTER) {
            return;
        }
        if (extraction == ProductExtractionTickState.ATTEMPTED) {
            cir.setReturnValue(TickRateModulation.URGENT);
        } else {
            cir.setReturnValue(TickRateModulation.SLOWER);
        }
    }
}
