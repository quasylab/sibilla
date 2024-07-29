package it.unicam.quasylab.sibilla.tools.synthesis.sampling;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.AbstractDiscreteInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.DiscreteSetInterval;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

class DiscreteSetIntervalTest {

    double[] arrayOfElements = {10.1,0.5,-1.3,-5.5,8.0,8.0};
    List<Double> listOfElements = DoubleStream.of(arrayOfElements).boxed().collect(Collectors.toList());
    DiscreteSetInterval discreteSetinterval = new DiscreteSetInterval(arrayOfElements);

    @Test
    void discreteSetInterval(){
        assertNoRepetitionAndSortedInterval(discreteSetinterval,5);
    }

    @Test
    void addElementSetInterval(){
        discreteSetinterval.addElements(4.3,2.1,2,2,5);
        assertNoRepetitionAndSortedInterval(discreteSetinterval,9);
    }

    @Test
    void assertRandomValueInTheInterval(){
        assertTrue(listOfElements.contains(discreteSetinterval.getRandomValue()));
    }

    @Test
    void testMethodGetIntervalValueClosestTo(){
        assertEquals(discreteSetinterval.getClosestValueTo(11),10.1);
        assertEquals(discreteSetinterval.getClosestValueTo(-7),-5.5);
        assertEquals(discreteSetinterval.getClosestValueTo(9),8.0);
    }

    @Test
    void testMethodIsDiscrete(){
        assertTrue(discreteSetinterval.isDiscrete());
        assertFalse(discreteSetinterval.isContinuous());
    }
    void assertNoRepetitionAndSortedInterval(AbstractDiscreteInterval i, int actualSize){
        assertEquals(i.size(),actualSize);
        assertEquals(i.getIntervalElements().stream().sorted().toList(), i.getIntervalElements());
    }

}