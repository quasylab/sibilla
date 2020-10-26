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

package it.unicam.quasylab.sibilla.gui.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;

public class AdvanceSettingsController  {

     private ExecutionEnvironment<PopulationState> ee;


    @FXML
    InteractiveController ic;

    @FXML
    JFXButton stepToBtn;
    @FXML
    JFXButton previousToBtn;
    @FXML
    TextField stepToNumber;
    @FXML
    TextField previousToNumber;


    @FXML
    public void initialize() {
    }

    public void setExecutionEnvironment(ExecutionEnvironment<PopulationState> ee) {
        this.ee = ee;
    }

    @FXML
    public void setRootController(InteractiveController ic) {
        this.ic = ic;
    }

    @FXML
    public void stepTo(MouseEvent mouseEvent) {
        if (stepToNumber.getText().equals(""))
            showAlert();
        int stepN = Integer.parseInt(stepToNumber.getText());
        if (stepN == 0){}
        int i=0;
        while (i<stepN){
            this.ic.step(mouseEvent);
            i++;
        }
        this.ic.update();
    }

    @FXML
    public void previousTo (MouseEvent mouseEvent){
        if (previousToNumber.getText().equals(""))
            showAlert();
        int previousN = Integer.parseInt(previousToNumber.getText());
        if (previousN == 0){}
        int i=0;
        while (i<previousN){
            this.ic.previous(mouseEvent);
            i++;
        }
        this.ic.update();
    }

    @FXML
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "You have not entered any value",
                ButtonType.OK);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.show();
    }

    @FXML
    private void checkEvent(MouseEvent me, MouseButton mb){
        if (me.getSource().equals(mb));
    }


}
