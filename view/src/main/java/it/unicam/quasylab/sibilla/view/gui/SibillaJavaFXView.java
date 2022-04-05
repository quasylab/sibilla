package it.unicam.quasylab.sibilla.view.gui;



import it.unicam.quasylab.sibilla.view.controller.GUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Optional;

public class SibillaJavaFXView extends Application implements View {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage=primaryStage;
        this.primaryStage.setTitle("Sibilla");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/view/icon/unicam.png")));
        showMainStage();
    }

    private void showMainStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/sibillaMainView.fxml"));
        VBox mainLayout = loader.load();
        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add(getClass().getResource("/view/style/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to quit?",
                    ButtonType.YES,
                    ButtonType.CANCEL);
            Optional<ButtonType> confirm = a.showAndWait();

            if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
                primaryStage.close();
                try {
                    GUIController.getInstance().saveOpenedProject();
                    GUIController.getInstance().closeProject();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            event.consume();
        });
    }

    @Override
    public void open() {
        Application.launch(getClass());
    }
}

