package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
abstract class LegacyEntityCounterMixin {
    @Unique private int nostalgicf3$renderedEntities;
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/state/LevelRenderState;reset()V"))
    private void nostalgicf3$rememberEntities(LevelRenderState state, Operation<Void> original) {
        nostalgicf3$renderedEntities = state.entityRenderStates.size();
        original.call(state);
    }
    @ModifyExpressionValue(method = "getEntityStatistics", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int nostalgicf3$entityCounter(int clearedCount) { return nostalgicf3$renderedEntities; }
}
