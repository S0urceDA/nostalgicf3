package dev.nostalgicf3.mixin;

import net.minecraft.client.gui.components.debug.DebugEntryParticleRenderStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DebugEntryParticleRenderStats.class)
abstract class DebugEntryParticleRenderStatsMixin {
    @ModifyArg(method = "display", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;addLine(Ljava/lang/String;)V"), index = 0)
    private String nostalgicf3$totalEntities(String line) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        return clientLevel != null ? line + ". T: " + clientLevel.getEntityCount() : line;
    }
}
