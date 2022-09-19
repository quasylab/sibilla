package it.unicam.quasylab.sibilla.core.optimization.sampling;

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
            Interval i = new Interval(id,lowerBound,upperBound);
        }catch (IllegalArgumentException ex){
            assertEquals(EXCEPT_LOWER_BIGGER_THAN_UPPER, ex.getMessage());
        }
    }

    @Test
    void testDifferentIdPerDifferentIntervals() {
        Interval i1 = new Interval(2,4);
        Interval i2 = new Interval(1,8);
        assertNotEquals(i1.getId(), i2.getId());
    }

}