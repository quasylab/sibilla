package quasylab.sibilla.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class LineChartController {

    @FXML public InteractiveController mainController;

    @FXML public LineChart linechartView;
    @FXML public NumberAxis linechartXaxis;
    @FXML public NumberAxis linechartYaxis;

    @FXML
    public void init(InteractiveController interactiveController) {
        mainController = interactiveController;
    }

    XYChart.Series linechartSeries_index0 = new XYChart.Series();
    XYChart.Series linechartSeries_index1 = new XYChart.Series();
    XYChart.Series linechartSeries_index2 = new XYChart.Series();
    XYChart.Series linechartSeries_index3 = new XYChart.Series();
    XYChart.Series linechartSeries_index4 = new XYChart.Series();

    public void lineChartView() {
        linechartXaxis.setLabel("Steps");
        linechartYaxis.setLabel("Occurrences");


        linechartSeries_index0.setName("Suscettible");
        linechartSeries_index1.setName("Asintomatic");
        linechartSeries_index2.setName("Grave");
        linechartSeries_index3.setName("Recovered");
        linechartSeries_index4.setName("Death");

        linechartSeries_index0.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(0)));
        linechartSeries_index1.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(1)));
        linechartSeries_index2.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(2)));
        linechartSeries_index3.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(3)));
        linechartSeries_index4.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(4)));


        linechartView.getData().addAll(linechartSeries_index0,linechartSeries_index1,
                linechartSeries_index2,linechartSeries_index3,linechartSeries_index4);
    }




}
