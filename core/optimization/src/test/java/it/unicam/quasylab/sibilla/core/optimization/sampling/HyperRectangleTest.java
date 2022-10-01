package it.unicam.quasylab.sibilla.core.optimization.sampling;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
        assertNotEquals(hr, hrScaled);
    }

    @Test
    void testChangeCenter(){
        HyperRectangle hr = new HyperRectangle(
                new Interval(-10,10),
                new Interval(-20,20),
                new Interval(-2,2)
        );
        double[] aPoint = {-1.0,-11.0,1.0};
        assertTrue(hr.couldContain(aPoint));
        double[] newCenter = {10.0,20.0,2.0};
        hr.changeCenter(newCenter);
        assertFalse(hr.couldContain(aPoint));
    }

    @Test
    void testChangeCenterMap(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("A",-10,10),
                new Interval("B",-20,20),
                new Interval("C",-2,2)
        );

        Map<String,Double> aPoint = new HashMap<>();
        aPoint.put("A",-1.0);
        aPoint.put("B",-11.0);
        aPoint.put("C",1.0);

        assertTrue(hr.couldContain(aPoint));

        Map<String,Double> newCenter = new HashMap<>();
        newCenter.put("A",10.0);
        newCenter.put("B",20.0);
        newCenter.put("C",2.0);

        hr.changeCenter(newCenter);

        assertFalse(hr.couldContain(aPoint));
    }

    @Test
    void testCouldContain(){

    }

}