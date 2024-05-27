package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import java.util.Random;

public class LTPollMaximalPositiveBasis extends AbstractPollMethod{
    @Override
    protected int[][] getPositiveBasis(int dimension, double deltaMesh, Random random) {
        return new LTDirection(random).getMaximalPositiveBasis(dimension,deltaMesh);
    }
}
