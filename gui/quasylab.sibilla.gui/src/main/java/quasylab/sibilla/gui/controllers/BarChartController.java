package quasylab.sibilla.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import quasylab.sibilla.core.ExecutionEnvironment;


import java.util.Map;
import java.util.TreeMap;

public class BarChartController {


    @FXML
    private CategoryAxis Xaxis;
    @FXML
    private NumberAxis Yaxis;
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
        Yaxis.setTickUnit(1);
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
