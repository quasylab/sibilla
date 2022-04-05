package it.unicam.quasylab.sibilla.view.controller;

import java.util.List;

/**
 * Allows to create a settings ledger
 * @param <T> extends Settings
 * @author LorenzoSerini
 */
public interface BuildableLedger <S extends Settings, T extends Buildable<S>>{

    /**
     * Returns buildable list
     * @return buildable list
     */
    List<T> getBuildableList();

    /**
     * set buildable list
     * @param buildableList Buildable list
     */
    void setBuildableList(List<T> buildableList);
}
