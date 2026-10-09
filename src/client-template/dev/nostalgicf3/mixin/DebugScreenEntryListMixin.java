package dev.nostalgicf3.mixin;

import dev.nostalgicf3.layout.ClassicLayout;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.@ID@;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.util.Comparator;

@Mixin(DebugScreenEntryList.class)
abstract class DebugScreenEntryListMixin {
    @ModifyArg(method = "rebuildCurrentList", at = @At(value = "INVOKE",
            target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), index = 0)
    private Comparator<@ID@> nostalgicf3$order(Comparator<@ID@> original) {
        return (first, second) -> ClassicLayout.compareIds(first.toString(), second.toString());
    }
}
