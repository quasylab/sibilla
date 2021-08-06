package it.unicam.quasylab.sibilla.view.gui;



import it.unicam.quasylab.sibilla.view.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.IOException;

public class SibillaJavaFX extends Application implements View {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage=primaryStage;
        this.primaryStage.setTitle("Sibilla");
        showMainStage();
    }

    private void showMainStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sibillaMainView.fxml"));
        VBox mainLayout = loader.load();
        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void open() {
        Application.launch(getClass());
    }
}

