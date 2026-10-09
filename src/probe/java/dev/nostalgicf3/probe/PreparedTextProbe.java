package dev.nostalgicf3.probe;

import java.lang.reflect.*;
import java.util.Arrays;

final class PreparedTextProbe {
    static void verify(String version) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> cacheType = Class.forName("dev.nostalgicf3.render.PreparedTextCache", true, loader);
        Class<?> entryType = Class.forName("dev.nostalgicf3.render.PreparedTextCache$Entry", true, loader);
        Object cache = cacheType.getConstructor().newInstance();
        Method select = cacheType.getMethod("select", String.class);
        Object entry = select.invoke(cache, "Static line");
        if (select.invoke(cache, new String("Static line")) != entry) throw new AssertionError("Equal text missed");
        if (select.invoke(cache, "\u00a7kRandom") != null) throw new AssertionError("Obfuscated glyphs cached");
        Class<?> fontType = Class.forName("net.minecraft.client.gui.Font", true, loader);
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field uf = unsafeType.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        Object font = unsafeType.getMethod("allocateInstance", Class.class).invoke(uf.get(null), fontType);
        Class<?> preparedType = Class.forName("net.minecraft.client.gui.Font$PreparedText", true, loader);
        Object prepared = Proxy.newProxyInstance(loader, new Class<?>[]{preparedType}, (proxy, method, args) -> null);
        Class<?> stateType = Class.forName(version.startsWith("1.")
                ? "net.minecraft.client.gui.render.state.GuiTextRenderState"
                : "net.minecraft.client.renderer.state.gui.GuiTextRenderState", true, loader);
        cacheType.getMethod("setCurrent", entryType).invoke(null, entry);
        Object pose = Class.forName("org.joml.Matrix3x2f", true, loader).getConstructor().newInstance();
        Constructor<?> ctor = stateType.getConstructors()[0];
        Object state;
        try {
            state = ctor.getParameterCount() == 10
                    ? ctor.newInstance(font, null, pose, 2, 3, -1, 0, false, false, null)
                    : ctor.newInstance(font, null, pose, 2, 3, -1, 0, false, null);
        } finally { cacheType.getMethod("setCurrent", entryType).invoke(null, new Object[]{null}); }
        Method hook = Arrays.stream(stateType.getDeclaredMethods())
                .filter(m -> m.getName().contains("nostalgicf3$reusePreparedText")).findFirst().orElseThrow();
        hook.setAccessible(true);
        Class<?> opType = hook.getParameterTypes()[hook.getParameterCount() - 1];
        int[] calls = {0};
        Object op = Proxy.newProxyInstance(loader, new Class<?>[]{opType}, (proxy, method, args) -> {
            calls[0]++; return prepared;
        });
        boolean old = version.equals("1.21.9") || version.equals("1.21.10");
        Object[] args = old ? new Object[]{font, null, 2f, 3f, -1, false, 0, op}
                : new Object[]{font, null, 2f, 3f, -1, false, false, 0, op};
        hook.invoke(state, args); hook.invoke(state, args);
        if (calls[0] != 1) throw new AssertionError("Prepared text not reused");
        args[2] = 4f; hook.invoke(state, args);
        if (calls[0] != 2) throw new AssertionError("Position change not invalidated");
        Class.forName("dev.nostalgicf3.layout.TextWidthCache", true, loader).getMethod("invalidateFonts").invoke(null);
        hook.invoke(state, args);
        if (calls[0] != 3) throw new AssertionError("Font reload not invalidated");
        Object replacement = select.invoke(cache, "Static line");
        if (replacement == entry) throw new AssertionError("Cache retained reloaded glyphs");
        Object outside = ctor.getParameterCount() == 10
                ? ctor.newInstance(font, null, pose, 2, 3, -1, 0, false, false, null)
                : ctor.newInstance(font, null, pose, 2, 3, -1, 0, false, null);
        hook.invoke(outside, args); hook.invoke(outside, args);
        if (calls[0] != 5) throw new AssertionError("Cached text outside F3");
        System.out.println("NOSTALGICF3_PREPARED_TEXT_PROBE_OK: equal text reuses preparation; movement/reload invalidate; obfuscated/non-F3 text uncached.");
    }
}
