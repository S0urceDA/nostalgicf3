package dev.nostalgicf3.mixin;

import dev.nostalgicf3.layout.TextWidthCache;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontManager.class)
abstract class FontManagerMixin {
    @Inject(method = {"apply", "updateOptions"}, at = @At("RETURN"))
    private void nostalgicf3$invalidateWidths(CallbackInfo ci) {
        TextWidthCache.invalidateFonts();
    }
}
