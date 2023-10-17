package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import org.junit.jupiter.api.Test;

import java.util.*;


public class PollTest {

    @Test
    void testMinimalBasis(){
        LTPollMinimalPositiveBasis minimalPositiveBasis = new LTPollMinimalPositiveBasis();
        Map<String,Double> point = new HashMap<>();
        point.put("x",5.0);
        point.put("y",5.0);
        point.put("z",5.0);
        List<Map<String,Double>> polledPoints = minimalPositiveBasis.getPolledPoints(point,0.5);
        System.out.println(polledPoints);

    }

    @Test
    void testMaximalBasis(){
        LTPollMaximalPositiveBasis minimalPositiveBasis = new LTPollMaximalPositiveBasis();
        Map<String,Double> point = new HashMap<>();
        point.put("x",5.0);
        point.put("y",5.0);
        point.put("z",5.0);
        List<Map<String,Double>> polledPoints = minimalPositiveBasis.getPolledPoints(point,1.0);
        System.out.println(polledPoints);
    }



}
