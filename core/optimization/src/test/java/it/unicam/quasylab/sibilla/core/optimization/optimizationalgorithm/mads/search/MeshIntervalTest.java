package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh.MeshIntervalBD;
import org.junit.jupiter.api.Test;


class MeshIntervalTest {

    @Test
    void testListOfElements(){
        MeshIntervalBD meshInterval = new MeshIntervalBD("a",-1.0,13.0,3,4);
        meshInterval.getIntervalElements().forEach(System.out::println);

        MeshIntervalBD meshIntervalCenter5 = new MeshIntervalBD("a",-1.0,13.0,3,5);
        meshIntervalCenter5.getIntervalElements().forEach(System.out::println);
    }

    @Test
    void testRandomElements(){
        MeshIntervalBD meshInterval = new MeshIntervalBD("a",-1.0,13.0,3,4);
        meshInterval.setSeed(123L);
        for (int i = 0; i < 50; i++) {
            System.out.println(meshInterval.getRandomValue());
        }
    }

    @Test
    void testClosestValue(){
        MeshIntervalBD meshInterval = new MeshIntervalBD("a",-1.0,13.0,4.5,4);
        meshInterval.getIntervalElements().forEach(e -> System.out.println(e+" "));
        for (int i = -4; i < 17; i++) {
            System.out.println("value : " + i +" closest : "+meshInterval.getClosestValueTo(i));
        }
    }

}