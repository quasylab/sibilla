package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.AbstractMADSTask;

public class OrthogonalPollMaximalPositiveBasis extends AbstractPollMethod{
    @Override
    protected int[][] getPositiveBasis(AbstractMADSTask madsTaskInstance) {
        return new OrthogonalDirection().generateOrthogonalBasis(madsTaskInstance.getDimensionality(),madsTaskInstance.getT(),madsTaskInstance.getL());
    }
}
