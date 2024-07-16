package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.AbstractMADSTask;

public class LTPollMaximalPositiveBasis extends AbstractPollMethod {
    @Override
    protected int[][] getPositiveBasis(AbstractMADSTask madsTaskInstance) {
        return new LTDirection(madsTaskInstance.getRandomInstance()).getMaximalPositiveBasis(madsTaskInstance.getDimensionality(),madsTaskInstance.getDeltaPoll());
    }


//    @Override
//    protected int[][] getPositiveBasis(int dimension, double deltaMesh, Random random) {
//        return new LTDirection(random).getMaximalPositiveBasis(dimension,deltaMesh);
//    }
}
