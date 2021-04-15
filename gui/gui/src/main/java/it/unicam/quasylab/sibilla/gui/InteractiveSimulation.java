/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.gui;

import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.gui.controllers.InteractiveController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import it.unicam.quasylab.sibilla.examples.pm.seir.CovidDefinition;

import java.io.IOException;


public class InteractiveSimulation extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        PopulationModelDefinition def = new PopulationModelDefinition(CovidDefinition::generatePopulationRegistry,
                CovidDefinition::getRules,
                CovidDefinition::states);
        ExecutionEnvironment<PopulationState> ee = new ExecutionEnvironment<>(
                new DefaultRandomGenerator(),
                def.createModel(),
                def.state()
        );



        FXMLLoader loader = new FXMLLoader(getClass().getResource(("/fxml/InteractiveView.fxml")));
        Parent root = loader.load();

        InteractiveController controller = loader.getController();

        controller.setExecutionEnvironment(ee);
        Scene scene = new Scene(root);

        scene.getStylesheets().add("/css/style.css");
        stage.setTitle("Debugger");
        stage.setScene(scene);
        stage.show();


        stage.setOnCloseRequest(this::closeProgram);
}

    private void closeProgram(WindowEvent evt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Do you really want to close this application?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null);
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            evt.consume();
        }
        else {
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
