package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.mesh.MeshIntervalBD;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MeshIntervalTest {

    @Test
    void testRandomElements() {
        MeshIntervalBD meshInterval = new MeshIntervalBD("a", -1.0, 13.0, 3, 4);
        meshInterval.setSeed(123L);
        List<Double> expectedRandomValues = Arrays.asList(
                7.0, 1.0, 4.0, 13.0, 1.0, 7.0, 13.0, 7.0, 1.0, 10.0
        );

        for (int i = 0; i < 10; i++) {
            assertEquals(expectedRandomValues.get(i), meshInterval.getRandomValue(), 0.001);
        }
    }


    @Test
    void testClosestValue() {
        MeshIntervalBD meshInterval = new MeshIntervalBD("a", -1.0, 13.0, 4.5, 4);
        List<Double> expectedElements = Arrays.asList(-0.5, 4.0, 8.5, 13.0);
        assertEquals(expectedElements, meshInterval.getIntervalElements());

        int[] testValues = {-4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        double[] expectedClosestValues = {-0.5, -0.5, -0.5, -0.5, -0.5, -0.5, 4.0, 4.0, 4.0, 4.0, 4.0, 8.5, 8.5, 8.5, 8.5, 13.0, 13.0, 13.0, 13.0, 13.0, 13.0};

        for (int i = 0; i < testValues.length; i++) {
            assertEquals(expectedClosestValues[i], meshInterval.getClosestValueTo(testValues[i]), 0.001);
        }
    }

}