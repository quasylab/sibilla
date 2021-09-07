package it.unicam.quasylab.sibilla.view.controller;

import java.util.ArrayList;
import java.util.List;

public class SettingsLedger {

    private List<Settings> settingsList;

    public SettingsLedger(){
        this.settingsList = new ArrayList<>();
    }

    public SettingsLedger(List<Settings> settingsList){
        this.settingsList=settingsList;
    }

    public List<Settings> getSettingsList(){
        return this.settingsList;
    }

    public void setSettingsList(List<Settings> settingsList){
        this.settingsList=settingsList;
    }
}
