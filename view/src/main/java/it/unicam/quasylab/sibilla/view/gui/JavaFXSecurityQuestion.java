package it.unicam.quasylab.sibilla.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class JavaFXSecurityQuestion implements Initializable {
    private static Path path;
    private static Stage window;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void showSecurityQuestion(Path path){
        setPath(path);
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/securityQuestion.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            window =  new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setScene(tableViewScene);
            window.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPath(Path filePath){
        path=filePath;
    }

    @FXML
   public void yesButtonPressed(){
       try {
           Files.deleteIfExists(path);
       } catch (IOException e) {
           e.printStackTrace();
       }
       window.close();
   }

    @FXML
   public void noButtonPressed(){
        window.close();
   }

   public boolean isDeleted(Path path){
       return !Files.exists(path);
   }


}
