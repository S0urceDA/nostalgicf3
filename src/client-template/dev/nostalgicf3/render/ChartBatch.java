package dev.nostalgicf3.render;

import @PIPELINE@.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import @STATE@.ColoredRectangleRenderState;
import @STATE@.GuiElementRenderState;
import @STATE@.GuiRenderState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Only active synchronously inside vanilla debug-chart extraction. */
public final class ChartBatch {
    private static final ThreadLocal<ChartBatch> ACTIVE = new ThreadLocal<>();
    private final ChartBatch parent;
    private GuiRenderState owner;
    private final List<ColoredRectangleRenderState> rectangles = new ArrayList<>();
    private boolean flushing;

    private ChartBatch(ChartBatch parent) { this.parent = parent; }
    public static ChartBatch begin() {
        ChartBatch parent = ACTIVE.get();
        if (parent != null) parent.flush();
        ChartBatch batch = new ChartBatch(parent);
        ACTIVE.set(batch);
        return batch;
    }
    public void end() {
        try { flush(); }
        finally { if (parent == null) ACTIVE.remove(); else ACTIVE.set(parent); }
    }
    public static void barrier() {
        ChartBatch batch = ACTIVE.get();
        if (batch != null) batch.flush();
    }
    public static boolean capture(GuiRenderState owner, GuiElementRenderState element) {
        ChartBatch batch = ACTIVE.get();
        if (batch == null || batch.flushing) return false;
        if (!(element instanceof ColoredRectangleRenderState rectangle) || rectangle.bounds() == null) {
            batch.flush();
            return false;
        }
        if (!batch.rectangles.isEmpty()) {
            ColoredRectangleRenderState first = batch.rectangles.get(0);
            if (batch.owner != owner || first.pipeline() != rectangle.pipeline()
                    || !Objects.equals(first.textureSetup(), rectangle.textureSetup())
                    || !Objects.equals(first.scissorArea(), rectangle.scissorArea())) batch.flush();
        }
        batch.owner = owner;
        batch.rectangles.add(rectangle);
        return true;
    }
    private void flush() {
        if (flushing || rectangles.isEmpty()) return;
        flushing = true;
        try {
            GuiElementRenderState element = rectangles.size() == 1 ? rectangles.get(0)
                    : new Rectangles(rectangles);
            owner.@ADD_ELEMENT@(element);
        } finally {
            rectangles.clear();
            owner = null;
            flushing = false;
        }
    }
    public static final class Rectangles implements GuiElementRenderState {
        private final List<ColoredRectangleRenderState> rectangles;
        private final ScreenRectangle bounds;
        public Rectangles(List<ColoredRectangleRenderState> rectangles) {
            this.rectangles = List.copyOf(rectangles);
            int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
            for (ColoredRectangleRenderState rectangle : rectangles) {
                ScreenRectangle b = rectangle.bounds();
                left = Math.min(left, b.left()); top = Math.min(top, b.top());
                right = Math.max(right, b.right()); bottom = Math.max(bottom, b.bottom());
            }
            bounds = new ScreenRectangle(left, top, right - left, bottom - top);
        }
        public void buildVertices(VertexConsumer consumer) {
            for (ColoredRectangleRenderState rectangle : rectangles) rectangle.buildVertices(consumer);
        }
        public RenderPipeline pipeline() { return rectangles.get(0).pipeline(); }
        public TextureSetup textureSetup() { return rectangles.get(0).textureSetup(); }
        public ScreenRectangle scissorArea() { return rectangles.get(0).scissorArea(); }
        public ScreenRectangle bounds() { return bounds; }
    }
}

