package quasylab.sibilla.gui.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import quasilab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.past.State;

import java.net.URL;
import java.util.ResourceBundle;

public class AdvanceSettingsController implements Initializable {


    @FXML
    InteractiveController ic;


    ExecutionEnvironment<?> ee;
    @FXML
    JFXButton stepToBtn;
    @FXML
    JFXButton previousToBtn;
    @FXML
    TextField stepToNumber;
    @FXML
    TextField previousToNumber;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setExecutionEnvironment(ExecutionEnvironment<? extends State> ee) {
        this.ee = ee;
    }

    @FXML
    public void stepTo(MouseEvent mouseEvent) {
        if (stepToNumber.getText().equals(""))
            showAlert();
        int stepN = Integer.parseInt(stepToNumber.getText());
        if (stepN == 0){}
        int i=0;
        while (i<stepN){
            this.ee.step();
            i++;
        }
        this.ic.updateFields();
    }

    @FXML
    public void previousTo (MouseEvent mouseEvent){
        if (previousToNumber.getText().equals(""))
            showAlert();

        ee.previous();
    }

    @FXML
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "You have not entered any value",
                ButtonType.OK);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.show();
    }

    @FXML
    private void checkEvent(MouseEvent me, MouseButton mb){
        if (me.getSource().equals(mb));
    }

}
