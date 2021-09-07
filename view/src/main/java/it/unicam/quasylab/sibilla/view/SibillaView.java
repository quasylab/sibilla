package it.unicam.quasylab.sibilla.view;

import it.unicam.quasylab.sibilla.view.controller.Settings;
import it.unicam.quasylab.sibilla.view.controller.SettingsLedger;
import it.unicam.quasylab.sibilla.view.gui.SibillaJavaFX;
import it.unicam.quasylab.sibilla.view.persistence.SettingsPersistenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SibillaView {
    private final View view;

    /**
     * Type of views
     */
    public enum SIBILLA_TYPE {
        BASIC;
    }

    public SibillaView(View view) {
        this.view = view;
    }

    public static void main(String[] args){
        if (args.length == 0) {
            Objects.requireNonNull(createSibilla("BASIC")).start();
        } else {
            try {
                Objects.requireNonNull(createSibilla(args[0])).start();
            } catch (IllegalArgumentException e) {
                System.err.println(args[0]+" Sibilla is unknown!");
            }
        }
    }

    /**
     * Launch the view
     */
    public void start(){
        view.open();
    }

    /**
     * Creates a Sibilla view
     * @param code the graphics with which you want to launch the application
     * @return SibillaView
     */
    public static SibillaView createSibilla(String code) {
        switch (SIBILLA_TYPE.valueOf(code.toUpperCase())) {
            case BASIC:
                return createBasicSibilla();
        }
        return null;
    }

    /**
     * Create a Basic Sibilla View
     * @return SibillaView
     */
    public static SibillaView createBasicSibilla() {
        return new SibillaView(new SibillaJavaFX());
    }
}

