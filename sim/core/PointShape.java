package sim.core;

/**
 * A particle with no extent (effectiveRadius() == 0). Not used by this TP - it exists to show
 * that "punctual particle" doesn't need to be a special case anywhere: it's just CircleShape(0),
 * or, for clarity elsewhere in the code, this singleton.
 */
public final class PointShape implements Shape {

    public static final PointShape INSTANCE = new PointShape();

    private PointShape() {}

    @Override
    public double effectiveRadius() {
        return 0.0;
    }
}
