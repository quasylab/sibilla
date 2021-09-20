package it.unicam.quasylab.sibilla.view.controller;

import java.util.ArrayList;
import java.util.List;

public class BasicBuildableLedger implements BuildableLedger<BasicSettings,BuildableFile<BasicSettings>> {

    private List<BuildableFile<BasicSettings>> buildableFilesList;

    public BasicBuildableLedger(){
        this.buildableFilesList = new ArrayList<>();
    }

    public BasicBuildableLedger(List<BuildableFile<BasicSettings>> buildableFilesList){
        this.buildableFilesList=buildableFilesList;
    }


    @Override
    public List<BuildableFile<BasicSettings>> getBuildableList() {
        return this.buildableFilesList;
    }


    @Override
    public void setBuildableList(List<BuildableFile<BasicSettings>> buildableList) {
        this.buildableFilesList=buildableList;
    }
}
