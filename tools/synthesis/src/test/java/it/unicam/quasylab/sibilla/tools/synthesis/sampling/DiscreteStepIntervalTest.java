package it.unicam.quasylab.sibilla.tools.synthesis.sampling;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.DiscreteStepInterval;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class DiscreteStepIntervalTest {

    @Test
    void testStartFromLowerOrFromUpper(){
        double lowerBound = -10;
        double upperBound = 10;
        double step = 1.8;
        DiscreteStepInterval discreteStepIntervalStartingLB = new DiscreteStepInterval(lowerBound,upperBound,step,true);
        DiscreteStepInterval discreteStepIntervalStartingUB = new DiscreteStepInterval(lowerBound,upperBound,step,false);

        assertEquals(discreteStepIntervalStartingLB.size(), discreteStepIntervalStartingUB.size());
        assertTrue(discreteStepIntervalStartingLB.contains(-10));
        assertTrue(discreteStepIntervalStartingUB.contains(10));
    }

    @Test
    void testRightSizeDiscreteInterval(){
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval(-10,10,1);
        assertEquals(21, discreteStepInterval.size());
        DiscreteStepInterval discreteStepInterval2 = new DiscreteStepInterval(-5,-3,1);
        assertEquals(3, discreteStepInterval2.size());
    }

    @Test
    void testChangeStepValue(){
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval(-10,10,1);
        discreteStepInterval.setStep(2);
        assertEquals(11, discreteStepInterval.size());
    }

    @Test
    void testScaleStep(){
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval(-10,10,1);
        discreteStepInterval.scaleStep(0.5);
        assertEquals(41, discreteStepInterval.size());
    }

    @Test
    void testUniformRandomValue(){
        DiscreteStepInterval dsi = new DiscreteStepInterval(-5,-2,0.75,true);
        TreeMap<Double,Integer> repetition = new TreeMap<>();
        for (int i = 0; i < 1000; i++) {
            double randomValue = dsi.getRandomValue();
            if(repetition.containsKey(randomValue))
                repetition.replace(randomValue,(repetition.get(randomValue)+1));
            else
                repetition.put(randomValue,1);
        }
        assertEquals(dsi.size(), repetition.keySet().size());
        for (Double key : repetition.keySet()) {
            assertTrue(repetition.get(key) >= 150 && repetition.get(key) <= 250);
        }
    }

    @Test
    void testGetClosestValueTo(){
        DiscreteStepInterval dsiStartLower = new DiscreteStepInterval(1,10,7.5,true);
        DiscreteStepInterval dsiStartUpper = new DiscreteStepInterval(1,10,7.5,false);
        assertEquals(dsiStartLower.getClosestValueTo(5.0),8.5);
        assertEquals(dsiStartUpper.getClosestValueTo(5.0),2.5);
    }



}