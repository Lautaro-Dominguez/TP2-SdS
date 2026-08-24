package sim.core;

/**
 * A particle: an id, a position, a heading angle, a shape and a scalar "property" (the "pr"
 * column from the assignment's static file - a placeholder for whatever a future TP attaches to
 * a particle: charge, temperature, species, etc).
 */
public final class Particle {

    private final int id;
    private Vector2D position;
    private double angle;
    private final Shape shape;
    private final double property;

    public Particle(int id, Vector2D position, double angle, Shape shape, double property) {
        this.id = id;
        this.position = position;
        this.angle = angle;
        this.shape = shape;
        this.property = property;
    }

    public int getId() { return id; }

    public Vector2D getPosition() { return position; }

    public void setPosition(Vector2D position) { this.position = position; }

    public double getAngle() { return angle; }

    public void setAngle(double angle) { this.angle = angle; }

    public Shape getShape() { return shape; }

    public double getProperty() { return property; }

    /** Border-to-border distance to another particle, respecting the space's boundary condition. */
    public double borderDistanceTo(Particle other, Space space) {
        double centerDistance = space.distance(this.position, other.position);
        return centerDistance - this.shape.effectiveRadius() - other.shape.effectiveRadius();
    }
}
