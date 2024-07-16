package it.unicam.quasylab.sibilla.tools.synthesis;
@FunctionalInterface
public interface ConvergenceCriteria {
    boolean isConverged(Synthesizer synthesizer);
}
