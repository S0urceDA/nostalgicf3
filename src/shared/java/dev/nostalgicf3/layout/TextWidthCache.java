package dev.nostalgicf3.layout;

import java.util.HashMap;
import java.util.Map;

/** Bounded F3-only cache. Font changes invalidate every overlay lazily. */
public final class TextWidthCache {
    private static long fontGeneration;
    private static final int LIMIT = 512;
    private final Map<String, Integer> widths = new HashMap<>();
    private Object font;
    private long generation = -1;

    public static void invalidateFonts() { fontGeneration++; }

    public int get(Object currentFont, String text) {
        if (font != currentFont || generation != fontGeneration) {
            widths.clear();
            font = currentFont;
            generation = fontGeneration;
        }
        Integer width = widths.get(text);
        return width == null ? -1 : width;
    }

    public void put(String text, int width) {
        if (widths.size() >= LIMIT) widths.clear();
        widths.put(text, width);
    }
}
