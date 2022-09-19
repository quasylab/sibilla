package it.unicam.quasylab.sibilla.core.optimization.sampling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for HyperRectangle class
 *
 * @author      Lorenzo Matteucci
 */

@SuppressWarnings({"UnusedDeclaration"})
class HyperRectangleTest {

    @Test
    void testAllIDDsAreUniqueInAnHyperRectangle() {
        try {
            Interval i1 = new Interval("default",2,5);
            Interval i2 = new Interval("default",40,50);
            HyperRectangle hr = new HyperRectangle(i1,i2);
        }catch (IllegalArgumentException ex){
            String errorMessage = "there cannot be more intervals with the same identifier";
            assertEquals(errorMessage, ex.getMessage());
        }
    }

    @Test
    void testScalingHyperRectangle() {
        HyperRectangle hr = new HyperRectangle(
                new Interval(-10,10),
                new Interval(-20,20),
                new Interval(-2,2)
        );
        HyperRectangle hrScaled = hr.getScaledCopy(0.5);
        System.out.println(hr);
        System.out.println(hrScaled);
        assertTrue(hr.equals(hrScaled));
    }

}