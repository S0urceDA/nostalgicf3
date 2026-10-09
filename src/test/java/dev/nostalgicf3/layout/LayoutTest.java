package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static dev.nostalgicf3.layout.ClassicLayout.Side.*;

/** Deterministic regression checks exercise changing visibility, multiline entries, and namespaces. */
public final class LayoutTest {
    private static void equal(Object actual, Object expected) {
        if (!actual.equals(expected)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
    public static void main(String[] args) {
        List<String> ids = List.of("game_version", "fps", "tps", "chunk_render_stats", "player_position",
                "biome", "memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Random random = new Random(42);
        for (int mask = 0; mask < 1 << ids.size(); mask++) {
            ClassicLayout.Frame frame = new ClassicLayout.Frame();
            List<String> active = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) if ((mask & 1 << i) != 0) active.add(ids.get(i));
            Collections.shuffle(active, random);
            for (String id : active) frame.add("minecraft:" + id, List.of(id));
            List<String> left = frame.column(LEFT, List.of());
            List<String> right = frame.column(RIGHT, List.of());
            equal(left.stream().filter(s -> !s.isEmpty()).toList(), ids.stream().filter(active::contains)
                    .filter(id -> ClassicLayout.sideOf("minecraft:" + id) == LEFT).toList());
            equal(right.stream().filter(s -> !s.isEmpty()).toList(), ids.stream().filter(active::contains)
                    .filter(id -> ClassicLayout.sideOf("minecraft:" + id) == RIGHT).toList());
            if (!left.isEmpty() && left.getFirst().isEmpty()) throw new AssertionError("Leading separator");
            if (!right.isEmpty() && right.getLast().isEmpty()) throw new AssertionError("Trailing separator");
        }
        ClassicLayout.Frame performance = new ClassicLayout.Frame();
        performance.add("minecraft:gpu_utilization", List.of("GPU"));
        performance.add("minecraft:simple_performance_impactors", List.of("T", "D"));
        performance.add("minecraft:fps", List.of("60 fps"));
        equal(performance.column(LEFT, List.of()), List.of("60 fps T D GPU"));
        ClassicLayout.Frame grouped = new ClassicLayout.Frame();
        grouped.add("minecraft:looking_at_block", List.of("Target", "property", "#tag"));
        grouped.add("other:memory", List.of("mod memory"));
        grouped.add("minecraft:memory", List.of("Mem", "Alloc"));
        equal(grouped.column(RIGHT, List.of()), List.of("Mem", "Alloc", "", "Target", "property", "#tag"));
        equal(grouped.column(LEFT, List.of("", "Charts", "To edit")), List.of("mod memory", "", "Charts", "To edit"));
        equal(new ClassicLayout.Frame().column(LEFT, List.of("", "Charts")), List.of("Charts"));
        ClassicLayout.Frame next = new ClassicLayout.Frame();
        equal(next.column(RIGHT, List.of()), List.of());
        System.out.println("Layout checks passed: 2048 visibility combinations, shuffled order, grouped output, FPS merging, footer and frame reset.");
    }
}
