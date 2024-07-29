package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LTDirectionTest {

    @Test
    void testBasis(){

        LTDirection ltDirection = new LTDirection();
        int[][] basis = ltDirection.getBasis(2,4.0);
    }
}