package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class ModernEarlyDebugOverlayMixin {
    @Shadow @Final public Hud hud;
    @Shadow private Screen screen;
    @Inject(method = "extractRenderState", at = @At(value = "FIELD", target =
            "Lnet/minecraft/client/gui/Gui;overlay:Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    private void nostalgicf3$beforeMenus(CallbackInfo ci, @Local GuiGraphicsExtractor graphics) {
        if (!(screen instanceof DebugOptionsScreen)) hud.extractDebugOverlay(graphics);
    }
    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/Hud;extractDebugOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void nostalgicf3$skipLateOverlay(Hud hud, GuiGraphicsExtractor graphics, Operation<Void> original) {
        // Drawn before the menu above.
    }
}
