package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search.MeshInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
//TODO
public class Mesh {

    private double tau;
    private int wPlus;
    private int wMinus;
    private double deltaMesh;

    //HyperRectangle meshSearchSpace;
    List<MeshInterval> meshIntervalList;
    Map<String,Double> point;

    public Mesh(HyperRectangle searchSpace,Map<String,Double> point, double deltaMesh){
        this.point = point;
        this.deltaMesh = deltaMesh;
        this.meshIntervalList = getMeshIntervalList(searchSpace,point,deltaMesh);
    }

    private List<MeshInterval> getMeshIntervalList(HyperRectangle searchSpace, Map<String,Double> referencePoint, double deltaMesh){
        List<MeshInterval> listOfMeshInterval = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            listOfMeshInterval.add(getMeshInterval(i,referencePoint,deltaMesh));
        }
        return listOfMeshInterval;
    }

    public HyperRectangle getMeshAsHyperRectangle() {
        return getMeshAsHyperRectangle(this.meshIntervalList);
    }

    public HyperRectangle getMeshAsHyperRectangle(List<MeshInterval> listOfMeshInterval) {
        return new HyperRectangle(listOfMeshInterval);
    }

    private MeshInterval getMeshInterval(Interval interval, Map<String,Double> referencePoint, double deltaMesh){
        return new MeshInterval(interval.getId(),
                interval.getLowerBound(),
                interval.getUpperBound(),
                deltaMesh,
                referencePoint.get(interval.getId()));
    }



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
        meshIntervalList.forEach(i -> i.setStep(deltaMesh));
        this.deltaMesh = deltaMesh;
    }

    public void setDeltaMesh(Map<String,Double> point) {
        meshIntervalList.forEach(i -> i.setPoint(point.get(i.getId())));
        this.point = point;
    }
}
