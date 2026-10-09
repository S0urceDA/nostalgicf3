package dev.nostalgicf3.probe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/** Uses the actual client entity-storage count without constructing a game or world. */
final class ParticleCounterProbe {
    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
    private static void equal(Object actual, String expected) {
        if (!expected.equals(actual)) throw new AssertionError("Particle counter: expected " + expected + " but got " + actual);
    }
    @SuppressWarnings("unchecked")
    static void verify() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> entryType = Class.forName("net.minecraft.client.gui.components.debug.DebugEntryParticleRenderStats", true, loader);
        Method hook = Arrays.stream(entryType.getDeclaredMethods())
                .filter(m -> m.getName().contains("nostalgicf3$totalEntities")).findFirst().orElseThrow();
        hook.setAccessible(true);
        Object entry = entryType.getConstructor().newInstance();
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Object unsafe = field(unsafeType, "theUnsafe").get(null);
        Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
        Class<?> levelType = Class.forName("net.minecraft.client.multiplayer.ClientLevel", false, loader);
        Class<?> managerType = Class.forName("net.minecraft.world.level.entity.TransientEntitySectionManager", false, loader);
        Class<?> lookupType = Class.forName("net.minecraft.world.level.entity.EntityLookup", true, loader);
        Class.forName("net.minecraft.SharedConstants", true, loader).getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap", true, loader).getMethod("bootStrap").invoke(null);
        Class<?> minecraftType = Class.forName("net.minecraft.client.Minecraft", true, loader);
        Field instanceField = field(minecraftType, "instance");
        Object previousClient = instanceField.get(null);
        Object client = allocate.invoke(unsafe, minecraftType);
        Field levelField = minecraftType.getField("level");
        Object level = allocate.invoke(unsafe, levelType);
        Object manager = allocate.invoke(unsafe, managerType);
        Object lookup = lookupType.getConstructor().newInstance();
        field(levelType, "entityStorage").set(level, manager);
        field(managerType, "entityStorage").set(manager, lookup);
        Map<java.util.UUID, Object> byUuid = (Map<java.util.UUID, Object>) field(lookupType, "byUuid").get(lookup);
        try {
            instanceField.set(null, client);
            equal(hook.invoke(entry, "P: 12"), "P: 12");
            levelField.set(client, level);
            equal(hook.invoke(entry, "P: 12"), "P: 12. T: 0");
            for (int i = 0; i < 7; i++) byUuid.put(new java.util.UUID(0, i), null);
            equal(hook.invoke(entry, "P: 12"), "P: 12. T: 7");
            byUuid.remove(new java.util.UUID(0, 0));
            equal(hook.invoke(entry, "P: 0"), "P: 0. T: 6");
            Class<?> particleType = Class.forName("net.minecraft.client.particle.ParticleEngine", true, loader);
            Object particleEngine = allocate.invoke(unsafe, particleType);
            field(particleType, "particles").set(particleEngine, new java.util.HashMap<>());
            field(minecraftType, "particleEngine").set(client, particleEngine);
            String vanillaParticles = "P: " + particleType.getMethod("countParticles").invoke(particleEngine);
            Method display = Arrays.stream(entryType.getDeclaredMethods())
                    .filter(m -> m.getName().equals("display")).findFirst().orElseThrow();
            Class<?> displayerType = display.getParameterTypes()[0];
            java.util.List<String> displayed = new java.util.ArrayList<>();
            Object displayer = java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{displayerType},
                    (proxy, method, args) -> {
                        if (method.getName().equals("addLine")) displayed.add((String) args[0]);
                        return null;
                    });
            Object serverLevel = allocate.invoke(unsafe, Class.forName("net.minecraft.server.level.ServerLevel", true, loader));
            display.invoke(entry, displayer, serverLevel, null, null);
            if (displayed.size() != 1) throw new AssertionError("Particle entry must emit exactly one line");
            equal(displayed.getFirst(), vanillaParticles + ". T: 6");
            displayed.clear();
            display.invoke(entry, displayer, level, null, null);
            equal(displayed.getFirst(), vanillaParticles + ". T: 6");
            levelField.set(client, null);
            equal(hook.invoke(entry, "P: 12"), "P: 12");
        } finally {
            instanceField.set(null, previousClient);
        }
        System.out.println("NOSTALGICF3_PARTICLE_COUNTER_PROBE_OK: transformed particle hook reads Minecraft client-world storage independently of the debug entry world, including zero, changing counts and world teardown.");
    }
}
