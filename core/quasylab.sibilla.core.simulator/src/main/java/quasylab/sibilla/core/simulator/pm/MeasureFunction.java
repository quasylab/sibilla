package quasylab.sibilla.core.simulator.pm;

import java.io.Serializable;

@FunctionalInterface
public interface MeasureFunction<S extends State> extends Serializable {
    public double apply(S state);
}
