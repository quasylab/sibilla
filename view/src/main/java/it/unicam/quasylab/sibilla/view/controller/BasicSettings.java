package it.unicam.quasylab.sibilla.view.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class BasicSettings implements Settings{
    private String filePath;
    private String label;
    private String module;
    private Map<String, Double> parameters;
    private double deadline;
    private double dt;
    private int replica;
    private Map<String, Boolean> measures;

    public BasicSettings(File file, String label, String module){
        this.filePath=file.getPath();
        this.label=label;
        this.module=module;
        this.parameters = new HashMap<>();
        this.measures = new HashMap<>();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public File getFile(){return new File(filePath);}

    @Override
    public void setFile(File file) {
        this.filePath = file.getPath();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public Map<String, Double> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Map<String, Double> parameters) {
        this.parameters = parameters;
    }

    @Override
    public double getDeadline() {
        return deadline;
    }

    @Override
    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }

    @Override
    public double getDt() {
        return dt;
    }

    @Override
    public void setDt(double dt) {
        this.dt = dt;
    }

    @Override
    public int getReplica() {
        return replica;
    }

    @Override
    public void setReplica(int replica) {
        this.replica = replica;
    }

    @Override
    public Map<String, Boolean> getMeasures() {
        return measures;
    }

    @Override
    public void setMeasures(Map<String, Boolean> measures) {
        this.measures = measures;
    }

}
