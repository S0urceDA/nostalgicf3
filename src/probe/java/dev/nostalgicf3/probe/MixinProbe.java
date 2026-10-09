package dev.nostalgicf3.probe;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import java.util.Arrays;

/** Test-only mod: force real Fabric/Mixin transformation without opening a game window. */
public final class MixinProbe implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        try {
            Class<?> overlay = Class.forName("net.minecraft.client.gui.components.DebugScreenOverlay", false,
                    Thread.currentThread().getContextClassLoader());
            for (String hook : new String[]{"beginFrame", "captureEntry", "left", "right"}) {
                if (Arrays.stream(overlay.getDeclaredMethods()).noneMatch(m -> m.getName().contains("nostalgicf3$" + hook))) {
                    throw new AssertionError("Missing transformed overlay hook: " + hook);
                }
            }
            System.out.println("NOSTALGICF3_MIXIN_PROBE_OK: all four overlay hooks transformed successfully.");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
}
