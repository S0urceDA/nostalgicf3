package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.@GRAPHICS@;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class EarlyDebugOverlayMixin {
    @Inject(method = "@GUI_METHOD@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    private void nostalgicf3$beforeMenus(CallbackInfo ci, @Local @GRAPHICS@ graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof DebugOptionsScreen)) minecraft.gui.@DEBUG_METHOD@(graphics);
    }
    @WrapOperation(method = "@GUI_METHOD@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/Gui;@DEBUG_METHOD@(Lnet/minecraft/client/gui/@GRAPHICS@;)V"))
    private void nostalgicf3$skipLateOverlay(Gui gui, @GRAPHICS@ graphics, Operation<Void> original) {
        // Drawn before the menu above.
    }
}
