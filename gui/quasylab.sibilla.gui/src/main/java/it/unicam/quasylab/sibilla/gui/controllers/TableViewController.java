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

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;


public class TableViewController {

    @FXML public InteractiveController mainController;


    @FXML public TableView<Measure> tableView;
    @FXML public TableColumn<Measure, String> agentsCol;
    @FXML public TableColumn<Measure, Integer> occurrencesCol;
    private ExecutionEnvironment<?> executionEnvironment;

    @FXML
    public void init(ExecutionEnvironment<?> executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    @FXML
    public void tableView() {
        //Create my observable list according to considered model
        ObservableList<Measure> observableList = FXCollections.observableArrayList();
        for (String s: executionEnvironment.getModel().measures()) {
            observableList.add(executionEnvironment.getModel().getMeasure(s));
        }
/*
        //Create my observable list according SEIR model
        ObservableList<Measure> occurrencesList2 = FXCollections.observableArrayList(
                mainController.getExecutionEnvironment().getModel().getMeasure("S"),
                mainController.getExecutionEnvironment().getModel().getMeasure("A"),
                mainController.getExecutionEnvironment().getModel().getMeasure("G"),
                mainController.getExecutionEnvironment().getModel().getMeasure("R"),
                mainController.getExecutionEnvironment().getModel().getMeasure("D"));

 */

        tableView.setItems(observableList);
        agentsCol = new TableColumn<>("AGENTS");
        occurrencesCol = new TableColumn<>("OCCURENCES");

        //In first column show the specie's names.
        agentsCol.setCellValueFactory(param -> new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super String> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super String> listener) {

            }

            @Override
            public String getValue() {
                // return a name of a single specie in currentState
                return  param.getValue().getName();
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        });

        //In second column show the occurrences species.
        occurrencesCol.setCellValueFactory(param -> new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super Integer> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super Integer> listener) {

            }

            @Override
            public Integer getValue() {
                double current = param.getValue().measure(executionEnvironment.currentState());
                return (int) Math.round(current);
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        });



        tableView.getColumns().setAll(agentsCol, occurrencesCol);
    }




}
