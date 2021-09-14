package it.unicam.quasylab.sibilla.view.controller;

import java.util.List;

/**
 * Allows to create a settings ledger
 * @param <T> extends Settings
 * @author LorenzoSerini
 */
public interface SettingsLedger <T extends Settings>{

    /**
     * Returns settings list
     * @return settings list
     */
    List<BasicSettings> getSettingsList();

    /**
     * set setting list
     * @param settingsList Settings list
     */
    void setSettingsList(List<BasicSettings> settingsList);
}
