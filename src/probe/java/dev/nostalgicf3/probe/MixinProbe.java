package dev.nostalgicf3.probe;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import java.util.Arrays;

/** Test-only mod: force real Fabric/Mixin transformation without opening a game window. */
public final class MixinProbe implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        try {
            Class<?> overlay = Class.forName("net.minecraft.client.gui.components.DebugScreenOverlay", false,
                    Thread.currentThread().getContextClassLoader());
            for (String hook : new String[]{"beginFrame", "captureEntry", "left", "right", "skipRedundantSeparator"}) {
                if (Arrays.stream(overlay.getDeclaredMethods()).noneMatch(m -> m.getName().contains("nostalgicf3$" + hook))) {
                    throw new AssertionError("Missing transformed overlay hook: " + hook);
                }
            }
            Class<?> entries = Class.forName("net.minecraft.client.gui.components.debug.DebugScreenEntryList", false,
                    Thread.currentThread().getContextClassLoader());
            if (Arrays.stream(entries.getDeclaredMethods()).noneMatch(m -> m.getName().contains("nostalgicf3$order"))) {
                throw new AssertionError("Missing entry ordering hook");
            }
            check("net.minecraft.client.gui.components.debug.DebugEntryPosition", "relativePosition");
            String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
                    .getMetadata().getVersion().getFriendlyString();
            if (version.startsWith("1.")) {
                check("net.minecraft.client.renderer.LevelRenderer", "rememberEntities", "entityCounter");
            }
            check(version.startsWith("26.2") || version.startsWith("26.3")
                    ? "net.minecraft.client.gui.Gui" : "net.minecraft.client.renderer.GameRenderer", "beforeMenus", "skipLateOverlay");
            check("net.minecraft.client.gui.components.debug.DebugEntryParticleRenderStats", "totalEntities");
            ParticleCounterProbe.verify();
            CollectorProbe.verify(overlay);
            System.out.println("NOSTALGICF3_MIXIN_PROBE_OK: overlay, ordering, coordinates, menu placement and applicable entity-counter hooks transformed successfully.");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }
    private static void check(String name, String... hooks) throws ClassNotFoundException {
        Class<?> target = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        for (String hook : hooks) {
            if (Arrays.stream(target.getDeclaredMethods()).noneMatch(m -> m.getName().contains("nostalgicf3$" + hook))) {
                throw new AssertionError("Missing transformed hook " + name + ": " + hook);
            }
        }
    }
}
