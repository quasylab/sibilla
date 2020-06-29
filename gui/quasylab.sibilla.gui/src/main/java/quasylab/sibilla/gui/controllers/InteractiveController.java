package quasylab.sibilla.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import quasilab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InteractiveController implements Initializable {

    @FXML private BorderPane root;

    //Table View
    @FXML private Tab tableviewTab;
    @FXML private TableView<?> tableView;
    //Area Chart
    @FXML private Tab areachartTab;
    @FXML private AreaChart<?, ?> areachartView;
    //Line Chart
    @FXML private Tab linechartTab;
    @FXML private LineChart<?, ?> linechartView;
    //Bar Chart
    @FXML private Tab barChartView;
    @FXML private BarChart<?, ?> barchartView;
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
    private ExecutionEnvironment<? extends State> ee;

    public void setExecutionEnvironment(ExecutionEnvironment<? extends State> ee){
        this.ee = ee;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    public void updateFields(){
        //this.modelTypeField.setText(String.valueOf(this.ee.getModel()));
        this.timeunitsField.setText(String.valueOf(this.ee.currentTime()));
        this.stepsField.setText(String.valueOf(this.ee.steps()));
        this.consoleArea.appendText(this.ee.currentState().toString()+"\n\n");
    }

    @FXML
    public void updateFields(ExecutionEnvironment<? extends State> ee){
        this.ee = ee;
        //this.modelTypeField.setText(String.valueOf(this.ee.getModel()));
        this.timeunitsField.setText(String.valueOf(this.ee.currentTime()));
        this.stepsField.setText(String.valueOf(this.ee.steps()));
        this.consoleArea.appendText(this.ee.currentState().toString()+"\n\n");
    }

    @FXML
    public void step(MouseEvent mouseEvent) {
        if (execute(stepBtn.getId(), ee)) {
            consoleArea.appendText("STEP\n");
            updateFields();
        }
    }


    @FXML
    public void previous(MouseEvent mouseEvent) {
        if (execute(previousBtn.getId(), ee)) {
            consoleArea.appendText("PREVIUOS\n");
            updateFields();
        }
    }

    @FXML
    private void restart(MouseEvent mouseEvent) {
        if (execute(restartBtn.getId(), ee)) {
            consoleArea.setText("RESTART\n");
            updateFields();
        }
    }

    @FXML
    public void openAdvanceSettings(MouseEvent mouseEvent) {
        loadWindow("/fxml/AdvanceSettings.fxml","Advance Settings");
    }


    private void loadWindow(String loc, String title){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(loc));
            Parent root = loader.load();
            AdvanceSettingsController controller = loader.getController();
            controller.setExecutionEnvironment(this.ee);
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
            case "q":
                return false;
            default:
                System.err.println("Unknown command "+btnId);
                System.err.println("Use n, p, r or q.");
        }
        return true;
    }

}
