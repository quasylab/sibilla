package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.Mesh;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;

import java.util.*;

public class LHSSearchMethod implements SearchMethod{
    @Override
    public List<Map<String, Double>> generateTrialPoints(int numberOfPoints, Mesh mesh) {
       return new LatinHyperCubeSamplingTask().getSamplesAsMap(numberOfPoints,mesh.getMeshAsHyperRectangle());
    }

}
