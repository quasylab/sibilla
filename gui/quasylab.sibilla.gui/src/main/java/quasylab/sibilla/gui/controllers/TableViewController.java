package quasylab.sibilla.gui.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import quasylab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.sampling.Measure;

import java.io.IOException;


public class TableViewController {

    @FXML public InteractiveController mainController;


    @FXML public TableView<Measure> tableView;
    @FXML public TableColumn<Measure, String> agentsCol;
    @FXML public TableColumn<Measure, Integer> occurrencesCol;

    @FXML
    public void init(InteractiveController interactiveController) {
        mainController = interactiveController;
    }

    @FXML
    public void tableView() {
        //Create my observable list according to considered model
        ObservableList<Measure> observableList = FXCollections.observableArrayList();
        for (String s: mainController.getExecutionEnvironment().getModel().measures()) {
            observableList.add(mainController.getExecutionEnvironment().getModel().getMeasure(s));
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
                double current = param.getValue().measure(mainController.getExecutionEnvironment().currentState());
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
