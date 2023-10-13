package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteSetInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import org.junit.jupiter.api.Test;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_LOWER_BIGGER_THAN_UPPER;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Test for Interval class
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class IntervalTest {
    @Test
    void testLowerBoundSmallerThanUpperBound() {
        String id = "myId";
        double lowerBound = 5.5;
        double upperBound = 1.5;
        try {
            Interval i = new ContinuousInterval(id,lowerBound,upperBound);
        }catch (IllegalArgumentException ex){
            assertEquals(EXCEPT_LOWER_BIGGER_THAN_UPPER, ex.getMessage());
        }
    }

    @Test
    void testDifferentIdPerDifferentIntervals() {
        Interval i1 = new ContinuousInterval(2,4);
        Interval i2 = new ContinuousInterval(1,8);
        assertNotEquals(i1.getId(), i2.getId());
    }

    @Test
    void testConversionToDiscreteStep(){
        ContinuousInterval continuousInterval = new ContinuousInterval("contID",0,3);
        DiscreteSetInterval discreteSetInterval = new DiscreteSetInterval("setID",0,1,2,3);
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval("stepID",0,3,1);

        DiscreteStepInterval fromContinuous = continuousInterval.convertToDiscreteStep();
        DiscreteStepInterval fromSet = discreteSetInterval.convertToDiscreteStep(1);
        DiscreteStepInterval fromStep = discreteStepInterval.convertToDiscreteStep(1);
    }

    @Test
    void testConversionToDiscreteSet(){
        ContinuousInterval continuousInterval = new ContinuousInterval("contID",0,3);
        DiscreteSetInterval discreteSetInterval = new DiscreteSetInterval("setID",0,1,2,3);
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval("stepID",0,3,1);

        DiscreteSetInterval fromContinuous = continuousInterval.convertToDiscreteSet();
        DiscreteSetInterval fromSet = discreteSetInterval.convertToDiscreteSet();
        DiscreteSetInterval fromStep = discreteStepInterval.convertToDiscreteSet();
    }

    @Test
    void testConversionToContinuousStep(){
        ContinuousInterval continuousInterval = new ContinuousInterval("contID",0,3);
        DiscreteSetInterval discreteSetInterval = new DiscreteSetInterval("setID",0,1,2,3);
        DiscreteStepInterval discreteStepInterval = new DiscreteStepInterval("stepID",0,3,1);

        ContinuousInterval fromContinuous = continuousInterval.convertToContinuous();
        ContinuousInterval fromSet = discreteSetInterval.convertToContinuous();
        ContinuousInterval fromStep = discreteStepInterval.convertToContinuous();
    }

}