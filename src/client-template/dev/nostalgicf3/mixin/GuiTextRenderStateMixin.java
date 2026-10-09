package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nostalgicf3.render.PreparedTextCache;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import @STATE@.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextRenderState.class)
abstract class GuiTextRenderStateMixin {
    @Unique private PreparedTextCache.Entry nostalgicf3$preparedEntry;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void nostalgicf3$rememberF3Text(CallbackInfo ci) {
        nostalgicf3$preparedEntry = PreparedTextCache.current();
    }
    @WrapOperation(method = "ensurePrepared", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;@PREPARE_PARAMS@)Lnet/minecraft/client/gui/Font$PreparedText;"))
    private Font.PreparedText nostalgicf3$reusePreparedText(Font font, FormattedCharSequence text,
            float x, float y, int color, boolean shadow@EMPTY_ARG@, int background,
            Operation<Font.PreparedText> original) {
        PreparedTextCache.Entry entry = nostalgicf3$preparedEntry;
        if (entry == null) return original.call(font, text, x, y, color, shadow@EMPTY_CALL@, background);
        Font.PreparedText prepared = entry.get(font, x, y, color, shadow, @EMPTY_VALUE@, background);
        if (prepared == null) {
            prepared = original.call(font, text, x, y, color, shadow@EMPTY_CALL@, background);
            entry.put(font, x, y, color, shadow, @EMPTY_VALUE@, background, prepared);
        }
        return prepared;
    }
}
