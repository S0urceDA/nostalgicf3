package dev.nostalgicf3.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

/** Fixed classic placement. Metadata is built once; frame buffers are reused. */
public final class ClassicLayout {
    public enum Side { LEFT, RIGHT }
    private static final int SEPARATED = 1, PERFORMANCE = 2, MEMORY = 4, DIFFICULTY = 8, DAY = 16;
    private record Placement(int rank, int flags) {}
    private static final Map<String, Placement> PLACEMENTS = new LinkedHashMap<>();
    private static final int RIGHT_START;
    private static final int[] FLAGS;
    static {
        register("game_version", "fps", "simple_performance_impactors", "gpu_utilization", "tps",
                "chunk_render_stats", "entity_render_stats", "particle_render_stats", "chunk_source_stats", "optifine",
                "player_position", "player_section_position", "player_speed", "light_levels", "heightmap", "biome",
                "local_difficulty", "day_count", "chunk_generation_stats", "entity_spawn_counts", "sound_mood",
                "sound_cache", "post_effect", "post_effects");
        RIGHT_START = PLACEMENTS.size();
        register("memory", "detailed_memory", "system_specs",
                "looking_at_block", "looking_at_block_state", "looking_at_block_tags",
                "looking_at_fluid", "looking_at_fluid_state", "looking_at_fluid_tags",
                "looking_at_entity", "looking_at_entity_tags");
        FLAGS = new int[PLACEMENTS.size()];
        for (Placement placement : PLACEMENTS.values()) FLAGS[placement.rank()] = placement.flags();
    }
    private static void register(String... paths) {
        for (String path : paths) {
            int flags = switch (path) {
                case "player_position", "looking_at_block", "looking_at_block_state", "looking_at_fluid",
                        "looking_at_fluid_state", "looking_at_entity" -> SEPARATED;
                case "fps", "simple_performance_impactors", "gpu_utilization" -> PERFORMANCE;
                case "memory", "detailed_memory" -> MEMORY;
                case "local_difficulty" -> DIFFICULTY;
                case "day_count" -> DAY;
                default -> 0;
            };
            PLACEMENTS.put("minecraft:" + path, new Placement(PLACEMENTS.size(), flags));
        }
    }
    /** Returns -1 for an unknown entry. Its output remains governed by vanilla's displayer API. */
    public static int indexOf(String id) {
        Placement placement = PLACEMENTS.get(id);
        return placement == null ? -1 : placement.rank();
    }
    public static boolean isKnown(String id) { return indexOf(id) >= 0; }
    public static Side sideOf(String id) {
        int index = indexOf(id);
        return index >= 0 && index < RIGHT_START ? Side.LEFT : Side.RIGHT;
    }
    public static int compareIds(String first, String second) {
        int a = indexOf(first), b = indexOf(second);
        return Integer.compare(a < 0 ? Integer.MAX_VALUE : a, b < 0 ? Integer.MAX_VALUE : b);
    }
    public static String relativePosition(String blockLine, int x, int y, int z) {
        return blockLine + " [" + (x & 15) + " " + (y & 15) + " " + (z & 15) + "]";
    }

    /** Owned by one overlay on the render thread; returned columns are valid until reset/reassembly. */
    public static final class Frame {
        @SuppressWarnings("unchecked")
        private final ArrayList<String>[] entries = (ArrayList<String>[]) new ArrayList<?>[FLAGS.length];
        private final ArrayList<String> left = new ArrayList<>(64), right = new ArrayList<>(64);
        private final StringBuilder performance = new StringBuilder(128);
        private final Map<Object, ArrayList<String>> unknownGroups = new HashMap<>();
        private final ArrayList<ArrayList<String>> groupBuffers = new ArrayList<>();
        private int groupCount;
        private boolean dirty;

        public void reset() {
            // After the first hidden frame clears old text, later hidden frames have nothing to reset.
            if (!dirty) return;
            dirty = false;
            for (ArrayList<String> entry : entries) if (entry != null) entry.clear();
            left.clear();
            right.clear();
            performance.setLength(0);
            unknownGroups.clear();
            for (int i = 0; i < groupCount; i++) groupBuffers.get(i).clear();
            groupCount = 0;
        }
        private ArrayList<String> entryBuffer(int index) {
            dirty = true;
            ArrayList<String> buffer = entries[index];
            if (buffer == null) entries[index] = buffer = new ArrayList<>(8);
            return buffer;
        }
        public void addLine(int index, String line) { entryBuffer(index).add(line); }
        public void addLines(int index, Collection<String> lines) {
            if (!lines.isEmpty()) append(entryBuffer(index), lines);
        }
        private ArrayList<String> groupBuffer(Object group) {
            dirty = true;
            ArrayList<String> buffer = unknownGroups.get(group);
            if (buffer == null) {
                if (groupCount == groupBuffers.size()) groupBuffers.add(new ArrayList<>(8));
                buffer = groupBuffers.get(groupCount++);
                unknownGroups.put(group, buffer);
            }
            return buffer;
        }
        public void captureUnknownGroup(Object group, Collection<String> lines) { append(groupBuffer(group), lines); }
        public void captureUnknownGroup(Object group, String line) { groupBuffer(group).add(line); }

        public List<String> column(Side side, List<String> vanillaRemainder) {
            dirty = true;
            ArrayList<String> result = side == Side.LEFT ? left : right;
            result.clear();
            performance.setLength(0);
            int fpsIndex = -1, difficultyIndex = -1;
            boolean memoryBlock = false;
            int start = side == Side.LEFT ? 0 : RIGHT_START;
            int end = side == Side.LEFT ? RIGHT_START : FLAGS.length;
            for (int index = start; index < end; index++) {
                ArrayList<String> lines = entries[index];
                if (lines == null || lines.isEmpty()) continue;
                int flags = FLAGS[index];
                // Finalize FPS text before checking the next section's separator.
                if ((flags & PERFORMANCE) == 0 && fpsIndex >= 0) {
                    result.set(fpsIndex, performance.toString());
                    fpsIndex = -1;
                }
                boolean memory = (flags & MEMORY) != 0;
                if (memoryBlock && !memory) {
                    result.add("");
                    memoryBlock = false;
                }
                if ((flags & SEPARATED) != 0) separate(result, true);
                for (int row = 0; row < lines.size(); row++) {
                    String line = lines.get(row);
                    if ((flags & PERFORMANCE) != 0) {
                        if (fpsIndex < 0) {
                            fpsIndex = result.size();
                            result.add("");
                        } else {
                            stripTrailing(performance);
                            performance.append(' ');
                        }
                        int first = 0;
                        while (first < line.length()) {
                            int point = line.codePointAt(first);
                            if (!Character.isWhitespace(point)) break;
                            first += Character.charCount(point);
                        }
                        performance.append(line, first, line.length());
                        continue;
                    }
                    if ((flags & DAY) != 0 && difficultyIndex >= 0) {
                        result.set(difficultyIndex, result.get(difficultyIndex) + " (" + line + ")");
                        continue;
                    }
                    if ((flags & DIFFICULTY) != 0) difficultyIndex = result.size();
                    result.add(line);
                }
                memoryBlock |= memory;
            }
            if (fpsIndex >= 0) result.set(fpsIndex, performance.toString());
            if (side == Side.RIGHT && !vanillaRemainder.isEmpty() && !vanillaRemainder.get(0).isEmpty()) {
                separate(result, false);
            }
            append(result, vanillaRemainder);
            if (side == Side.RIGHT) {
                for (int i = 0; i < groupCount; i++) {
                    ArrayList<String> group = groupBuffers.get(i);
                    if (group.isEmpty()) continue;
                    separate(result, false);
                    append(result, group);
                    result.add("");
                }
            }
            return result;
        }
        private static void separate(ArrayList<String> rows, boolean allowLeading) {
            if (rows.isEmpty() ? allowLeading : !rows.get(rows.size() - 1).isEmpty()) rows.add("");
        }
        private static void append(ArrayList<String> target, Collection<String> source) {
            // ArrayList.addAll calls toArray, creating a temporary array even when buffers are reused.
            if (source instanceof List<String> list && source instanceof RandomAccess) {
                for (int i = 0, size = list.size(); i < size; i++) target.add(list.get(i));
            } else {
                for (String line : source) target.add(line);
            }
        }
        private static void stripTrailing(StringBuilder text) {
            int end = text.length();
            while (end > 0) {
                int point = Character.codePointBefore(text, end);
                if (!Character.isWhitespace(point)) break;
                end -= Character.charCount(point);
            }
            text.setLength(end);
        }
    }
    private ClassicLayout() {}
}
