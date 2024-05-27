package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import java.util.Random;

public class LTPollMinimalPositiveBasis extends AbstractPollMethod{
    @Override
    protected int[][] getPositiveBasis(int dimension, double deltaMesh,Random random) {
        return new LTDirection(random).getMinimalPositiveBasis(dimension,deltaMesh);
    }
}
