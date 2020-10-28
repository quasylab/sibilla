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
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;

import java.io.IOException;


public class InteractiveController {

    @FXML public JFXTabPane tabPane;
    @FXML public JFXTabPane tablePane;
    //Root
    @FXML private BorderPane root;
    //Tabs
    @FXML public Tab tableTab;
    @FXML public Tab areachartTab;
    @FXML public Tab linechartTab;
    @FXML public Tab barchartTab;
    //Fields
    @FXML private TextField timeunitsField;
    @FXML private TextField stepsField;
    //Commands
    @FXML private JFXButton stepBtn;
    @FXML private JFXButton previousBtn;
    @FXML private JFXButton restartBtn;
    @FXML private ToggleButton advanceCommands;
    //Other
    @FXML private JFXProgressBar progressBar;
    //Controllers
    @FXML public TableViewController tableViewController;
    @FXML public AreaChartController areaChartController;
    @FXML public LineChartController lineChartController;
    @FXML public BarChartController barChartController;


    private ExecutionEnvironment<PopulationState> ee;

    @FXML
    public void setExecutionEnvironment(ExecutionEnvironment<PopulationState> ee){
        this.ee = ee;
        init();
        update();
    }


    @FXML
    public void init() {
        tableViewController.init(this.ee);
        areaChartController.init(this.ee);
        lineChartController.init(this.ee);
        barChartController.init(this.ee);
    }


    @FXML
    public void initialize() {
        tablePane.widthProperty().addListener((observable, oldValue, newValue) ->
        {
            tablePane.setTabMinWidth((tablePane.getWidth() - 11) / tablePane.getTabs().size());
            tablePane.setTabMaxWidth((tablePane.getWidth() - 11) / tablePane.getTabs().size());
        });
        tabPane.widthProperty().addListener((observable, oldValue, newValue) ->
        {
            tabPane.setTabMinWidth((tabPane.getWidth() - 11) / tabPane.getTabs().size());
            tabPane.setTabMaxWidth((tabPane.getWidth() - 11) / tabPane.getTabs().size());
        });
        //Initialize Table View
        Node tableview = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(("/fxml/views/TableView.fxml")));
        try {
            tableview = loader.load();
            //tableview = FXMLLoader.load(getClass().getResource("/fxml/views/TableView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        tableTab.setContent(tableview);
        tableViewController = loader.getController();

        //Initialize Area Chart
        Node areachart = null;
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource(("/fxml/views/AreaChartView.fxml")));
        try {
            areachart = loader2.load();
            //areachart = FXMLLoader.load(getClass().getResource("/fxml/views/AreaChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        areachartTab.setContent(areachart);
        areaChartController = loader2.getController();

        //Initialize Line Chart
        Node linechart = null;
        FXMLLoader loader3 = new FXMLLoader(getClass().getResource(("/fxml/views/LineChartView.fxml")));
        try {
            linechart = loader3.load();
            //linechart = FXMLLoader.load(getClass().getResource("/fxml/views/LineChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        linechartTab.setContent(linechart);
        lineChartController = loader3.getController();

        //Initialize Bar Chart
        Node barchart = null;
        FXMLLoader loader4 = new FXMLLoader(getClass().getResource(("/fxml/views/BarChartView.fxml")));
        try {
             barchart = loader4.load();
            //barchart = FXMLLoader.load(getClass().getResource("/fxml/views/BarChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        barchartTab.setContent(barchart);
        barChartController = loader4.getController();
    }



    @FXML
    public void update(){
        this.timeunitsField.setText(String.valueOf(this.ee.currentTime()));
        this.stepsField.setText(String.valueOf(this.ee.steps()));
        this.tableViewController.tableView();
    }


    @FXML
    public void step(MouseEvent mouseEvent) {
        if (execute(stepBtn.getId(), ee)) {
            areaChartController.step();
            lineChartController.step();
            barChartController.step();
        }
        update();
    }

    @FXML
    public void previous(MouseEvent mouseEvent) {
        if (execute(previousBtn.getId(), ee)) {
            areaChartController.back();
            lineChartController.back();
            barChartController.back();
        }
        update();
    }

    @FXML
    public void restart(MouseEvent mouseEvent) {
        if (execute(restartBtn.getId(), ee)) {
            areaChartController.restart();
            lineChartController.restart();
            barChartController.restart();
        }
        update();
    }

    @FXML
    public void openAdvanceSettings(MouseEvent mouseEvent) {
        loadWindow("/fxml/AdvanceSettings.fxml","Advance Settings");
    }


    @FXML
    private void loadWindow(String loc, String title){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(loc));
            Parent root = loader.load();
            AdvanceSettingsController controller = loader.getController();
            controller.setExecutionEnvironment(this.ee);
            controller.setRootController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    @FXML
    private static boolean execute(String btnId, ExecutionEnvironment<?> ee) {
        switch (btnId) {
            case "stepBtn" :
                ee.step();
                return true;
            case "previousBtn":
                ee.previous();
                return true;
            case "restartBtn":
                ee.restart();
                return true;
        }
        return true;
    }

}
