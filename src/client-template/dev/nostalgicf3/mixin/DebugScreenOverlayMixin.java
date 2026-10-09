package dev.nostalgicf3.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.nostalgicf3.layout.ClassicLayout;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.@ID@;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(DebugScreenOverlay.class)
abstract class DebugScreenOverlayMixin {
    @Unique private ClassicLayout.Frame nostalgicf3$frame;

    @Inject(method = "@RENDER@", at = @At("HEAD"))
    private void nostalgicf3$beginFrame(CallbackInfo ci) {
        nostalgicf3$frame = new ClassicLayout.Frame();
    }

    @WrapOperation(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;display(Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void nostalgicf3$captureEntry(DebugScreenEntry entry, DebugScreenDisplayer vanilla,
            Level level, LevelChunk clientChunk, LevelChunk serverChunk, Operation<Void> original,
            @Local @ID@ id) {
        List<String> lines = new ArrayList<>();
        DebugScreenDisplayer capture = new DebugScreenDisplayer() {
            public void addPriorityLine(String line) { lines.add(line); }
            public void addLine(String line) { lines.add(line); }
            public void addToGroup(@ID@ group, Collection<String> groupLines) { lines.addAll(groupLines); }
            public void addToGroup(@ID@ group, String line) { lines.add(line); }
        };
        original.call(entry, capture, level, clientChunk, serverChunk);
        nostalgicf3$frame.add(id.toString(), lines);
    }

    @ModifyArg(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/DebugScreenOverlay;@LINES@(Lnet/minecraft/client/gui/@GRAPHICS@;Ljava/util/List;Z)V", ordinal = 0), index = 1)
    private List<String> nostalgicf3$left(List<String> footer) {
        return nostalgicf3$frame.column(ClassicLayout.Side.LEFT, footer);
    }

    @ModifyArg(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/DebugScreenOverlay;@LINES@(Lnet/minecraft/client/gui/@GRAPHICS@;Ljava/util/List;Z)V", ordinal = 1), index = 1)
    private List<String> nostalgicf3$right(List<String> footer) {
        return nostalgicf3$frame.column(ClassicLayout.Side.RIGHT, footer);
    }
}
