package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.nostalgicf3.render.ChartBatch;
import net.minecraft.client.gui.@GRAPHICS@;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractDebugChart.class)
abstract class DebugChartMixin {
    @WrapMethod(method = "@CHART@")
    private void nostalgicf3$batchChart(@GRAPHICS@ graphics, int x, int width@CHART_EXTRA@, Operation<Void> original) {
        ChartBatch batch = ChartBatch.begin();
        try { original.call(graphics, x, width@CHART_CALL@); }
        finally { batch.end(); }
    }
}
