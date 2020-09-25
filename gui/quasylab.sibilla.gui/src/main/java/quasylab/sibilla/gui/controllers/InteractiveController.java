package quasylab.sibilla.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import quasylab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.sampling.Measure;

import java.io.DataOutput;
import java.io.IOException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;


public class InteractiveController {

    @FXML private BorderPane root;
    //Table View
    @FXML public Parent tableView;
    //Area Chart
    @FXML public Parent areaChart;
    //Line Chart
    @FXML public Parent lineChart;

    //Bar Chart
    @FXML private Tab barchartTab;
    @FXML private BarChart<?, ?> barchartView;
    @FXML public CategoryAxis barchartXaxis;
    @FXML public NumberAxis barchartYaxis;
    //Header
    //@FXML private TextField modelTypeField;
    @FXML private TextField timeunitsField;
    @FXML private TextField stepsField;
    //Commands
    @FXML private JFXButton stepBtn;
    @FXML private JFXButton previousBtn;
    @FXML private JFXButton restartBtn;
    @FXML private ToggleButton advanceCommands;
    //Other
    @FXML private JFXProgressBar progressBar;

    //Controller delle viste
    @FXML public TableViewController tableViewController = new TableViewController();
    @FXML public AreaChartController areaChartController = new AreaChartController();
    @FXML public LineChartController lineChartController = new LineChartController();

    //Riferimento ad ExecutionEnvironment
    private ExecutionEnvironment<PopulationState> ee;

    @FXML
    public void setExecutionEnvironment(ExecutionEnvironment<PopulationState> ee){
        this.ee = ee;
        init();
        update();
    }

    public ExecutionEnvironment<PopulationState> getExecutionEnvironment(){
        return this.ee;
    }


    @FXML
    public void init() {
        tableViewController.init(this);
        areaChartController.init(this.ee);
        lineChartController.init(this);
    }

    /**
     * Get measures from given model.
     * @return string array of measures
     */
    public String[] measures(){
        return ee.getModel().measures();
    }

    /**
     * This methos create an observable list of my data series in according with given model.
     * @return an observable list of data series.
     */
    public Map<String,ObservableList<XYChart.Series<Double,Double>>> doSeries() {
        TreeMap<String, ObservableList<XYChart.Series<Double, Double>>> toReturn = new TreeMap<>();
        if (ee.getModel().measures() == null) {
            System.out.println("There are no measurements");
        } else {

            for (String m : ee.getModel().measures()) {
                FXCollections.observableArrayList();
                XYChart.Series<Double, Double> current = new XYChart.Series<>();
                ObservableList<XYChart.Series<Double, Double>> myList = null;
                toReturn.put(m, myList);
            }
        }
        return toReturn;
    }


/*
    @FXML
    public void initialize() {

        //Initialize Table View
        Node tableview = null;
        try {
            tableview = FXMLLoader.load(getClass().getResource("/fxml/views/TableView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        tableviewTab.setContent(tableview);


        //Initialize Area Chart
        Node areachart = null;
        try {
            areachart = FXMLLoader.load(getClass().getResource("/fxml/views/AreaChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        areachartTab.setContent(areachart);

        //Initialize Line Chart
        Node linechart = null;
        try {
            linechart = FXMLLoader.load(getClass().getResource("/fxml/views/LineChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        linechartTab.setContent(linechart);

        //Initialize Bar Chart
        Node barchart = null;
        try {
            barchart = FXMLLoader.load(getClass().getResource("/fxml/views/BarChartView.fxml"));
        } catch (IOException e) {
            System.out.println("StatusSectionController doesn't found fxml file");
        }
        barchartTab.setContent(barchart);


    }


 */


    @FXML
    private void tableView(){
        tableViewController.tableView();
    }


    @FXML
    private void createLineChart(){
        lineChartController.lineChartView();
    }


    @FXML
    public void update(){
        this.timeunitsField.setText(String.valueOf(this.ee.currentTime()));
        this.stepsField.setText(String.valueOf(this.ee.steps()));
        //tableView();
        //createLineChart();
        //barChartView();
    }

/*
    @FXML
    private void tableView() {
        //Create my observable list according to considered model
        ObservableList<Measure> observableList = FXCollections.observableArrayList();
        for (String s: ee.getModel().measures()) {
            observableList.add(ee.getModel().getMeasure(s));
        }

        //Create my observable list according SEIR model
        ObservableList<Measure> occurrencesList2 = FXCollections.observableArrayList(
                ee.getModel().getMeasure("S"),
                ee.getModel().getMeasure("A"),
                ee.getModel().getMeasure("G"),
                ee.getModel().getMeasure("R"),
                ee.getModel().getMeasure("D"));

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
                double current = param.getValue().measure(ee.currentState());
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

 */
/*

    XYChart.Series index0 = new XYChart.Series();
    XYChart.Series index1 = new XYChart.Series();
    XYChart.Series index2 = new XYChart.Series();
    XYChart.Series index3 = new XYChart.Series();
    XYChart.Series index4 = new XYChart.Series();


    @FXML
    private void areaChartView() {
        //if (ee.currentTime() == 0){
        // X axis
        //areachartXaxis.setAutoRanging(false);
        //areachartXaxis.setLowerBound(0);
        //areachartXaxis.setUpperBound(ee.steps());
        areachartXaxis.setTickUnit(1);
        areachartXaxis.setLabel("Steps");
        // Y axis
        //areachartYaxis.setAutoRanging(false);
        //areachartYaxis.setLowerBound(0);
        //areachartYaxis.setUpperBound(10000);
        areachartYaxis.setTickUnit(1);
        areachartYaxis.setLabel("Occurrences");

        index0.setName("Suscettible");
        index1.setName("Asintomatic");
        index2.setName("Grave");
        index3.setName("Recovered");
        index4.setName("Death");


        index0.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(0)));
        index1.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(1)));
        index2.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(2)));
        index3.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(3)));
        index4.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(4)));

        ObservableList<XYChart.Series<Integer, Double>> seriesList = FXCollections.observableArrayList();
        seriesList.setAll(index0,index1,index2,index3,index4);
        areachartView.setData(seriesList);
        //areachartView.getData().addAll(index0,index1,index2,index3,index4);
    }

 */




/*


    XYChart.Series linechartSeries_index0 = new XYChart.Series();
    XYChart.Series linechartSeries_index1 = new XYChart.Series();
    XYChart.Series linechartSeries_index2 = new XYChart.Series();
    XYChart.Series linechartSeries_index3 = new XYChart.Series();
    XYChart.Series linechartSeries_index4 = new XYChart.Series();

    private void lineChartView() {
        linechartXaxis.setLabel("Steps");
        linechartYaxis.setLabel("Occurrences");


        linechartSeries_index0.setName("Suscettible");
        linechartSeries_index1.setName("Asintomatic");
        linechartSeries_index2.setName("Grave");
        linechartSeries_index3.setName("Recovered");
        linechartSeries_index4.setName("Death");

        linechartSeries_index0.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(0)));
        linechartSeries_index1.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(1)));
        linechartSeries_index2.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(2)));
        linechartSeries_index3.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(3)));
        linechartSeries_index4.getData().add(new XYChart.Data<>(ee.steps(), ee.currentState().getOccupancy(4)));


        linechartView.getData().addAll(linechartSeries_index0,linechartSeries_index1,
                linechartSeries_index2,linechartSeries_index3,linechartSeries_index4);
    }


 */
    /*

    XYChart.Series barchartSeries_index0 = new XYChart.Series();
    XYChart.Series barchartSeries_index1 = new XYChart.Series();
    XYChart.Series barchartSeries_index2 = new XYChart.Series();
    XYChart.Series barchartSeries_index3 = new XYChart.Series();
    XYChart.Series barchartSeries_index4 = new XYChart.Series();

    private void barChartView() {
        barchartXaxis.setLabel("Steps");
        barchartYaxis.setLabel("Occurrences");

        barchartSeries_index0.setName("Suscettible");
        barchartSeries_index1.setName("Asintomatic");
        barchartSeries_index2.setName("Grave");
        barchartSeries_index3.setName("Recovered");
        barchartSeries_index4.setName("Death");

        barchartSeries_index0.getData().add(new XYChart.Data<>("STEPS", ee.currentState().getOccupancy(0)));
        barchartSeries_index1.getData().add(new XYChart.Data<>("STEPS", ee.currentState().getOccupancy(1)));
        barchartSeries_index2.getData().add(new XYChart.Data<>("STEPS", ee.currentState().getOccupancy(2)));
        barchartSeries_index3.getData().add(new XYChart.Data<>("STEPS", ee.currentState().getOccupancy(3)));
        barchartSeries_index4.getData().add(new XYChart.Data<>("STEPS", ee.currentState().getOccupancy(4)));

        barchartView.getData().addAll(barchartSeries_index0,barchartSeries_index1,barchartSeries_index2,barchartSeries_index3,barchartSeries_index4);

    }

     */

    @FXML
    public void step(MouseEvent mouseEvent) {
        if (execute(stepBtn.getId(), ee)) {
            //consoleArea.appendText("STEP\n");
            //update();
            areaChartController.step();
        }
    }

    @FXML
    public void previous(MouseEvent mouseEvent) {
        if (execute(previousBtn.getId(), ee)) {
            //consoleArea.appendText("PREVIUOS\n");
            update();
            areaChartController.back();
        }
    }

    @FXML
    private void restart(MouseEvent mouseEvent) {
        if (execute(restartBtn.getId(), ee)) {
            //consoleArea.setText("RESTART\n");
            update();
        }
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


    public Parent getRoot() {
        return this.root;
    }
}
