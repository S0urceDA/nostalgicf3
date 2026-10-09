package dev.nostalgicf3.render;

import dev.nostalgicf3.layout.TextWidthCache;
import net.minecraft.client.gui.Font;
import java.util.HashMap;
import java.util.Map;

/** Stores prepared glyphs only for string draws issued by F3's line renderer. */
public final class PreparedTextCache {
    private static final ThreadLocal<Entry> CURRENT = new ThreadLocal<>();
    private final Map<String, Entry> entries = new HashMap<>();
    private long generation = -1;
    public Entry select(String text) {
        long current = TextWidthCache.fontGeneration();
        if (current != generation) { entries.clear(); generation = current; }
        // Obfuscated formatting deliberately chooses new glyphs on each preparation.
        if (text.contains("\u00a7k") || text.contains("\u00a7K")) return null;
        Entry entry = entries.get(text);
        if (entry == null) {
            if (entries.size() >= 512) entries.clear();
            entry = new Entry(); entries.put(text, entry);
        }
        return entry;
    }
    public static Entry current() { return CURRENT.get(); }
    public static void setCurrent(Entry entry) {
        if (entry == null) CURRENT.remove(); else CURRENT.set(entry);
    }
    public static final class Entry {
        private Font font;
        private float x, y;
        private int color, background;
        private boolean shadow, empty;
        private long generation = -1;
        private Font.PreparedText prepared;
        public Font.PreparedText get(Font font, float x, float y, int color, boolean shadow,
                boolean empty, int background) {
            return this.font == font && this.x == x && this.y == y && this.color == color
                    && this.shadow == shadow && this.empty == empty && this.background == background
                    && generation == TextWidthCache.fontGeneration() ? prepared : null;
        }
        public void put(Font font, float x, float y, int color, boolean shadow,
                boolean empty, int background, Font.PreparedText prepared) {
            this.font = font; this.x = x; this.y = y; this.color = color;
            this.shadow = shadow; this.empty = empty; this.background = background;
            this.generation = TextWidthCache.fontGeneration(); this.prepared = prepared;
        }
    }
}
