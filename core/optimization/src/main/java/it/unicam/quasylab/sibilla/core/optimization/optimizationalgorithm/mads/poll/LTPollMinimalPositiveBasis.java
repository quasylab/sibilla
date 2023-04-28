package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

public class LTPollMinimalPositiveBasis extends AbstractPollMethod{

    private final LTDirection ltDirection;

    public LTPollMinimalPositiveBasis() {
        this.ltDirection = new LTDirection();
    }
    @Override
    protected int[][] getPositiveBasis(int dimension, double deltaMesh) {
        return this.ltDirection.getMinimalPositiveBasis(dimension,deltaMesh);
    }
}
