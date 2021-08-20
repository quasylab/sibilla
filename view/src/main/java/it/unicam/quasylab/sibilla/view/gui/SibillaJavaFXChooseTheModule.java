package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.view.controller.GUIController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SibillaJavaFXChooseTheModule implements Initializable {
    private static Stage window;

    @FXML
    private ListView<String> moduleList;

    private static String chosenModule;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moduleList.getItems().addAll(GUIController.getInstance().getSibillaShellInterpreter().getRuntime().getModules());//sibillaShellInterpreter.getRuntime().getModules());
        moduleList.getSelectionModel().clearSelection();
        chosenModule= null;
    }


    public void showTheStage(){
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/chooseTheModuleView.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            window =  new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setScene(tableViewScene);
            window.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void chooseModuleOkButtonPressed(){
        try {
            chosenModule = moduleList.getSelectionModel().getSelectedItem();
            GUIController.getInstance().loadModule(moduleList.getSelectionModel().getSelectedItem());
            window.close();
        } catch (CommandExecutionException e) {
            e.printStackTrace();
        }
    }

    public String getChosenModule(){ return chosenModule; }

}

