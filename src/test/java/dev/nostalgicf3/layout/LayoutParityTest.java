package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Differential fixtures against the shipped 0.1.6 policy, including reuse across changing frames. */
public final class LayoutParityTest {
    static final List<String> IDS = List.of("game_version", "fps", "simple_performance_impactors", "gpu_utilization",
            "tps", "chunk_render_stats", "entity_render_stats", "particle_render_stats", "chunk_source_stats", "optifine",
            "player_position", "player_section_position", "player_speed", "light_levels", "heightmap", "biome",
            "local_difficulty", "day_count", "chunk_generation_stats", "entity_spawn_counts", "sound_mood", "sound_cache",
            "post_effect", "post_effects", "memory", "detailed_memory", "system_specs", "looking_at_block",
            "looking_at_block_state", "looking_at_block_tags", "looking_at_fluid", "looking_at_fluid_state",
            "looking_at_fluid_tags", "looking_at_entity", "looking_at_entity_tags");
    private static final String[] TEXT = {"", "Row", " trailing ", "  leading", "\t\u2003spaces\u2003\t", "\u00a0nonbreaking\u00a0", "\uD83D\uDE00"};
    private static void equal(Object actual, Object expected, int frame) {
        if (!actual.equals(expected)) throw new AssertionError("Frame " + frame + ": expected " + expected + " but got " + actual);
    }
    public static void main(String[] args) {
        Random random = new Random(1661);
        ClassicLayout.Frame optimized = new ClassicLayout.Frame();
        for (int frame = 0; frame < 12000; frame++) {
            optimized.reset();
            ReferenceLayout.Frame reference = new ReferenceLayout.Frame();
            List<String> order = new ArrayList<>(IDS);
            Collections.shuffle(order, random);
            for (String path : order) {
                if (random.nextBoolean()) continue;
                String id = "minecraft:" + path;
                for (int call = random.nextInt(4); call >= 0; call--) {
                    List<String> lines = new ArrayList<>();
                    for (int count = random.nextInt(4); count > 0; count--) lines.add(TEXT[random.nextInt(TEXT.length)]);
                    boolean group = random.nextBoolean();
                    reference.capture(id, List.of(new ReferenceLayout.Event(group, lines)));
                    if (!group && lines.size() == 1) optimized.addLine(ClassicLayout.indexOf(id), lines.get(0));
                    else optimized.addLines(ClassicLayout.indexOf(id), lines);
                }
            }
            for (int count = random.nextInt(10); count > 0; count--) {
                String key = "mod:group" + random.nextInt(4);
                List<String> lines = random.nextBoolean() ? List.of() : List.of(TEXT[random.nextInt(TEXT.length)]);
                reference.captureUnknownGroup(key, lines);
                optimized.captureUnknownGroup(key, lines);
            }
            List<String> remainder = switch (random.nextInt(4)) {
                case 0 -> List.of();
                case 1 -> List.of("", "Debug charts: hints");
                case 2 -> List.of("Mod version", "");
                default -> List.of("", "Mod version", "", "More");
            };
            List<String> left = optimized.column(ClassicLayout.Side.LEFT, remainder);
            List<String> right = optimized.column(ClassicLayout.Side.RIGHT, remainder);
            equal(left, reference.column(ReferenceLayout.Side.LEFT, remainder), frame);
            equal(right, reference.column(ReferenceLayout.Side.RIGHT, remainder), frame);
            // Reassembling either side must neither append twice nor change the other side.
            equal(optimized.column(ClassicLayout.Side.RIGHT, remainder), reference.column(ReferenceLayout.Side.RIGHT, remainder), frame);
            equal(left, reference.column(ReferenceLayout.Side.LEFT, remainder), frame);
        }
        optimized.reset();
        equal(optimized.column(ClassicLayout.Side.LEFT, List.of()), List.of(), 12000);
        equal(optimized.column(ClassicLayout.Side.RIGHT, List.of()), List.of(), 12000);
        // Empty-first groups preserve insertion order when a later call fills them.
        optimized.captureUnknownGroup("first", List.of());
        optimized.captureUnknownGroup("second", "Second");
        optimized.captureUnknownGroup("first", "First");
        equal(optimized.column(ClassicLayout.Side.RIGHT, List.of()), List.of("First", "", "Second", ""), 12001);
        System.out.println("Parity checks passed: 12000 randomized full-column comparisons with 0.1.6, reset, Unicode whitespace and mod-group order.");
    }
}
