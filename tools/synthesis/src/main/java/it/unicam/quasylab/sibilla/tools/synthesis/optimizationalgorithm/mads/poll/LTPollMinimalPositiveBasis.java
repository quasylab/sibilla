package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.AbstractMADSTask;

public class LTPollMinimalPositiveBasis extends AbstractPollMethod {
    @Override
    protected int[][] getPositiveBasis(AbstractMADSTask madsTaskInstance) {
        return new LTDirection(madsTaskInstance.getRandomInstance()).getMinimalPositiveBasis(madsTaskInstance.getDimensionality(),madsTaskInstance.getDeltaPoll());
    }
}
