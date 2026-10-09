package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static dev.nostalgicf3.layout.ClassicLayout.Side.*;
import static dev.nostalgicf3.layout.ClassicLayout.Event.*;

public final class LayoutTest {
    private static void equal(Object actual, Object expected) {
        if (!actual.equals(expected)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
    private static void add(ClassicLayout.Frame frame, String id, String... lines) {
        frame.capture("minecraft:" + id, List.of(group(List.of(lines))));
    }
    public static void main(String[] args) {
        List<String> ids = List.of("game_version", "fps", "tps", "chunk_render_stats", "player_position",
                "biome", "memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Set<String> rightIds = Set.of("memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Random random = new Random(42);
        for (int mask = 0; mask < 1 << ids.size(); mask++) {
            ClassicLayout.Frame frame = new ClassicLayout.Frame();
            List<String> active = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) if ((mask & 1 << i) != 0) active.add(ids.get(i));
            List<String> left = new ArrayList<>(), right = new ArrayList<>();
            for (String id : active) {
                if (!rightIds.contains(id)) {
                    if (id.equals("player_position")) left.add("");
                    left.add(id);
                } else {
                    boolean target = id.startsWith("looking_at_");
                    boolean afterMemory = id.equals("system_specs") && active.contains("memory");
                    if (target || afterMemory) right.add("");
                    right.add(id);
                }
            }
            Collections.shuffle(active, random);
            for (String id : active) add(frame, id, id);
            equal(frame.column(LEFT, List.of()), left);
            equal(frame.column(RIGHT, List.of()), right);
        }
        ClassicLayout.Frame performance = new ClassicLayout.Frame();
        add(performance, "gpu_utilization", "GPU");
        add(performance, "simple_performance_impactors", "T", "D");
        add(performance, "fps", "60 fps");
        equal(performance.column(LEFT, List.of()), List.of("60 fps T D GPU"));
        ClassicLayout.Frame noFps = new ClassicLayout.Frame();
        add(noFps, "gpu_utilization", "GPU");
        add(noFps, "simple_performance_impactors", "T", "D");
        equal(noFps.column(LEFT, List.of()), List.of("T D GPU"));

        ClassicLayout.Frame day = new ClassicLayout.Frame();
        add(day, "day_count", "Day 42");
        add(day, "biome", "Biome");
        equal(day.column(LEFT, List.of()), List.of("Biome", "Day 42"));
        add(day, "local_difficulty", "Local Difficulty: 2.0");
        equal(day.column(LEFT, List.of()), List.of("Biome", "Local Difficulty: 2.0 (Day 42)"));

        ClassicLayout.Frame targets = new ClassicLayout.Frame();
        targets.capture("minecraft:looking_at_block_state", List.of(group(List.of()), group(List.of("Block")), group(List.of("State"))));
        add(targets, "looking_at_block_tags", "#tag");
        add(targets, "looking_at_fluid_state");
        add(targets, "looking_at_fluid_tags");
        add(targets, "looking_at_entity");
        add(targets, "memory", "Mem");
        add(targets, "system_specs", "Java", "CPU");
        equal(targets.column(RIGHT, List.of()), List.of("Mem", "", "Java", "CPU", "", "Block", "State", "#tag"));

        ClassicLayout.Frame modern = new ClassicLayout.Frame();
        for (String id : List.of("sound_cache", "sound_mood", "day_count", "local_difficulty", "looking_at_entity_tags",
                "detailed_memory", "looking_at_entity", "looking_at_fluid_tags", "looking_at_fluid_state",
                "looking_at_block_tags", "looking_at_block_state", "system_specs", "memory")) add(modern, id, id);
        equal(modern.column(RIGHT, List.of()), List.of("memory", "detailed_memory", "", "system_specs", "",
                "looking_at_block_state", "looking_at_block_tags", "", "looking_at_fluid_state",
                "looking_at_fluid_tags", "", "looking_at_entity", "looking_at_entity_tags"));
        equal(modern.column(LEFT, List.of()), List.of("local_difficulty (day_count)", "sound_mood", "sound_cache"));
        ClassicLayout.Frame noSpecs = new ClassicLayout.Frame();
        add(noSpecs, "memory", "Mem");
        add(noSpecs, "detailed_memory", "Allocation");
        add(noSpecs, "looking_at_block_state", "Block");
        equal(noSpecs.column(RIGHT, List.of()), List.of("Mem", "Allocation", "", "Block"));
        equal(new ClassicLayout.Frame().column(LEFT, List.of("Third-party priority", "", "Charts")),
                List.of("Third-party priority", "", "Charts"));
        equal(ClassicLayout.isKnown("other:memory"), false);
        equal(ClassicLayout.relativePosition("Block: -1 -17 16", -1, -17, 16), "Block: -1 -17 16 [15 15 0]");
        equal(ClassicLayout.relativePosition("Block: 0 15 31", 0, 15, 31), "Block: 0 15 31 [0 15 15]");
        System.out.println("Layout checks passed: 2048 visibility fixtures, modern placement, empty/repeated targets, disabled FPS/difficulty, memory spacing, vanilla remainder, and negative relative coordinates.");
    }
}
