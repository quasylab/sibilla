package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.states;

import org.apache.commons.math3.random.AbstractRandomGenerator;

import java.util.SplittableRandom;

public class SplittableRandomGenerator extends AbstractRandomGenerator {

    private SplittableRandom sr;

    public SplittableRandomGenerator() {
        sr = new SplittableRandom();
    }

    public SplittableRandomGenerator(long seed) {
        sr = new SplittableRandom(seed);
    }

    @Override
    public void setSeed(long l) {
        sr = new SplittableRandom(l);
    }

    @Override
    public double nextDouble() {
        return sr.nextDouble();
    }
}