package it.unicam.quasylab.sibilla.view.controller;

import javafx.scene.control.CheckBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    private String filePath;
    private String label;
    private String module;
    private Map<String, Double> parameters;
    private double deadline;
    private double dt;
    private int replica;
    private Map<String, Boolean> measures;

    public Settings(File file, String label, String module){
        this.filePath=file.getPath();
        this.label=label;
        this.module=module;
        this.parameters = new HashMap<>();
        this.measures = new HashMap<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile(){return new File(filePath);}

    public void setFile(File file) {
        this.filePath = file.getPath();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Map<String, Double> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Double> parameters) {
        this.parameters = parameters;
    }

    public double getDeadline() {
        return deadline;
    }

    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public int getReplica() {
        return replica;
    }

    public void setReplica(int replica) {
        this.replica = replica;
    }

    public Map<String, Boolean> getMeasures() {
        return measures;
    }

    public void setMeasures(Map<String, Boolean> measures) {
        this.measures = measures;
    }

}
