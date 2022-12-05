package it.unicam.quasylab.sibilla.core.optimization.sampling;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

class DiscreteSetIntervalTest {

    double[] arrayOfElements = {10.1,0.5,-1.3,-5.5,8.0,8.0};
    List<Double> listOfElements = DoubleStream.of(arrayOfElements).boxed().collect(Collectors.toList());


    @Test
    void discreteSetInterval(){
        DiscreteSetInterval dsi = new DiscreteSetInterval(arrayOfElements);
        assertNoRepetitionAndSortedInterval(dsi,5);
    }

    @Test
    void addElementSetInterval(){
        DiscreteSetInterval dsi = new DiscreteSetInterval(arrayOfElements);
        dsi.addElements(4.3,2.1,2,2,5);
        assertNoRepetitionAndSortedInterval(dsi,9);
    }

    @Test
    void assertRandomValueInTheInterval(){
        DiscreteSetInterval dsi = new DiscreteSetInterval(arrayOfElements);
        assertTrue(listOfElements.contains(dsi.getRandomValue()));
    }


    void assertNoRepetitionAndSortedInterval(AbstractDiscreteInterval i, int actualSize){
        assertEquals(i.size(),actualSize);
        assertEquals(i.getSequenceOfElement().stream().sorted().toList(), i.sequenceOfElement);
    }



}