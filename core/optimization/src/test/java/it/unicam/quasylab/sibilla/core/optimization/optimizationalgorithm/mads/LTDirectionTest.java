package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll.LTDirection;
import org.junit.jupiter.api.Test;

import static it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.Common.*;

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
        System.out.println(desiredDet);
        int det = determinant(basis);
        System.out.println(det);
    }

    @Test
    void testMinimalBasis(){
        LTDirection ltDirection = new LTDirection();
        int[][] minPositiveBasis = ltDirection.getMinimalPositiveBasis(5,1.0/16.0);
        print2D(minPositiveBasis);
    }

    @Test
    void testMaximalBasis(){
        LTDirection ltDirection = new LTDirection();
        int[][] maxPositiveBasis = ltDirection.getMaximalPositiveBasis(5,1.0/128.0);
        print2D(maxPositiveBasis);
    }



}