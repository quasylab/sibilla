package it.unicam.quasylab.sibilla.core.optimization.surrogate;
/**
 * A functional interface used to re-arrange a Training Set a certain training set
 *
 * @author      Lorenzo Matteucci
 */
@FunctionalInterface
public interface TrainingSetRearranger {
    /**
     * Returns takes a training set and returns a rearranged one
     *
     * @param  ts  a training set
     * @return <code>TrainingSet</code> a re-arranged version of
     * the one passed as parameter
     */
    TrainingSet rearrange(TrainingSet ts);
}
