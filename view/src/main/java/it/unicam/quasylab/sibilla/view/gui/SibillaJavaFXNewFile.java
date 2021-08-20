package it.unicam.quasylab.sibilla.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SibillaJavaFXNewFile implements Initializable {

    @FXML
    private TextField fileName;

    private static File filePath;

    private static Stage window;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileName.deselect();
    }

    public void showTheStage(File file){
        filePath=file;
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/newSibillaFile.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setScene(tableViewScene);
            window.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void okButtonPressed(){
        try {
            FileWriter fw1 = new FileWriter(filePath.getPath()+"/"+this.fileName.getText()+".pm");
            fw1.close();
            window.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
