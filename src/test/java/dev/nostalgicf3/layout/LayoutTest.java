package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static dev.nostalgicf3.layout.ClassicLayout.Side.*;
import static dev.nostalgicf3.layout.ClassicLayout.Event.*;

/** Exact row fixtures derived from J9's ordering and displayer rules. */
public final class LayoutTest {
    private static void equal(Object actual, Object expected) {
        if (!actual.equals(expected)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }
    public static void main(String[] args) {
        List<String> ids = List.of("game_version", "fps", "tps", "chunk_render_stats", "player_position",
                "biome", "memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Set<String> rightIds = Set.of("memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Set<String> grouped = Set.of("player_position", "memory", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Set<String> separated = Set.of("player_position", "system_specs", "looking_at_block", "looking_at_fluid", "looking_at_entity");
        Random random = new Random(42);
        for (int mask = 0; mask < 1 << ids.size(); mask++) {
            ClassicLayout.Frame frame = new ClassicLayout.Frame();
            List<String> active = new ArrayList<>();
            List<String> expectedLeft = new ArrayList<>(), expectedRight = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) if ((mask & 1 << i) != 0) {
                String id = ids.get(i);
                active.add(id);
                List<String> column = rightIds.contains(id) ? expectedRight : expectedLeft;
                if (separated.contains(id)) column.add("");
                column.add(id);
            }
            Collections.shuffle(active, random);
            for (String id : active) frame.capture("minecraft:" + id,
                    List.of(grouped.contains(id) ? group(List.of(id)) : line(id)));
            equal(frame.column(LEFT, List.of()), expectedLeft);
            equal(frame.column(RIGHT, List.of()), expectedRight);
        }
        ClassicLayout.Frame performance = new ClassicLayout.Frame();
        performance.capture("minecraft:gpu_utilization", List.of(line("GPU")));
        performance.capture("minecraft:simple_performance_impactors", List.of(line("T"), line("D")));
        performance.capture("minecraft:fps", List.of(line("60 fps")));
        equal(performance.column(LEFT, List.of()), List.of("60 fps T D GPU"));
        ClassicLayout.Frame world = new ClassicLayout.Frame();
        world.capture("minecraft:player_position", List.of(group(List.of("XYZ"))));
        world.capture("minecraft:chunk_generation_stats", List.of(line("Generation")));
        world.capture("minecraft:entity_spawn_counts", List.of(line("Spawns")));
        equal(world.column(LEFT, List.of("", "Help")), List.of("", "XYZ", "Generation", "Spawns", "", "Help"));
        ClassicLayout.Frame targets = new ClassicLayout.Frame();
        targets.capture("minecraft:looking_at_block", List.of(group(List.of()), group(List.of("Target", "#tag"))));
        targets.capture("minecraft:looking_at_fluid", List.of(group(List.of())));
        targets.capture("minecraft:memory", List.of(group(List.of("Mem"))));
        targets.capture("minecraft:system_specs", List.of(group(List.of("Java", "CPU"))));
        targets.capture("other:memory", List.of(line("Unknown")));
        equal(targets.column(RIGHT, List.of()), List.of("Unknown", "Mem", "", "Java", "CPU", "", "", "Target", "#tag", ""));
        ClassicLayout.Frame modern = new ClassicLayout.Frame();
        modern.capture("minecraft:looking_at_block_tags", List.of(group(List.of("#tag"))));
        modern.capture("minecraft:looking_at_block_state", List.of(group(List.of("Block"))));
        equal(modern.column(RIGHT, List.of()), List.of("", "Block", "#tag"));
        equal(new ClassicLayout.Frame().column(LEFT, List.of("", "Charts")), List.of("", "Charts"));
        ClassicLayout.Frame unknown = new ClassicLayout.Frame();
        unknown.capture("z:entry", List.of(line("first")));
        unknown.capture("a:entry", List.of(line("second")));
        equal(unknown.column(RIGHT, List.of()), List.of("first", "second"));
        System.out.println("Exact layout checks passed: 2048 row-for-row visibility fixtures plus empty/repeated groups, FPS merging, unknown entries, footer and modern tags.");
    }
}
