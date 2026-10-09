package dev.nostalgicf3.layout;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.sun.management.ThreadMXBean;

/** Local assembly benchmark, not a Minecraft FPS benchmark. Never runs during ordinary checks. */
public final class LayoutBenchmark {
    private static volatile int sink;
    private static final int ITERATIONS = 25000;
    private static final String[] IDS = LayoutParityTest.IDS.stream().map(id -> "minecraft:" + id).toArray(String[]::new);
    private static final int[] INDICES = Arrays.stream(IDS).mapToInt(ClassicLayout::indexOf).toArray();
    private static final List<String> ROWS = List.of("Row one", " Row two ", "Row three");
    private static final List<String> LEFT = List.of("Renderer", "", "Debug charts: hints");
    private static final List<String> RIGHT = List.of("Mod version", "");
    private static final ClassicLayout.Frame REUSED = new ClassicLayout.Frame();
    private static void before() {
        ReferenceLayout.Frame frame = new ReferenceLayout.Frame();
        for (String id : IDS) {
            List<ReferenceLayout.Event> events = new ArrayList<>();
            events.add(ReferenceLayout.Event.group(new ArrayList<>(ROWS)));
            frame.capture(id, events);
        }
        frame.captureUnknownGroup("mod:a", new ArrayList<>(ROWS));
        frame.captureUnknownGroup("mod:b", new ArrayList<>(ROWS));
        consume(frame.column(ReferenceLayout.Side.LEFT, LEFT), frame.column(ReferenceLayout.Side.RIGHT, RIGHT));
    }
    private static void after() {
        REUSED.reset();
        for (int index : INDICES) REUSED.addLines(index, ROWS);
        REUSED.captureUnknownGroup("mod:a", ROWS);
        REUSED.captureUnknownGroup("mod:b", ROWS);
        consume(REUSED.column(ClassicLayout.Side.LEFT, LEFT), REUSED.column(ClassicLayout.Side.RIGHT, RIGHT));
    }
    private static void consume(List<String> left, List<String> right) {
        sink = left.size() + right.size() + left.get(1).hashCode() + right.get(0).hashCode();
    }
    private static double[] measure(boolean optimized, ThreadMXBean bean) {
        long thread = Thread.currentThread().threadId();
        long allocated = bean.getThreadAllocatedBytes(thread), start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) if (optimized) after(); else before();
        long elapsed = System.nanoTime() - start;
        return new double[]{(double) elapsed / ITERATIONS, (double) (bean.getThreadAllocatedBytes(thread) - allocated) / ITERATIONS};
    }
    public static void main(String[] args) {
        ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!bean.isThreadAllocatedMemorySupported()) throw new IllegalStateException("Allocation measurement unavailable");
        bean.setThreadAllocatedMemoryEnabled(true);
        for (int i = 0; i < 20000; i++) { before(); after(); }
        double[] oldNs = new double[7], newNs = new double[7], oldBytes = new double[7], newBytes = new double[7];
        for (int round = 0; round < 7; round++) {
            double[] oldResult, newResult;
            if ((round & 1) == 0) { oldResult = measure(false, bean); newResult = measure(true, bean); }
            else { newResult = measure(true, bean); oldResult = measure(false, bean); }
            oldNs[round] = oldResult[0]; oldBytes[round] = oldResult[1];
            newNs[round] = newResult[0]; newBytes[round] = newResult[1];
        }
        Arrays.sort(oldNs); Arrays.sort(newNs); Arrays.sort(oldBytes); Arrays.sort(newBytes);
        System.out.printf("Assembly benchmark: 35 entries x 3 rows + 2 mod groups, %d iterations/round, median of 7 alternating rounds%n", ITERATIONS);
        System.out.printf("0.1.6: %.0f ns/frame, %.0f allocated bytes/frame%n", oldNs[3], oldBytes[3]);
        System.out.printf("Optimized: %.0f ns/frame, %.0f allocated bytes/frame%n", newNs[3], newBytes[3]);
        System.out.printf("Reduction: %.1f%% assembly time, %.1f%% allocation%n", 100 * (1 - newNs[3] / oldNs[3]), 100 * (1 - newBytes[3] / oldBytes[3]));
    }
}
