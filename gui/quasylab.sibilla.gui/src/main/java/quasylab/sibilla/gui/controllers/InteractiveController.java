package quasylab.sibilla.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import quasilab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.past.State;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public class InteractiveController {

    @FXML private BorderPane root;

    //Table View
    @FXML private Tab tableviewTab;
    @FXML private TableView<PopulationState> tableView;
    @FXML public TableColumn<PopulationState,String> agentsCol;
    @FXML public TableColumn<PopulationState,Integer> occurrencesCol;
    //Area Chart
    @FXML private Tab areachartTab;
    @FXML private AreaChart<Integer, Double> areachartView;
    @FXML public NumberAxis areachartXaxis;
    @FXML public NumberAxis areachartYaxis;
    //Line Chart
    @FXML private Tab linechartTab;
    @FXML private LineChart<Integer, Double> linechartView;
    @FXML public NumberAxis linechartXaxis;
    @FXML public NumberAxis linechartYaxis;
    //Bar Chart
    @FXML private Tab barchartTab;
    @FXML private BarChart<?, ?> barchartView;
    @FXML public CategoryAxis barchartXaxis;
    @FXML public NumberAxis barchartYaxis;
    //Pie Chart
    @FXML private Tab piechartTab;
    @FXML private PieChart piechartView;

    //Header
    @FXML private TextField modelTypeField;
    @FXML private TextField timeunitsField;
    @FXML private TextField stepsField;
    //Commands
    @FXML private JFXButton stepBtn;
    @FXML private JFXButton previousBtn;
    @FXML private JFXButton restartBtn;
    @FXML private ToggleButton advanceCommands;
    //Other
    @FXML private JFXProgressBar progressBar;
    @FXML private TextArea consoleArea;




    //Riferimento ad ExecutionEnvironment
    private ExecutionEnvironment<PopulationState> ee;

    public void setExecutionEnvironment(ExecutionEnvironment<PopulationState> ee){
        this.ee = ee;
        update();
    }


    @FXML
    public void update(){
        //this.modelTypeField.setText(String.valueOf(this.ee.getModel()));
        this.timeunitsField.setText(String.valueOf(this.ee.currentTime()));
        this.stepsField.setText(String.valueOf(this.ee.steps()));
        this.consoleArea.appendText(this.ee.currentState().toString()+"\n\n");
        tableView();
        areaChartView();
        lineChartView();
        barChartView();
        pieChartView();
    }


    @FXML
    private void tableView() {
        /*ObservableList<Integer> occurrencesList = FXCollections.observableArrayList(
            (int) ee.currentState().getOccupancy(0),
            (int) ee.currentState().getOccupancy(1),
            (int) ee.currentState().getOccupancy(2),
            (int) ee.currentState().getOccupancy(3),
            (int) ee.currentState().getOccupancy(4));

         */
        ObservableList<PopulationState> observableList = FXCollections.observableArrayList();
        for (int i = 0; i<=ee.currentState().size(); i++){
            observableList.add(ee.currentState());
        }

        ObservableList<PopulationState> occurrencesList = FXCollections.observableArrayList(
                ee.currentState(),
                ee.currentState(),
                ee.currentState(),
                ee.currentState(),
                ee.currentState());



        tableView.setItems(occurrencesList);
        agentsCol = new TableColumn<>("AGENTS");
        occurrencesCol = new TableColumn<>("OCCURENCES");


        //System.out.println(tableView.getItems().get(1).getOccupancy(1));
/*
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
                return  String.valueOf(param.getValue().getOccupancy());
                //return  String.valueOf(tableView.getItems().get(1).getOccupancy(1));
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        });


        occurrencesCol.getColumns().;
        occurrencesCol.setCellValueFactory(param -> new ObservableValue() {

            @Override
            public void addListener(ChangeListener listener) {

            }

            @Override
            public void removeListener(ChangeListener listener) {

            }

            @Override
            public Object getValue() {
                return param.getValue().getOccupancy(1);
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        });
*/


        occurrencesCol.setCellValueFactory(param -> new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super Integer> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super Integer> listener) {

            }

            @Override
            public Integer getValue() {
                return (int) param.getValue().getOccupancy(0);
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


    @FXML
    private void pieChartView() {
        double i0 = ee.currentState().getOccupancy(0);
        double i1 = ee.currentState().getOccupancy(1);
        double i2 = ee.currentState().getOccupancy(2);
        double i3 = ee.currentState().getOccupancy(3);
        double i4 = ee.currentState().getOccupancy(4);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Susceptible", i0),
                new PieChart.Data("Asintomatic", i1),
                new PieChart.Data("Grave", i2),
                new PieChart.Data("Recovered", i3),
                new PieChart.Data("Death", i4));
        piechartView.setData(pieChartData);
    }

    @FXML
    public void step(MouseEvent mouseEvent) {
        if (execute(stepBtn.getId(), ee)) {
            consoleArea.appendText("STEP\n");
            update();
        }
    }

    @FXML
    public void previous(MouseEvent mouseEvent) {
        if (execute(previousBtn.getId(), ee)) {
            consoleArea.appendText("PREVIUOS\n");
            update();
        }
    }

    @FXML
    private void restart(MouseEvent mouseEvent) {
        if (execute(restartBtn.getId(), ee)) {
            consoleArea.setText("RESTART\n");
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


}
