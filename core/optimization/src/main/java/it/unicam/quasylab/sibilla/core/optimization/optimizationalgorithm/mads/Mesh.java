package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

public class Mesh {

    private double tau;
    private int wPlus;
    private int wMinus;
    private double deltaMesh;

    public double getTau() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
    }

    public int getwPlus() {
        return wPlus;
    }

    public void setwPlus(int wPlus) {
        this.wPlus = wPlus;
    }

    public int getwMinus() {
        return wMinus;
    }

    public void setwMinus(int wMinus) {
        this.wMinus = wMinus;
    }

    public double getDeltaMesh() {
        return deltaMesh;
    }

    public void setDeltaMesh(double deltaMesh) {
        this.deltaMesh = deltaMesh;
    }
}
