package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh.Mesh;

import java.util.List;
import java.util.Map;

public interface SearchMethod {
    List<Map<String,Double>> generateTrialPoints(int numberOfPoints, Mesh mesh);
}
