package it.unicam.quasylab.sibilla.core.optimization.surrogate;
/**
 * a functional interface used to evaluate whether a certain training
 * set is to be considered valid: eval returns true if the Training Set
 * is valid
 *
 * @author      Lorenzo Matteucci
 */
@FunctionalInterface
public interface TrainingSetEvalCriteria {
    /**
     * Returns the evaluation of the training set, passed as a parameter,
     * according to the specified criterion
     *
     * @param  ts  a training set
     * @return      <code>true</code> If the training set is
     * evaluated positively
     */
    boolean eval(DataSet ts);
}