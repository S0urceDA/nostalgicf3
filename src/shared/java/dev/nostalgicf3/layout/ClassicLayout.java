package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fixed classic placement, with newer entries alongside related information. */
public final class ClassicLayout {
    public enum Side { LEFT, RIGHT }
    public record Event(boolean group, List<String> lines) {
        public Event { lines = List.copyOf(lines); }
        public static Event line(String text) { return new Event(false, List.of(text)); }
        public static Event group(List<String> lines) { return new Event(true, lines); }
    }
    private record Placement(Side side, int rank) {}
    private static final Map<String, Placement> PLACEMENTS = new HashMap<>();
    private static final Set<String> SEPARATED = Set.of("minecraft:player_position",
            "minecraft:looking_at_block", "minecraft:looking_at_block_state",
            "minecraft:looking_at_fluid", "minecraft:looking_at_fluid_state", "minecraft:looking_at_entity");
    private static final Set<String> PERFORMANCE = Set.of("minecraft:fps",
            "minecraft:simple_performance_impactors", "minecraft:gpu_utilization");
    static {
        register(Side.LEFT, "game_version", "fps", "simple_performance_impactors", "gpu_utilization", "tps",
                "chunk_render_stats", "entity_render_stats", "particle_render_stats", "chunk_source_stats", "optifine",
                "player_position", "player_section_position", "player_speed", "light_levels", "heightmap", "biome",
                "local_difficulty", "day_count", "chunk_generation_stats", "entity_spawn_counts", "sound_mood",
                "sound_cache", "post_effect", "post_effects");
        register(Side.RIGHT, "memory", "detailed_memory", "system_specs",
                "looking_at_block", "looking_at_block_state", "looking_at_block_tags",
                "looking_at_fluid", "looking_at_fluid_state", "looking_at_fluid_tags",
                "looking_at_entity", "looking_at_entity_tags");
    }
    private static void register(Side side, String... paths) {
        for (String path : paths) PLACEMENTS.put("minecraft:" + path, new Placement(side, PLACEMENTS.size()));
    }
    public static boolean isKnown(String id) { return PLACEMENTS.containsKey(id); }
    private static Placement placement(String id) {
        return PLACEMENTS.getOrDefault(id, new Placement(Side.RIGHT, Integer.MAX_VALUE));
    }
    public static Side sideOf(String id) { return placement(id).side(); }
    public static int compareIds(String first, String second) {
        return Integer.compare(placement(first).rank(), placement(second).rank());
    }
    public static String relativePosition(String blockLine, int x, int y, int z) {
        return blockLine + " [" + (x & 15) + " " + (y & 15) + " " + (z & 15) + "]";
    }

    public static final class Frame {
        private final Map<String, List<Event>> entries = new LinkedHashMap<>();
        private final Map<String, List<String>> unknownGroups = new LinkedHashMap<>();
        public void captureUnknownGroup(String group, List<String> lines) {
            unknownGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).addAll(lines);
        }
        public void capture(String id, List<Event> events) {
            entries.computeIfAbsent(id, ignored -> new ArrayList<>()).addAll(events);
        }
        public List<String> column(Side side, List<String> vanillaRemainder) {
            List<String> result = new ArrayList<>();
            List<String> ids = entries.keySet().stream().filter(id -> sideOf(id) == side)
                    .sorted(ClassicLayout::compareIds).toList();
            int fpsIndex = -1, difficultyIndex = -1;
            boolean memoryBlock = false;
            for (String id : ids) {
                List<String> lines = entries.get(id).stream().flatMap(event -> event.lines().stream()).toList();
                if (lines.isEmpty()) continue;
                boolean memory = id.equals("minecraft:memory") || id.equals("minecraft:detailed_memory");
                if (memoryBlock && !memory) {
                    result.add("");
                    memoryBlock = false;
                }
                if (SEPARATED.contains(id) && (result.isEmpty() || !result.get(result.size() - 1).isEmpty())) result.add("");
                for (String line : lines) {
                    if (PERFORMANCE.contains(id)) {
                        if (fpsIndex >= 0) {
                            result.set(fpsIndex, result.get(fpsIndex).stripTrailing() + " " + line.stripLeading());
                            continue;
                        }
                        fpsIndex = result.size();
                    }
                    if (id.equals("minecraft:day_count") && difficultyIndex >= 0) {
                        result.set(difficultyIndex, result.get(difficultyIndex) + " (" + line + ")");
                        continue;
                    }
                    if (id.equals("minecraft:local_difficulty")) difficultyIndex = result.size();
                    result.add(PERFORMANCE.contains(id) ? line.stripLeading() : line);
                }
                memoryBlock |= memory;
            }
            // Known rows are assembled separately; restore vanilla's boundary before right-side mod text.
            if (side == Side.RIGHT && !result.isEmpty() && !vanillaRemainder.isEmpty()
                    && !result.get(result.size() - 1).isEmpty() && !vanillaRemainder.get(0).isEmpty()) result.add("");
            result.addAll(vanillaRemainder);
            if (side == Side.RIGHT) {
                for (List<String> group : unknownGroups.values()) {
                    if (group.isEmpty()) continue;
                    if (!result.isEmpty() && !result.get(result.size() - 1).isEmpty()) result.add("");
                    result.addAll(group);
                    result.add("");
                }
            }
            return result;
        }
    }
    private ClassicLayout() {}
}
