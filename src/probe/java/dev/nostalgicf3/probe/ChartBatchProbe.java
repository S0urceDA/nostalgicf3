package dev.nostalgicf3.probe;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

/** Uses actual game rectangle emitters and transformed GUI submission. No GPU is needed. */
final class ChartBatchProbe {
    static void verify(String version) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String pkg = version.startsWith("1.") ? "net.minecraft.client.gui.render.state."
                : "net.minecraft.client.renderer.state.gui.";
        Class<?> rectangleType = Class.forName(pkg + "ColoredRectangleRenderState", true, loader);
        Class<?> boundsType = Class.forName("net.minecraft.client.gui.navigation.ScreenRectangle", true, loader);
        Class<?> vertexType = Class.forName("com.mojang.blaze3d.vertex.VertexConsumer", true, loader);
        Object pose = Class.forName("org.joml.Matrix3x2f", true, loader).getConstructor().newInstance();
        Constructor<?> ctor = Arrays.stream(rectangleType.getConstructors())
                .filter(c -> c.getParameterCount() == 11).findFirst().orElseThrow();
        List<Object> rectangles = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            int top = 60 - i % 60;
            Object bounds = boundsType.getConstructor(int.class, int.class, int.class, int.class)
                    .newInstance(i, top, 1, 60 - top);
            rectangles.add(ctor.newInstance(null, null, pose, i, top, i + 1, 60,
                    0xff000000 | i, 0xff000000 | i, null, bounds));
        }
        Method emit = rectangleType.getMethod("buildVertices", vertexType);
        List<String> expected = new ArrayList<>(), actual = new ArrayList<>();
        Object expectedConsumer = recorder(loader, vertexType, expected);
        for (Object rectangle : rectangles) emit.invoke(rectangle, expectedConsumer);
        Class<?> compositeType = Class.forName("dev.nostalgicf3.render.ChartBatch$Rectangles", true, loader);
        Object composite = compositeType.getConstructor(List.class).newInstance(rectangles);
        compositeType.getMethod("buildVertices", vertexType).invoke(composite, recorder(loader, vertexType, actual));
        if (!expected.equals(actual)) throw new AssertionError("Chart vertices/order/colors changed");
        Object union = compositeType.getMethod("bounds").invoke(composite);
        if ((int) boundsType.getMethod("left").invoke(union) != 0
                || (int) boundsType.getMethod("right").invoke(union) != 500)
            throw new AssertionError("Wrong chart bounds");

        Class<?> batchType = Class.forName("dev.nostalgicf3.render.ChartBatch", true, loader);
        Class<?> stateType = Class.forName(pkg + "GuiRenderState", true, loader);
        Class<?> elementType = Class.forName(pkg + "GuiElementRenderState", true, loader);
        Object state = stateType.getConstructor().newInstance();
        Method submit = stateType.getMethod(version.startsWith("1.") ? "submitGuiElement" : "addGuiElement", elementType);
        Object batch = batchType.getMethod("begin").invoke(null);
        try { for (Object rectangle : rectangles) submit.invoke(state, rectangle); }
        finally { batchType.getMethod("end").invoke(batch); }
        Class<?> rangeType = Class.forName(pkg + "GuiRenderState$TraverseRange", true, loader);
        Method each = stateType.getMethod("forEachElement", Consumer.class, rangeType);
        List<Object> submitted = new ArrayList<>();
        for (Object range : rangeType.getEnumConstants()) {
            submitted.clear();
            each.invoke(state, (Consumer<Object>) submitted::add, range);
            if (!submitted.isEmpty()) break;
        }
        if (submitted.size() != 1 || !compositeType.isInstance(submitted.get(0)))
            throw new AssertionError("Expected one GUI submission for 500 chart bars, got " + submitted.size());
        // Outside chart extraction, submissions must remain untouched.
        submit.invoke(state, rectangles.get(0));
        submitted.clear();
        for (Object range : rangeType.getEnumConstants()) {
            submitted.clear(); each.invoke(state, (Consumer<Object>) submitted::add, range);
            if (!submitted.isEmpty()) break;
        }
        if (submitted.size() != 2) throw new AssertionError("Chart scope leaked");
        System.out.println("NOSTALGICF3_CHART_BATCH_PROBE_OK: 500 rectangles -> one submission, identical vertex/color order and bounded scope.");
    }
    private static Object recorder(ClassLoader loader, Class<?> vertexType, List<String> output) {
        return Proxy.newProxyInstance(loader, new Class<?>[]{vertexType}, (proxy, method, args) -> {
            if (method.getName().equals("toString")) return "VertexRecorder";
            output.add(method.getName() + Arrays.toString(args));
            return method.getReturnType().isAssignableFrom(vertexType) ? proxy : null;
        });
    }
}
