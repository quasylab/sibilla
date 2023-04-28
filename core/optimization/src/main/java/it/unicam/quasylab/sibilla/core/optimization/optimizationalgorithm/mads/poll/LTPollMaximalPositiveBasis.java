package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

public class LTPollMaximalPositiveBasis extends AbstractPollMethod{

    private final LTDirection ltDirection;

    public LTPollMaximalPositiveBasis() {
        this.ltDirection = new LTDirection();
    }


    @Override
    protected int[][] getPositiveBasis(int dimension, double deltaMesh) {
        return this.ltDirection.getMaximalPositiveBasis(dimension,deltaMesh);
    }
}
