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

import it.unicam.quasylab.sibilla.gui.logarithmicView.LogarithmicNumberAxis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import it.unicam.quasylab.sibilla.core.ExecutionEnvironment;


import java.util.Map;
import java.util.TreeMap;

public class BarChartController {


    @FXML
    private CategoryAxis Xaxis;
    @FXML
    private LogarithmicNumberAxis Yaxis;
    @FXML
    public BarChart<String, Double> barchartView;

    private int counter;

    Map<String,ObservableList<XYChart.Data<String, Double>>> myDataList;
    Map<String, XYChart.Series<String, Double>> measuresTable;
    private ExecutionEnvironment<?> executionEnvironment;

    @FXML
    public void init(ExecutionEnvironment<?> executionEnvironment) {
        //Xaxis.setTickUnit(1);
        Xaxis.setLabel("Time");
        Yaxis.setLabel("Occurrences");
        this.executionEnvironment = executionEnvironment;
        this.myDataList = new TreeMap<>();
        this.measuresTable = new TreeMap<>();
        for( String measureName: executionEnvironment.measures()) {
            ObservableList<XYChart.Data<String,Double>> dataList = FXCollections.observableArrayList();
            XYChart.Series<String,Double> dataSerie = new XYChart.Series<>(dataList);
            dataSerie.setName(measureName);
            barchartView.getData().add(dataSerie);
            myDataList.put(measureName,dataList);
            measuresTable.put(measureName,dataSerie);
        }
        addData(executionEnvironment.currentTime(),executionEnvironment.lastMeasures());
        counter = 0;
    }

    private void addData(double currentTime, Map<String, Double> lastMeasures) {
        for (Map.Entry<String, Double> entry: lastMeasures.entrySet()) {
            ObservableList<XYChart.Data<String, Double>> observableList = myDataList.get(entry.getKey());
            if (observableList != null) {
                observableList.add(new XYChart.Data<>(Double.toString(currentTime),entry.getValue()));
            }
        }
    }

    public void step() {
        addData(executionEnvironment.currentTime(), executionEnvironment.lastMeasures());
        counter++;
    }

    public void back() {
        if (counter > 0) {
            for (Map.Entry<String, ObservableList<XYChart.Data<String, Double>>> entry: myDataList.entrySet()) {
                entry.getValue().remove(entry.getValue().size() - 1);
            }
            counter--;
        }
    }

    public void restart(){
        this.barchartView.getData().clear();
        init(this.executionEnvironment);
    }
}
