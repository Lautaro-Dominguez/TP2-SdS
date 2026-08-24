package sim.core;

import java.util.Random;

/**
 * Axis-aligned rectangle [0,width] x [0,height], open or periodic.
 *
 * A square (as used in this TP, L x L) is just the special case width == height - no separate
 * "SquareSpace" class is needed, which is exactly the kind of generalization the assignment
 * hinted at: the square in the statement is a particular instance of a more general shape.
 */
public final class RectangularSpace implements Space {

    private final double width;
    private final double height;
    private final boolean periodic;

    public RectangularSpace(double width, double height, boolean periodic) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.periodic = periodic;
    }

    public static RectangularSpace square(double l, boolean periodic) {
        return new RectangularSpace(l, l, periodic);
    }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    @Override
    public double measure() {
        return width * height;
    }

    @Override
    public double[] boundingBox() {
        return new double[] { 0, width, 0, height };
    }

    @Override
    public boolean isPeriodic() {
        return periodic;
    }

    @Override
    public boolean contains(Vector2D p) {
        return p.x >= 0 && p.x <= width && p.y >= 0 && p.y <= height;
    }

    @Override
    public Vector2D randomPoint(Random rng, double margin) {
        if (!periodic && (2 * margin >= width || 2 * margin >= height)) {
            throw new IllegalArgumentException(
                "margin " + margin + " does not fit inside a " + width + "x" + height + " space");
        }
        double lowX = periodic ? 0 : margin;
        double highX = periodic ? width : width - margin;
        double lowY = periodic ? 0 : margin;
        double highY = periodic ? height : height - margin;
        double x = lowX + rng.nextDouble() * (highX - lowX);
        double y = lowY + rng.nextDouble() * (highY - lowY);
        return new Vector2D(x, y);
    }

    @Override
    public double distance(Vector2D a, Vector2D b) {
        double dx = Math.abs(a.x - b.x);
        double dy = Math.abs(a.y - b.y);
        if (periodic) {
            dx = Math.min(dx, width - dx);
            dy = Math.min(dy, height - dy);
        }
        return Math.sqrt(dx * dx + dy * dy);
    }
}
