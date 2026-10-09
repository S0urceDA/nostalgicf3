package dev.nostalgicf3.probe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** Exercises the real transformed collector without initializing Minecraft or creating a window. */
final class CollectorProbe {
    private static Method hook(Class<?> target, String suffix) {
        Method method = Arrays.stream(target.getDeclaredMethods())
                .filter(m -> m.getName().contains("nostalgicf3$" + suffix)).findFirst().orElseThrow();
        method.setAccessible(true);
        return method;
    }
    private static Field field(Class<?> target, String suffix) {
        Field field = Arrays.stream(target.getDeclaredFields())
                .filter(f -> f.getName().contains("nostalgicf3$" + suffix)).findFirst().orElseThrow();
        field.setAccessible(true);
        return field;
    }
    private static void equal(Object actual, Object expected) {
        if (!actual.equals(expected)) throw new AssertionError("Collector expected " + expected + " but got " + actual);
    }
    static void verify(Class<?> overlay) throws Exception {
        // Constructor-free allocation is confined to this test-only mod. Render hooks used here need no game state.
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeType.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Object instance = unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, overlay);
        field(overlay, "entryIndices").set(instance, new HashMap<>());
        Method capture = hook(overlay, "captureEntry");
        Class<?>[] types = capture.getParameterTypes();
        Class<?> entryType = types[0], displayerType = types[1], operationType = types[5], localType = types[6];
        ClassLoader loader = overlay.getClassLoader();
        String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
        Class<?> idType = Class.forName("net.minecraft.resources." + (version.equals("1.21.10") ? "ResourceLocation" : "Identifier"), true, loader);
        Method parse = idType.getMethod("parse", String.class);
        Object group = parse.invoke(null, "probe:group");
        Method addLine = displayerType.getMethod("addLine", String.class);
        Method addPriority = displayerType.getMethod("addPriorityLine", String.class);
        Method addGroup = displayerType.getMethod("addToGroup", idType, java.util.Collection.class);
        Method addSingleGroup = displayerType.getMethod("addToGroup", idType, String.class);
        Method display = entryType.getMethod("display", displayerType, types[2], types[3], types[4]);
        List<String> forwarded = new ArrayList<>();
        Object vanilla = Proxy.newProxyInstance(loader, new Class<?>[]{displayerType}, (proxy, method, args) -> {
            forwarded.add(method.getName() + ":" + args[0]);
            return null;
        });
        Object operation = Proxy.newProxyInstance(loader, new Class<?>[]{operationType}, (proxy, method, args) -> {
            if (!method.getName().equals("call")) throw new AssertionError(method);
            Object[] call = (Object[]) args[0];
            display.invoke(call[0], call[1], call[2], call[3], call[4]);
            return null;
        });
        Class<?> callbackType = hook(overlay, "beginFrame").getParameterTypes()[0];
        Object callback = callbackType.getConstructor(String.class, boolean.class).newInstance("probe", false);
        Method begin = hook(overlay, "beginFrame"), left = hook(overlay, "left"), right = hook(overlay, "right");
        begin.invoke(instance, callback);
        for (String path : List.of("fps", "simple_performance_impactors", "system_specs", "memory", "mod_entry")) {
            Object entry = Proxy.newProxyInstance(loader, new Class<?>[]{entryType}, (proxy, method, args) -> {
                if (!method.getName().equals("display")) throw new AssertionError(method);
                Object collector = args[0];
                switch (path) {
                    case "fps" -> addLine.invoke(collector, "60 fps vsync ");
                    case "simple_performance_impactors" -> addLine.invoke(collector, " fancy-clouds");
                    case "system_specs" -> addGroup.invoke(collector, group, List.of("Java", "CPU"));
                    case "memory" -> addSingleGroup.invoke(collector, group, "Mem");
                    default -> {
                        addPriority.invoke(collector, "Renderer");
                        addLine.invoke(collector, "Mod version");
                        addGroup.invoke(collector, group, List.of("Mod A"));
                        addSingleGroup.invoke(collector, group, "Mod B");
                    }
                }
                return null;
            });
            Object id = parse.invoke(null, path.equals("mod_entry") ? "probe:entry" : "minecraft:" + path);
            // MixinExtras rewrites @Local parameters to LocalRef in the transformed handler.
            Object local = localType.isInstance(id) ? id : Proxy.newProxyInstance(loader, new Class<?>[]{localType},
                    (proxy, method, args) -> { if (method.getName().equals("get")) return id; throw new AssertionError(method); });
            capture.invoke(instance, entry, vanilla, null, null, null, operation, local);
        }
        equal(left.invoke(instance, List.of("Renderer", "", "Debug charts: hints")),
                List.of("60 fps vsync fancy-clouds", "Renderer", "", "Debug charts: hints"));
        equal(right.invoke(instance, List.of("Mod version", "")),
                List.of("Mem", "", "Java", "CPU", "", "Mod version", "", "Mod A", "Mod B", ""));
        equal(forwarded, List.of("addPriorityLine:Renderer", "addLine:Mod version"));
        Object collector = field(overlay, "collector").get(instance);
        begin.invoke(instance, callback);
        equal(left.invoke(instance, List.of()), List.of());
        equal(right.invoke(instance, List.of()), List.of());
        if (field(overlay, "collector").get(instance) != collector || field(overlay, "vanilla").get(instance) != null) {
            throw new AssertionError("Collector not reused or vanilla lists retained");
        }
        // Empty overlay path must work before the lazy frame/collector have ever been allocated.
        Object empty = unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, overlay);
        begin.invoke(empty, callback);
        equal(left.invoke(empty, List.of("Hints")), List.of("Hints"));
        equal(right.invoke(empty, List.of()), List.of());
        System.out.println("NOSTALGICF3_COLLECTOR_PROBE_OK: real transformed capture, group merging, forwarding, reset and lazy empty path.");
    }
}
