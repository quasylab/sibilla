package quasylab.sibilla.core.simulator.pm;

import java.io.Serializable;
import java.util.function.Function;

public interface MeasureFunction<S> extends Function<S, Double>, Serializable {

}
