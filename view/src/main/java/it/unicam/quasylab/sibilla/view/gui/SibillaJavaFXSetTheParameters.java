package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.view.controller.GUIController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SibillaJavaFXSetTheParameters implements Initializable {
    private static Stage window;


    @FXML
    private TextField deadline;

    @FXML
    private TextField dt;

    @FXML
    private TextField replica;

    @FXML
    private Label errorMessage;





    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


    public void showTheStage(){
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/setTheParametersView.fxml"));
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
    public void setButtonPressed(){
        try {

            if(!deadline.getText().equals("")){
                if(!dt.getText().equals("")){
                    if(!replica.getText().equals("")){
                        GUIController.getInstance().getSibillaShellInterpreter().getRuntime().setDeadline(Double.parseDouble(deadline.getText()));
                        GUIController.getInstance().getSibillaShellInterpreter().getRuntime().setDt(Double.parseDouble(dt.getText()));
                        GUIController.getInstance().getSibillaShellInterpreter().getRuntime().setReplica(Integer.parseInt(replica.getText()));
                        window.close();
                    }else errorMessage.setText("ERROR: set a value to the Replica!");
                }else errorMessage.setText("ERROR: set a value to the Dt!");
            }else errorMessage.setText("ERROR: set a value to the Dead Line!");

    } catch (CommandExecutionException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void clearButtonPressed(){
        deadline.clear();
        dt.clear();
        replica.clear();
    }



}
