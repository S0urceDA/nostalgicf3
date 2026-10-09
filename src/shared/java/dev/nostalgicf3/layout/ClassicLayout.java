package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fixed entry placement and per-output spacing matching OptiFine J9. */
public final class ClassicLayout {
    public enum Side { LEFT, RIGHT }
    public record Event(boolean group, List<String> lines) {
        public Event { lines = List.copyOf(lines); }
        public static Event line(String text) { return new Event(false, List.of(text)); }
        public static Event group(List<String> lines) { return new Event(true, lines); }
    }
    private record Placement(Side side, int rank) {}
    private static final Map<String, Placement> PLACEMENTS = new HashMap<>();
    private static final Set<String> NEW_LINE = Set.of("minecraft:player_position", "minecraft:system_specs",
            "minecraft:looking_at_block", "minecraft:looking_at_fluid", "minecraft:looking_at_entity",
            "minecraft:looking_at_block_state", "minecraft:looking_at_fluid_state",
            "minecraft:looking_at_block_tags", "minecraft:looking_at_fluid_tags");
    private static final Set<String> PERFORMANCE = Set.of("minecraft:fps",
            "minecraft:simple_performance_impactors", "minecraft:gpu_utilization");
    static {
        register(Side.LEFT, "game_version", "fps", "simple_performance_impactors", "gpu_utilization", "tps",
                "chunk_render_stats", "entity_render_stats", "particle_render_stats", "chunk_source_stats", "optifine",
                "player_position", "player_section_position", "player_speed", "light_levels", "heightmap", "biome",
                "local_difficulty", "chunk_generation_stats", "entity_spawn_counts", "sound_mood",
                "post_effect", "post_effects");
        register(Side.RIGHT, "memory", "system_specs",
                "looking_at_block", "looking_at_block_state", "looking_at_block_tags",
                "looking_at_fluid", "looking_at_fluid_state", "looking_at_fluid_tags",
                "looking_at_entity");
        register(Side.RIGHT, "detailed_memory", "looking_at_entity_tags", "day_count", "sound_cache");
        register(Side.RIGHT, "entity_hitboxes", "chunk_borders", "3d_crosshair", "chunk_section_paths",
                "chunk_section_octree", "visualize_water_levels", "visualize_heightmap", "visualize_collision_boxes",
                "visualize_entity_supporting_blocks", "visualize_block_light_levels", "visualize_sky_light_levels",
                "visualize_solid_faces", "visualize_chunks_on_server", "visualize_sky_light_sections", "chunk_section_visibility");
    }
    private static void register(Side side, String... paths) {
        for (String path : paths) PLACEMENTS.put("minecraft:" + path, new Placement(side, PLACEMENTS.size()));
    }
    private static Placement placement(String id) {
        return PLACEMENTS.getOrDefault(id, new Placement(Side.RIGHT, -1));
    }
    public static Side sideOf(String id) { return placement(id).side(); }
    public static int compareIds(String first, String second) {
        return Integer.compare(placement(first).rank(), placement(second).rank());
    }

    public static final class Frame {
        private final Map<String, List<Event>> entries = new LinkedHashMap<>();
        public void capture(String id, List<Event> events) {
            entries.computeIfAbsent(id, ignored -> new ArrayList<>()).addAll(events);
        }
        public List<String> column(Side side, List<String> vanillaFooter) {
            List<String> result = new ArrayList<>();
            List<String> ids = entries.keySet().stream().filter(id -> sideOf(id) == side)
                    .sorted(ClassicLayout::compareIds)
                    .toList();
            int fpsIndex = -1;
            for (String id : ids) {
                for (Event event : entries.get(id)) {
                    if (event.group()) {
                        // J9 inserts a row on every marked group call, including empty groups/columns.
                        if (NEW_LINE.contains(id)) result.add("");
                        result.addAll(event.lines());
                    } else {
                        for (String line : event.lines()) {
                            if (PERFORMANCE.contains(id)) {
                                if (fpsIndex >= 0) {
                                    result.set(fpsIndex, result.get(fpsIndex) + " " + line);
                                    continue;
                                }
                                fpsIndex = result.size();
                            }
                            result.add(line);
                        }
                    }
                }
            }
            result.addAll(vanillaFooter);
            return result;
        }
    }
    private ClassicLayout() {}
}
