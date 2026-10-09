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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(DebugScreenOverlay.class)
abstract class DebugScreenOverlayMixin {
    @Unique private ClassicLayout.Frame nostalgicf3$frame;
    @Unique private final Map<@ID@, Integer> nostalgicf3$entryIndices = new HashMap<>();
    @Unique private DebugScreenDisplayer nostalgicf3$collector;
    @Unique private DebugScreenDisplayer nostalgicf3$vanilla;
    @Unique private int nostalgicf3$currentIndex;

    @Inject(method = "@RENDER@", at = @At("HEAD"))
    private void nostalgicf3$beginFrame(CallbackInfo ci) {
        if (nostalgicf3$frame != null) nostalgicf3$frame.reset();
    }

    @WrapOperation(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;display(Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void nostalgicf3$captureEntry(DebugScreenEntry entry, DebugScreenDisplayer vanilla,
            Level level, LevelChunk clientChunk, LevelChunk serverChunk, Operation<Void> original,
            @Local @ID@ id) {
        if (nostalgicf3$frame == null) nostalgicf3$frame = new ClassicLayout.Frame();
        Integer index = nostalgicf3$entryIndices.get(id);
        if (index == null) {
            index = ClassicLayout.indexOf(id.toString());
            nostalgicf3$entryIndices.put(id, index);
        }
        if (nostalgicf3$collector == null) nostalgicf3$collector = new DebugScreenDisplayer() {
            public void addPriorityLine(String line) {
                if (nostalgicf3$currentIndex >= 0) nostalgicf3$frame.addLine(nostalgicf3$currentIndex, line);
                else nostalgicf3$vanilla.addPriorityLine(line);
            }
            public void addLine(String line) {
                if (nostalgicf3$currentIndex >= 0) nostalgicf3$frame.addLine(nostalgicf3$currentIndex, line);
                else nostalgicf3$vanilla.addLine(line);
            }
            public void addToGroup(@ID@ group, Collection<String> groupLines) {
                if (nostalgicf3$currentIndex >= 0) nostalgicf3$frame.addLines(nostalgicf3$currentIndex, groupLines);
                else nostalgicf3$frame.captureUnknownGroup(group, groupLines);
            }
            public void addToGroup(@ID@ group, String line) {
                if (nostalgicf3$currentIndex >= 0) nostalgicf3$frame.addLine(nostalgicf3$currentIndex, line);
                else nostalgicf3$frame.captureUnknownGroup(group, line);
            }
        };
        nostalgicf3$currentIndex = index;
        nostalgicf3$vanilla = vanilla;
        try {
            original.call(entry, nostalgicf3$collector, level, clientChunk, serverChunk);
        } finally {
            nostalgicf3$vanilla = null;
            nostalgicf3$currentIndex = -1;
        }
    }

    @WrapOperation(method = "@RENDER@", at = {
            @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0),
            @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2)})
    private boolean nostalgicf3$skipRedundantSeparator(List<?> list, Object line, Operation<Boolean> original) {
        // Vanilla adds these around balanced text; fixed columns need only the chart-hint separator.
        return true;
    }

    @ModifyArg(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/DebugScreenOverlay;@LINES@(Lnet/minecraft/client/gui/@GRAPHICS@;Ljava/util/List;Z@EXTRA_ARG@)V", ordinal = 0), index = 1)
    private List<String> nostalgicf3$left(List<String> footer) {
        return nostalgicf3$frame == null ? footer : nostalgicf3$frame.column(ClassicLayout.Side.LEFT, footer);
    }

    @ModifyArg(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/DebugScreenOverlay;@LINES@(Lnet/minecraft/client/gui/@GRAPHICS@;Ljava/util/List;Z@EXTRA_ARG@)V", ordinal = 1), index = 1)
    private List<String> nostalgicf3$right(List<String> footer) {
        return nostalgicf3$frame == null ? footer : nostalgicf3$frame.column(ClassicLayout.Side.RIGHT, footer);
    }
}
