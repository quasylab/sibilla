package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_INTERVALS_WITH_SAME_ID;
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
            Interval i1 = new ContinuousInterval("default",2,5);
            Interval i2 = new ContinuousInterval("default",40,50);
            HyperRectangle hr = new HyperRectangle(i1,i2);
        }catch (IllegalArgumentException ex){
            assertEquals(EXCEPT_INTERVALS_WITH_SAME_ID, ex.getMessage());
        }
    }

    @Test
    void testScalingHyperRectangle() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(-10,10),
                new ContinuousInterval(-20,20),
                new ContinuousInterval(-2,2)
        );
        HyperRectangle hrScaled = hr.getScaledCopy(0.5);
        assertNotEquals(hr, hrScaled);
    }

    @Test
    void testChangeCenter(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(-10,10),
                new ContinuousInterval(-20,20),
                new ContinuousInterval(-2,2)
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
                new ContinuousInterval("A",-10,10),
                new ContinuousInterval("B",-20,20),
                new ContinuousInterval("C",-2,2)
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
    void testRandomSampleWithSeed() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("A", -10, 10),
                new ContinuousInterval("B", -20, 20),
                new ContinuousInterval("C", -2, 2)
        );
        hr.setSeeds(123456789L);

        Map<String, Double> firstSample = hr.getRandomValue();
        assertEquals(2.853092964515211, firstSample.get("A"));
        assertEquals(-19.26242372770849, firstSample.get("B"));
        assertEquals(0.21556020540300702, firstSample.get("C"));

        Map<String, Double> secondSample = hr.getRandomValue();
        assertEquals(0.6211862671946555, secondSample.get("A"));
        assertEquals(16.009715588845374, secondSample.get("B"));
        assertEquals(0.29399274394423713, secondSample.get("C"));

        Map<String, Double> thirdSample = hr.getRandomValue();
        assertEquals(0.8616039812589662, thirdSample.get("A"));
        assertEquals(0.992132570205861, thirdSample.get("B"));
        assertEquals(-0.5715646080207919, thirdSample.get("C"));
    }

    @Test
    void testList(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("A", -10, 10),
                new ContinuousInterval("B", -20, 20),
                new ContinuousInterval("C", -2, 2)
        );
        List<String> ids = hr.getIdsList();

        assertEquals(3, ids.size());
        assertEquals("A", ids.get(0));
        assertEquals("B", ids.get(1));
        assertEquals("C", ids.get(2));
    }


}