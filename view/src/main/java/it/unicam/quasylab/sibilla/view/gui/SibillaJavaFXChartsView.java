package it.unicam.quasylab.sibilla.view.gui;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SibillaJavaFXChartsView implements Initializable {

    @FXML
    private LineChart<String,Double> meanChart;

    @FXML
    private LineChart<String,Double> varianceChart;

    @FXML
    private LineChart<String,Double> standardDeviationChart;

    private static File fileToPlot;

    private Map<Double, Double> meanMap;
    private Map<Double, Double> varianceMap;
    private Map<Double, Double> standardDeviationMap;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        meanMap = new LinkedHashMap<>();
        varianceMap = new LinkedHashMap<>();
        standardDeviationMap = new LinkedHashMap<>();
        try {
            Scanner sc = new Scanner(fileToPlot);
            while (sc.hasNext()){
                String[] a = sc.next().split(";");

                meanMap.put(Double.valueOf(a[0]), Double.valueOf(a[1]));
                varianceMap.put(Double.valueOf(a[0]), Double.valueOf(a[2]));
                standardDeviationMap.put(Double.valueOf(a[0]), Double.valueOf(a[3]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XYChart.Series<String,Double> meanSeries = new XYChart.Series<>();
        XYChart.Series<String,Double> varianceSeries = new XYChart.Series<>();
        XYChart.Series<String,Double> standardDeviationSeries = new XYChart.Series<>();


        for(Double key:meanMap.keySet()){
            meanSeries.getData().add(new XYChart.Data<>(String.valueOf(key),meanMap.get(key)));
        }
        for(Double key:varianceMap.keySet()){
            varianceSeries.getData().add(new XYChart.Data<>(String.valueOf(key),varianceMap.get(key)));
        }
        for(Double key:standardDeviationMap.keySet()){
            standardDeviationSeries.getData().add(new XYChart.Data<>(String.valueOf(key),standardDeviationMap.get(key)));
        }
        this.meanChart.getData().add(meanSeries);
        this.varianceChart.getData().add(varianceSeries);
        this.standardDeviationChart.getData().add(standardDeviationSeries);
    }

    public void showTheStage(File file){
        fileToPlot=file;
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/chartsView.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            Stage window = new Stage();
            window.setTitle(file.getName());
            window.initModality(Modality.WINDOW_MODAL);
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
