package it.unicam.quasylab.sibilla.view;



import it.unicam.quasylab.sibilla.view.gui.SibillaJavaFXView;

import java.util.Objects;


/**
 * This is the GUI main class
 *
 * @author LorenzoSerini
 *
 */
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
     * @return SibillaJavaFXView
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
     * @return SibillaJavaFXView
     */
    public static SibillaView createBasicSibilla() {
        return new SibillaView(new SibillaJavaFXView());
    }
}

