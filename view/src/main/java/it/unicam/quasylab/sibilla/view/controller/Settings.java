package it.unicam.quasylab.sibilla.view.controller;

import java.io.File;
import java.util.Map;

/**
 * Allows to create a settings
 * @author LorenzoSerini
 */
public interface Settings {

    /**
     * returns the associated file
     * @return settings file
     */
    String getFilePath();

    /**
     * returns the associated file
     * @return settings file
     */
    File getFile();


    /**
     * set the settings file
     * @param file The settings file
     */
    void setFile(File file);

    /**
     * returns the label name
     * @return the label name
     */
    String getLabel();

    /**
     * set the label name
     * @param label The label name
     */
    void setLabel(String label);

    /**
     * returns the associated module
     * @return the associated module
     */
    String getModule();

    /**
     * set the associated module
     * @param module The associated module
     */
    void setModule(String module);


    /**
     * returns the associated parameters
     * @return the associated parameters
     */
    Map<String, Double> getParameters();


    /**
     * set the associated parameters
     * @param parameters The associated parameters
     */
    void setParameters(Map<String, Double> parameters);

    /**
     * returns deadline
     * @return deadline
     */
    double getDeadline();

    /**
     * Set deadline
     * @param deadline The deadline
     */
    void setDeadline(double deadline);


    /**
     * returns Dt
     * @return Dt
     */
    double getDt();

    /**
     * set Dt
     * @param dt Dt
     */
    void setDt(double dt);

    /**
     * returns replica
     * @return Replica
     */
    int getReplica();

    /**
     * set replica
     * @param replica Replica
     */
    void setReplica(int replica);

    /**
     * returns measures map
     * @return measures map
     */
    Map<String, Boolean> getMeasures();

    /**
     * set measures map
     * @param measures measures map
     */
    void setMeasures(Map<String, Boolean> measures);

}
