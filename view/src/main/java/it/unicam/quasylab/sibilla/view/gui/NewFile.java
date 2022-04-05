package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.view.controller.GUIController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class NewFile implements Initializable {

    @FXML
    private TextField fileName;

    @FXML
    private ChoiceBox<String> module;

    private static File filePath;

    private static Stage window;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> parametersModuleData = FXCollections.observableArrayList();
        parametersModuleData.addAll(Arrays.asList(GUIController.getInstance().getSibillaRuntime().getModules()));
        module.setItems(parametersModuleData);
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
        if(!this.fileName.getText().isEmpty()) {
            if(!this.module.getValue().isEmpty()) {
                try {
                    FileWriter fw1 = new FileWriter(filePath.getPath() + "/" + this.fileName.getText() + ".pm");
                    fw1.close();
                    window.close();
                    GUIController.getInstance().addNewBuildableFile(new File(filePath.getPath() + "/" + this.fileName.getText() + ".pm"), module.getValue());
                    GUIController.getInstance().saveSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
