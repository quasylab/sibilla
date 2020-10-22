package quasylab.sibilla.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import quasylab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.models.pm.PopulationState;

import java.util.Map;
import java.util.TreeMap;

public class LineChartController {

    @FXML public LineChart<Double, Double> linechartView;
    @FXML public NumberAxis Xaxis;
    @FXML public NumberAxis Yaxis;
    private int counter;

    Map<String, ObservableList<XYChart.Data<Double, Double>>> myDataList;
    Map<String, XYChart.Series<Double, Double>> measuresTable;
    private ExecutionEnvironment<?> executionEnvironment;

    @FXML
    public void init(ExecutionEnvironment<?> executionEnvironment) {
        Xaxis.setTickUnit(1);
        Xaxis.setLabel("Time");
        Yaxis.setTickUnit(1);
        Yaxis.setLabel("Occurrences");
        this.executionEnvironment = executionEnvironment;
        this.myDataList = new TreeMap<>();
        this.measuresTable = new TreeMap<>();
        for( String measureName: executionEnvironment.measures()) {
            ObservableList<XYChart.Data<Double,Double>> dataList = FXCollections.observableArrayList();
            XYChart.Series<Double,Double> dataSerie = new XYChart.Series<>(dataList);
            dataSerie.setName(measureName);
            linechartView.getData().add(dataSerie);
            myDataList.put(measureName,dataList);
            measuresTable.put(measureName,dataSerie);
        }
        addData(executionEnvironment.currentTime(),executionEnvironment.lastMeasures());
        counter = 0;
    }

    private void addData(double currentTime, Map<String, Double> lastMeasures) {
        for (Map.Entry<String, Double> entry: lastMeasures.entrySet()) {
            ObservableList<XYChart.Data<Double, Double>> observableList = myDataList.get(entry.getKey());
            if (observableList != null) {
                observableList.add(new XYChart.Data<>(currentTime,entry.getValue()));
            }
        }
    }

    public void step() {
        addData(executionEnvironment.currentTime(), executionEnvironment.lastMeasures());
        counter++;
    }

    public void back() {
        if (counter > 0) {
            for (Map.Entry<String, ObservableList<XYChart.Data<Double, Double>>> entry: myDataList.entrySet()) {
                entry.getValue().remove(entry.getValue().size() - 1);
            }
            counter--;
        }
    }

    public void restart(){
        this.linechartView.getData().clear();
        init(this.executionEnvironment);
    }





}
