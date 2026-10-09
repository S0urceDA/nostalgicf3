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
        boolean known = ClassicLayout.isKnown(id.toString());
        List<ClassicLayout.Event> events = new ArrayList<>();
        DebugScreenDisplayer capture = new DebugScreenDisplayer() {
            public void addPriorityLine(String line) {
                if (known) events.add(ClassicLayout.Event.line(line)); else vanilla.addPriorityLine(line);
            }
            public void addLine(String line) {
                if (known) events.add(ClassicLayout.Event.line(line)); else vanilla.addLine(line);
            }
            public void addToGroup(@ID@ group, Collection<String> groupLines) {
                if (known) events.add(ClassicLayout.Event.group(new ArrayList<>(groupLines)));
                else nostalgicf3$frame.captureUnknownGroup(group.toString(), new ArrayList<>(groupLines));
            }
            public void addToGroup(@ID@ group, String line) {
                addToGroup(group, List.of(line));
            }
        };
        original.call(entry, capture, level, clientChunk, serverChunk);
        if (known) nostalgicf3$frame.capture(id.toString(), events);
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
        return nostalgicf3$frame.column(ClassicLayout.Side.LEFT, footer);
    }

    @ModifyArg(method = "@RENDER@", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/components/DebugScreenOverlay;@LINES@(Lnet/minecraft/client/gui/@GRAPHICS@;Ljava/util/List;Z@EXTRA_ARG@)V", ordinal = 1), index = 1)
    private List<String> nostalgicf3$right(List<String> footer) {
        return nostalgicf3$frame.column(ClassicLayout.Side.RIGHT, footer);
    }
}
