package it.unicam.quasylab.sibilla.view.controller;

import java.util.ArrayList;
import java.util.List;

public class BasicSettingsLedger implements SettingsLedger<BasicSettings> {

    private List<BasicSettings> settingsList;

    public BasicSettingsLedger(){
        this.settingsList = new ArrayList<>();
    }

    public BasicSettingsLedger(List<BasicSettings> settingsList){
        this.settingsList=settingsList;
    }

    @Override
    public List<BasicSettings> getSettingsList(){
        return this.settingsList;
    }

    @Override
    public void setSettingsList(List<BasicSettings> settingsList){
        this.settingsList=settingsList;
    }
}
