package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import java.util.Map;

public class Particle {

    private final Map<String,Double> position;
    private final Map<String,Double> velocity;
    private Map<String,Double> pBest;

    private double pBestEvaluation;

    public Particle(Map<String,Double> position,
                    Map<String,Double> velocity,
                    Map<String,Double> pBest){
        this.position = position;
        this.velocity = velocity;
        this.pBest = pBest;
    }

    public Map<String, Double> getVelocity() {
        return velocity;
    }

    public Map<String, Double> getPosition() {
        return position;
    }

    public Map<String, Double> getPBest() {
        return pBest;
    }

    public void setPBest(Map<String, Double> pBest) {
        this.pBest = pBest;
    }

    public double getpBestEvaluation() {
        return pBestEvaluation;
    }

    public void setpBestEvaluation(double pBestEvaluation) {
        this.pBestEvaluation = pBestEvaluation;
    }
}
