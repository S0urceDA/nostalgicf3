package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nostalgicf3.layout.ClassicLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Locale;

@Mixin(DebugEntryPosition.class)
abstract class DebugEntryPositionMixin {
    @WrapOperation(method = "display", at = @At(value = "INVOKE", target =
            "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", ordinal = 1))
    private String nostalgicf3$relativePosition(Locale locale, String format, Object[] args, Operation<String> original) {
        String line = original.call(locale, format, args);
        var camera = Minecraft.getInstance().getCameraEntity();
        if (camera == null) return line;
        var pos = camera.blockPosition();
        return ClassicLayout.relativePosition(line, pos.getX(), pos.getY(), pos.getZ());
    }
}
