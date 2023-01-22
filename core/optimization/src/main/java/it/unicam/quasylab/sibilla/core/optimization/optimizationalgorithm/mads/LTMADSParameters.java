package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;
@SuppressWarnings("all")
//TODO
public record LTMADSParameters(int iteration,
                               double stoppingTolerance,
                               double[][] G,
                               double[][] D,
                               double tau,
                               double wMinus ,
                               double wPlus ,
                               double[][] deltaMesh,
                               double[][] deltaPoll
) {

}
