package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import org.junit.jupiter.api.Test;
import smile.math.matrix.Matrix;
import static it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.Common.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@SuppressWarnings("all")
//TODO
class LTDirectionTest {

    @Test
    void testL(){
        LTDirection ltDirection = new LTDirection();
        int[][] l = ltDirection.getL(4,2);
        //System.out.println(Arrays.deepToString(l));
    }

    @Test
    void testVectorBl(){
        LTDirection ltDirection = new LTDirection();
        for (int i = 0; i < 2; i++) {
            var tuple =ltDirection.getBlVectorAndCapI(5,1.0/16.0);
            int[] blVec = tuple.blVector();
            int iHat = tuple.iHat();
            //System.out.println("example : " + i );
            //System.out.println(Arrays.toString(blVec));
            //System.out.println(iHat);
            //System.out.println("_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        }
    }

    @Test
    void testBasis(){
        LTDirection ltDirection = new LTDirection();
        int[][] basis = ltDirection.getBasis(5,1.0/16.0);
        int l = (int) ((-1)*log(1.0/16.0,4));
        double desiredDet = Math.pow(2,5*l);
        //System.out.println(desiredDet);
        //int det = determinantOfMatrix(basis,5);
        //System.out.println(det);
        //System.out.println(desiredDet);
    }

    @Test
    void testMinimalBasis(){
        LTDirection ltDirection = new LTDirection();
        int[][] minPositiveBasis = ltDirection.getMinimalPositiveBasis(5,1.0/16.0);
        //System.out.println("^_^_^_^_^_^_^_^_^_^_^_^");
        //print2D(minPositiveBasis);
    }

    @Test
    void testMaximalBasis(){
        LTDirection ltDirection = new LTDirection();
        int[][] minPositiveBasis = ltDirection.getMaximalPositiveBasis(5,1.0);
        //print2D(minPositiveBasis);
    }



}