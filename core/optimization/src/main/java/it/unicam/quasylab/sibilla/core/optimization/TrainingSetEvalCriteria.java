package it.unicam.quasylab.sibilla.core.optimization;

import it.unicam.quasylab.sibilla.core.optimization.surrogate.TrainingSet;

@FunctionalInterface
public interface TrainingSetEvalCriteria {
    boolean eval(TrainingSet ts);
}