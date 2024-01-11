package it.unicam.quasylab.sibilla.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignalTest {

    @Test
    void testTruncation() {
        Signal s = new Signal();
        s.add(0.0,1);
        s.add(18,2);
        s.add(49,3);
        s.add(95,4);
        s.add(112,5);
        Signal ts = s.truncate(100);
        assertEquals(100, ts.getEnd());
        assertEquals(ts.valueAt(ts.getEnd()),4.0);
    }
}