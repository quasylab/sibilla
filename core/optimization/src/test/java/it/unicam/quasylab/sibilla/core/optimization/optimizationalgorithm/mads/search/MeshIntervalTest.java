package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import org.junit.jupiter.api.Test;


class MeshIntervalTest {

    @Test
    void testListOfElements(){
        MeshInterval meshInterval = new MeshInterval("a",-1.0,13.0,3,4);
        meshInterval.getIntervalElements().forEach(System.out::println);
    }

    @Test
    void testRandomElements(){
        MeshInterval meshInterval = new MeshInterval("a",-1.0,13.0,3,4);
        meshInterval.getIntervalElements().forEach(System.out::println);
        for (int i = 0; i < 50; i++) {
            System.out.println(meshInterval.getRandomValue());
        }
    }

    @Test
    void testClosestValue(){
        MeshInterval meshInterval = new MeshInterval("a",-1.0,13.0,4.5,4);
        meshInterval.getIntervalElements().forEach(e -> System.out.println(e+" "));
        for (int i = -4; i < 17; i++) {
            System.out.println("value : " + i +" closest : "+meshInterval.getClosestValueTo(i));
        }
    }

}