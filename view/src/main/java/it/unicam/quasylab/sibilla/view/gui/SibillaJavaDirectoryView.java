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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class SibillaJavaDirectoryView implements Initializable {

    @FXML
    private TextField packageName;

    private static File filePath;

    private static Stage window;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        packageName.deselect();
    }

    public void showTheStage(File file) {
        filePath = file;
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/newDirectoryView.fxml"));
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
    public void presetButtonPressed(){
        try {
            Files.createDirectories(new File(filePath.getPath()+"/"+packageName.getText()).toPath());
            FileWriter fw1 = new FileWriter(filePath.getPath()+"/"+packageName.getText() + "/"+packageName.getText()+ ".pm");
            fw1.close();
            window.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void directoryButtonPressed(){
        try {
            Files.createDirectories(new File(filePath.getPath()+"/"+packageName.getText()).toPath());
            window.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
