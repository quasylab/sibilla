package it.unicam.quasylab.sibilla.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SibillaJavaFXHelpController implements Initializable {
    private static final String RULES = "REGOLE PRINCIPALI:\n\n" +
            "1) Selezionare il modulo da caricare nella sezione \"Module\" e cliccare il tasto MODULE per caricarlo;\n\n" +
            "2) Selezionare il file da caricare nella sezione \"Files\" e cliccare il tasto LOAD per caricarlo;\n\n" +
            "3) Impostare i parametri (Deadline, Dt, Replica) nelle sezioni dedicate e cliccare il tasto simulate per far partire la simulazione;\n\n" +
            "4) Cliccare il tasto SAVE per salvare i risultati della simulazione nella cartella \"shell/build/install/sshell/bin/results\";\n\n\n\n\n"+
            "REGOLE GENERALI:\n\n1) Se si vuole creare un file con nuove regole, scrivere le regole nella sezione dedicata e assegnare un nome al file,\n " +
            "successivamente cliccare sul tasto CREATE FILE;\n\n"+
            "2) Se si vuole eliminare un file, selezionare il file nella sezione dedicata \"Files\", successivamente cliccare sul tasto DELETE";

    @FXML
    TextArea rules;
    @FXML
    private MenuBar helpMenuBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rules.setText(RULES);
    }

    @FXML
    public void returnButtonPressed() throws IOException {
        Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/sibillaMainView.fxml"));
        Scene tableViewScene = new Scene(tableViewParent);
        Stage window =  (Stage) helpMenuBar.getScene().getWindow();
        window.setScene(tableViewScene);
        window.show();
    }


}
