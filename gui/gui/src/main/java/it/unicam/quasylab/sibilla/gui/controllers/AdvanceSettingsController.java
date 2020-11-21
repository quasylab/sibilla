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
import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class AdvanceSettingsController  {


    private ExecutionEnvironment<PopulationState> ee;


     private InteractiveController ic;

    @FXML public JFXButton stepToBtn;
    @FXML public TextField stepToNumber;
    @FXML public JFXButton timeToBtn;
    @FXML public TextField timeToNumber;


    @FXML
    public void initialize() {
    }

    public void setExecutionEnvironment(ExecutionEnvironment<PopulationState> ee) {
        this.ee = ee;
    }


    public void setRootController(InteractiveController ic) {
        this.ic = ic;
    }


    public void stepTo(MouseEvent me) {
        if (stepToNumber.getText().equals(""))
            showAlert();
        double stepToN = Double.parseDouble((stepToNumber.getText()));
        //Reset simulation steps
        if (stepToN == 0){
            this.ic.restart(me);
            this.ic.update();
        }
        //Advance with simulation steps
        if (ee.steps() < stepToN){
            while (ee.steps() < stepToN){
                this.ic.step(me);
            }
            this.ic.update();
        }
        //Go back with simulation steps
        if (ee.steps() > stepToN){
            while (ee.steps() > stepToN){
                this.ic.previous(me);
            }
            this.ic.update();
        }
    }



    public void timeTo(MouseEvent me){
        if (timeToNumber.getText().equals(""))
            showAlert();
        double timeToN = Double.parseDouble((timeToNumber.getText()));
        //Reset simulation time
        if (timeToN == 0){
            this.ic.restart(me);
            this.ic.update();
        }
        //Advance with simulation time
        if (ee.currentTime() < timeToN){
            while (ee.currentTime() <= timeToN){
                this.ic.step(me);
            }
            this.ic.update();
        }
        //Go back with simulation time
        if (ee.currentTime() > timeToN){
            while (ee.currentTime() >= timeToN){
                this.ic.previous(me);
            }
            this.ic.update();
        }

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


}
