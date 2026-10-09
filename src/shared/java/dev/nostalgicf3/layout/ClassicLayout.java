package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Orders entry IDs, never their changing or localized text. */
public final class ClassicLayout {
    public enum Side { LEFT, RIGHT }
    private record Placement(Side side, int section, int rank) {}
    private record Output(String id, List<String> lines) {}
    private static final Map<String, Placement> PLACEMENTS = new HashMap<>();
    static {
        section(Side.LEFT, 0, "game_version", "fps", "simple_performance_impactors", "gpu_utilization", "tps",
                "chunk_render_stats", "entity_render_stats", "particle_render_stats", "chunk_source_stats");
        section(Side.LEFT, 1, "player_position", "player_section_position", "light_levels", "heightmap", "biome",
                "local_difficulty", "day_count", "chunk_generation_stats");
        section(Side.LEFT, 2, "entity_spawn_counts", "sound_mood", "post_effect");
        section(Side.RIGHT, 0, "java_version", "memory", "detailed_memory", "system_specs");
        section(Side.RIGHT, 1, "looking_at_block", "looking_at_block_state");
        section(Side.RIGHT, 2, "looking_at_fluid", "looking_at_fluid_state");
        section(Side.RIGHT, 3, "looking_at_entity");
    }

    private static void section(Side side, int section, String... paths) {
        for (int i = 0; i < paths.length; i++) {
            PLACEMENTS.put("minecraft:" + paths[i], new Placement(side, section, i));
        }
    }

    private static Placement placement(String id) {
        return PLACEMENTS.getOrDefault(id, new Placement(Side.LEFT, 3, Integer.MAX_VALUE));
    }

    public static Side sideOf(String id) { return placement(id).side(); }

    /** A fresh frame captures enabled entries only; it never changes visibility or evaluates entries. */
    public static final class Frame {
        private final Map<String, List<String>> entries = new LinkedHashMap<>();

        public void add(String id, List<String> lines) {
            if (!lines.isEmpty()) entries.computeIfAbsent(id, ignored -> new ArrayList<>()).addAll(lines);
        }

        public List<String> column(Side side, List<String> vanillaFooter) {
            List<Output> outputs = entries.entrySet().stream()
                    .filter(entry -> sideOf(entry.getKey()) == side)
                    .map(entry -> new Output(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparingInt((Output output) -> placement(output.id()).section())
                            .thenComparingInt(output -> placement(output.id()).rank()).thenComparing(Output::id))
                    .toList();
            List<String> result = new ArrayList<>();
            int previousSection = -1;
            int fpsIndex = -1;
            for (Output output : outputs) {
                List<String> lines = output.lines();
                if (lines.stream().allMatch(String::isEmpty)) continue;
                int section = placement(output.id()).section();
                if (previousSection != -1 && section != previousSection) separator(result);
                previousSection = section;
                if (isPerformance(output.id())) {
                    String text = String.join(" ", lines);
                    if (fpsIndex < 0) { fpsIndex = result.size(); result.add(text); }
                    else result.set(fpsIndex, result.get(fpsIndex) + " " + text);
                } else {
                    // Keep all grouped output intact; blank groups don't move any entries between columns.
                    result.addAll(lines);
                }
            }
            int first = 0;
            while (first < vanillaFooter.size() && vanillaFooter.get(first).isEmpty()) first++;
            if (first < vanillaFooter.size()) {
                separator(result);
                result.addAll(vanillaFooter.subList(first, vanillaFooter.size()));
            }
            while (!result.isEmpty() && result.getLast().isEmpty()) result.removeLast();
            return result;
        }

        private static boolean isPerformance(String id) {
            return id.equals("minecraft:fps") || id.equals("minecraft:simple_performance_impactors")
                    || id.equals("minecraft:gpu_utilization");
        }
        private static void separator(List<String> lines) {
            if (!lines.isEmpty() && !lines.getLast().isEmpty()) lines.add("");
        }
    }
    private ClassicLayout() {}
}
