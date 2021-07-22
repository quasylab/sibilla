package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.shell.SibillaShellInterpreter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SibillaJavaFXMainController implements Initializable {
    private static final String INIT_COMMAND = "init ";
    public static final String INFO = "INFO: ";
    public static final String CREATION_MESSAGE = "Simulation environment created";
    public static final String MODULE_MESSAGE = "Module %s has been successfully loaded\n";
    public static final String CODE_MESSAGE =  "Code\n%s\nhas been successfully loaded";
    public static final String PARAMETERS_MESSAGE =  "\n\ndeadline: %s\ndt: %s\nreplica: %s\n\nThe simulation has concluded with success!";
    public static final String SAVE_MESSAGE =  "save in\n%s\nOK!";
    public static final String CREATED_SUCCESSFULLY_MESSAGE =  "File %s created successfully";
    public static final String CREATED_ERROR_MESSAGE =  "File %s creation ERROR!";
    public static final String DELETED_FILE_MESSAGE =  "File %s successfully deleted";
    public static final String DEFAULT_LOAD_FILES_PATH = "shell/build/install/sshell/examples";
    public static final String DEFAULT_SAVE_FILES_PATH = "shell/build/install/sshell/bin/results";

    private SibillaShellInterpreter sibillaShellInterpreter;
    private ObservableList<Double> doubleNumberList = FXCollections.observableArrayList();
    private ObservableList<Integer> integerNumberList = FXCollections.observableArrayList();

    @FXML
    private TextArea rules;
    @FXML
    private MenuBar mainMenuBar;
    @FXML
    private TextField fileName;
    @FXML
    private TextArea monitor;
    @FXML
    private ChoiceBox<Integer> deadlineChoiceBox;
    @FXML
    private ChoiceBox<Integer> replicaChoiceBox;
    @FXML
    private ChoiceBox<Double> dtChoiceBox;
    @FXML
    private ListView<String> moduleList;

    @FXML
    private ListView<Path> filesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sibillaShellInterpreter = new SibillaShellInterpreter();

        for(int i=1; i<101; i++) integerNumberList.add(i);
        for(double i=0.5; i<100.5; i=i+0.5) doubleNumberList.add(i);
        deadlineChoiceBox.setItems(integerNumberList);
        replicaChoiceBox.setItems(integerNumberList);
        dtChoiceBox.setItems(doubleNumberList);

        moduleList.getItems().addAll(sibillaShellInterpreter.getRuntime().getModules());
        try {
            filesList.getItems().addAll(allFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }

        monitor.setStyle("-fx-text-inner-color: red;");
        monitor.setText(INFO+CREATION_MESSAGE);
    }

    @FXML
    public void moduleButtonPressed() {
        //TODO: DA INSERIRE MESSAGGIO DI ERRORE (modulo non selezionato!)
        try {
            sibillaShellInterpreter.getRuntime().loadModule(moduleList.getSelectionModel().getSelectedItem());
            monitor.setText(INFO+String.format(MODULE_MESSAGE, moduleList.getSelectionModel().getSelectedItem()));
            moduleList.getSelectionModel().clearSelection();
        } catch (CommandExecutionException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadButtonPressed() throws CommandExecutionException {
        sibillaShellInterpreter.getRuntime().load(filesList.getSelectionModel().getSelectedItem().toFile());
        monitor.setText(INFO+String.format(CODE_MESSAGE,filesList.getSelectionModel().getSelectedItem().toFile()));
        filesList.getSelectionModel().clearSelection();
    }

    @FXML
    public void simulateButtonPressed() {
        sibillaShellInterpreter.execute(INIT_COMMAND+"\"init\"");
        try {
            sibillaShellInterpreter.getRuntime().addAllMeasures();

            sibillaShellInterpreter.getRuntime().setDeadline(deadlineChoiceBox.getValue());
            sibillaShellInterpreter.getRuntime().setDt(dtChoiceBox.getValue());
            sibillaShellInterpreter.getRuntime().setReplica(replicaChoiceBox.getValue());


            sibillaShellInterpreter.getRuntime().simulate("");
            monitor.setText(INFO+String.format(PARAMETERS_MESSAGE,sibillaShellInterpreter.getRuntime().getDeadline(), sibillaShellInterpreter.getRuntime().getDt(), sibillaShellInterpreter.getRuntime().getReplica()));

        } catch (CommandExecutionException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void saveButtonPressed(){
        try {
            sibillaShellInterpreter.getRuntime().save(DEFAULT_SAVE_FILES_PATH, "sir", "__");
            monitor.setText(INFO+String.format(SAVE_MESSAGE,DEFAULT_SAVE_FILES_PATH));
        } catch (FileNotFoundException | CommandExecutionException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void deleteButtonPressed(){
        try {
            Files.deleteIfExists(filesList.getSelectionModel().getSelectedItem());
            monitor.setText(INFO+String.format(DELETED_FILE_MESSAGE,filesList.getSelectionModel().getSelectedItem().getFileName()));
            refreshFilesList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void createFileButtonPressed(){
        try {
            FileWriter fw = new FileWriter(DEFAULT_LOAD_FILES_PATH+"/"+fileName.getText());
            fw.write(rules.getText());
            fw.close();
            refreshFilesList();
            monitor.setText(INFO+String.format(CREATED_SUCCESSFULLY_MESSAGE, fileName.getText()));
            rules.clear();
            fileName.clear();
        } catch (IOException e) {
            e.printStackTrace();
            monitor.setText(INFO+String.format(CREATED_ERROR_MESSAGE, fileName.getText()));
        }
    }

    @FXML
    public void helpButtonPressed(){
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/sibillaHelpView.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            Stage window =  (Stage) mainMenuBar.getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Path> allFiles() throws IOException {
        Stream<Path> paths = Files.walk(Paths.get(DEFAULT_LOAD_FILES_PATH));
        return paths
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    private void refreshFilesList(){
        try {
            filesList.getItems().clear();
            filesList.getItems().addAll(allFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

