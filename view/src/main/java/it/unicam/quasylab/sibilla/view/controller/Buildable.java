package it.unicam.quasylab.sibilla.view.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface Buildable<T extends Settings>{


    File getFile();

    /**
     * set the associated module
     * @param module The associated module
     */
    void setModule(String module);

    /**
     * returns the associated module
     * @return the associated module
     */
    String getModule();

    void setSettingsList(List<T> settingsList);

    List<T> getSettingsList();
}
