package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh.Mesh;

import java.util.List;
import java.util.Map;
import java.util.Random;
@SuppressWarnings("unused")
public interface SearchMethod {
    List<Map<String,Double>> generateTrialPoints(int numberOfPoints, Mesh mesh, Random random);

    default List<Map<String,Double>> generateTrialPoints(int numberOfPoints, Mesh mesh){
        return generateTrialPoints(numberOfPoints, mesh);
    }
}
