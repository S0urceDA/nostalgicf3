package dev.nostalgicf3.mixin;

import dev.nostalgicf3.render.ChartBatch;
import @STATE@.GuiElementRenderState;
import @STATE@.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
abstract class GuiRenderStateMixin {
    @Inject(method = "@ADD_ELEMENT@", at = @At("HEAD"), cancellable = true)
    private void nostalgicf3$collectChartRectangles(GuiElementRenderState element, CallbackInfo ci) {
        if (ChartBatch.capture((GuiRenderState) (Object) this, element)) ci.cancel();
    }
    @Inject(method = {"@BARRIERS@", "nextStratum", "blurBeforeThisStratum", "up", "reset"}, at = @At("HEAD"))
    private void nostalgicf3$flushChartRectangles(CallbackInfo ci) {
        ChartBatch.barrier();
    }
}
