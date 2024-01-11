package it.unicam.quasylab.sibilla.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    @Test
    void join() {
        Interval interval1 = new Interval(0, 10);
        Interval other1 = new Interval(5, 15);
        assertTrue(interval1.join(other1).isPresent());
        assertEquals(new Interval(0, 15), interval1.join(other1).get());

        Interval interval2 = new Interval(0, 5);
        Interval other2 = new Interval(5, 15);
        assertTrue(interval2.join(other2).isPresent());
        assertEquals(new Interval(0, 15), interval2.join(other2).get());

        Interval interval3 = new Interval(0, 5);
        Interval other3 = new Interval(6, 15);
        assertFalse(interval3.join(other3).isPresent());

        Interval interval4 = new Interval(6, 15);
        Interval other4 = new Interval(1, 6);
        assertTrue(interval4.join(other4).isPresent());
        assertEquals(new Interval(1, 15), interval4.join(other4).get());

    }

}