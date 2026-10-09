package dev.nostalgicf3.layout;

public final class TextWidthCacheTest {
    public static void main(String[] args) {
        TextWidthCache cache = new TextWidthCache();
        Object font = new Object();
        expect(cache.get(font, "CPU"), -1);
        cache.put("CPU", 30);
        expect(cache.get(font, new String("CPU")), 30);
        cache.put("", 0);
        expect(cache.get(font, ""), 0);
        int measurements = 0;
        for (int frame = 0; frame < 100; frame++) {
            for (int pass = 0; pass < 2; pass++) {
                if (cache.get(font, "Unchanged line") < 0) {
                    measurements++;
                    cache.put("Unchanged line", 80);
                }
            }
        }
        expect(measurements, 1);
        TextWidthCache.invalidateFonts();
        expect(cache.get(font, "CPU"), -1);
        cache.put("CPU", 31);
        Object replacement = new Object();
        expect(cache.get(replacement, "CPU"), -1);
        for (int i = 0; i < 513; i++) cache.put("Position " + i, i);
        expect(cache.get(replacement, "Position 0"), -1);
        System.out.println("Text width cache: equal strings, zero width, font reload, font replacement and bounded retention passed");
    }
    private static void expect(int actual, int expected) {
        if (actual != expected) throw new AssertionError(actual + " != " + expected);
    }
}

