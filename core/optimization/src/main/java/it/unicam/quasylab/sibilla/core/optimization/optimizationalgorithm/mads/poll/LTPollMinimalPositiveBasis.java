package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.AbstractMADSTask;

import java.util.Random;

public class LTPollMinimalPositiveBasis extends AbstractPollMethod{
    @Override
    protected int[][] getPositiveBasis(AbstractMADSTask madsTaskInstance) {
        return new LTDirection(madsTaskInstance.getRandomInstance()).getMinimalPositiveBasis(madsTaskInstance.getDimensionality(),madsTaskInstance.getDeltaPoll());
    }
}
