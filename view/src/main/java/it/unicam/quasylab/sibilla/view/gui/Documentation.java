package it.unicam.quasylab.sibilla.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Documentation {

    private static final String helpFile = "/documentation/SIBILLA__Getting_Started.pdf";

    public void showHelpFile(){
        if (Desktop.isDesktopSupported()) {
            File myFile = new File(getClass().getResource(helpFile).getFile());
            try {

                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                // no application registered for PDFs
            }
        }
    }


}
