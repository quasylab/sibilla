package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import tech.tablesaw.api.Table;

/**
 * A SamplingMethod object represents a sampling methods used to generate
 * sets of points in a certain input space.
 *
 * @author      Lorenzo Matteucci
 */
public interface SamplingTask {
    /**
     * methods used to generate sets of points in a hyper-rectangle, it could be used
     * either for training or for prediction.
     *
     * @param  numberOfSamples  number that determines the quantity of samples
     * @param  hyperRectangle an n-dimensional rectangle representing a portion of the space through intervals,
     *                        could be interpreted as the design space.
     * @return the samples' table of type <a href=https://github.com/jtablesaw/tablesaw">Table</a>
     * @see    <a href=https://jtablesaw.github.io/tablesaw/gettingstarted.html">tablesaw</a>
     */
     Table getSampleTable(int numberOfSamples, HyperRectangle hyperRectangle );

     Table getSampleTable(int numberOfSamplesPerDimension, HyperRectangle hyperRectangle, long seed);
}

