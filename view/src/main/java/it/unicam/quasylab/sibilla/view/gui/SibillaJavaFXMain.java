package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.view.controller.BasicSettings;
import it.unicam.quasylab.sibilla.view.controller.BasicSettingsLedger;
import it.unicam.quasylab.sibilla.view.controller.GUIController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static it.unicam.quasylab.sibilla.view.controller.GUIController.*;

public class SibillaJavaFXMain implements Initializable {

    private GUIController guiController;
    private SibillaJavaFXDocumentation documentation;
    private SibillaJavaFXNewFile newFileController;
    private SibillaJavaDirectoryView newDirectoryView;
    private SibillaJavaFXChartsView chartsViewController;

    private final Image folderIcon = new Image(getClass().getResourceAsStream("/view/icon/folder.png"));
    private final Image fileIcon = new Image(getClass().getResourceAsStream("/view/icon/file.png"));


    //main
    @FXML private VBox main;

    //File Menu
    @FXML private MenuBar mainMenuBar;
    @FXML private MenuItem newFileItem;
    @FXML private MenuItem saveItem;
    @FXML private MenuItem saveAsItem;
    @FXML private MenuItem closeAndSaveItem;
    @FXML private MenuItem closeItem;
    @FXML private MenuItem exitItem;

    //Edit Menu
    @FXML private MenuItem copyItem;
    @FXML private MenuItem deleteItem;

    //ToolBar
    @FXML private Button buildButton;
    @FXML private Button chartsButton;

    //Tree View Models
    @FXML private TreeView<File> treeViewModels;
    @FXML private Menu newMenuContext;
    @FXML private MenuItem buildContext;
    @FXML private MenuItem copyContext;
    @FXML private MenuItem deleteModContext;
    @FXML private MenuItem chartsContext;

    //Tree View Simulation Cases
    @FXML private TreeView<String> treeViewSimulationCases;
    @FXML private MenuItem simulateContext;
    @FXML private MenuItem deleteSimContext;

    //Central Monitor Tab Pane
    @FXML private TabPane monitorTabPane;

    //Settings Scroll Pane
    @FXML private ScrollPane settingsScrollPane;
    @FXML private GridPane measuresGridPane;
    @FXML private TextField settingsLabel;
    @FXML private ChoiceBox<String> settingsModule;
    @FXML private TableView<ParametersRawTableView> parametersTable;
    @FXML private TableColumn<ParametersRawTableView, String> parameterColumn;
    @FXML private TableColumn<ParametersRawTableView, String> valueColumn;
    @FXML private TextField deadline;
    @FXML private TextField dt;
    @FXML private TextField replica;
    private Map<CheckBox, String> measuresMap;

    //Bottom Component
    @FXML private TextArea infoTextArea;
    @FXML private TextArea terminal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        documentation = new SibillaJavaFXDocumentation();
        guiController = GUIController.getInstance();
        newFileController = new SibillaJavaFXNewFile();
        newDirectoryView = new SibillaJavaDirectoryView();
        chartsViewController = new SibillaJavaFXChartsView();

        this.measuresMap = new HashMap<>();

        settingsScrollPane.setDisable(true);

        main.addEventFilter(MouseEvent.ANY, event -> {
                    Tab monitorTab = monitorTabPane.getSelectionModel().getSelectedItem();
                    if(!treeViewModels.getSelectionModel().isEmpty()){
                        if (treeViewModels.getRoot().getValue().equals(treeViewModels.getSelectionModel().getSelectedItem().getValue())){ newFileItem.setDisable(true);
                        }else newFileItem.setDisable(false);
                    }else{newFileItem.setDisable(true); }
                    if (!monitorTabPane.getTabs().isEmpty()) {
                        if(guiController.isPmFile(monitorTab.getText())) {
                            buildButton.setDisable(false);
                            chartsButton.setDisable(true);
                        }else if(guiController.isCsvFile(monitorTab.getText())){
                            buildButton.setDisable(true);
                            chartsButton.setDisable(false);
                        }
                    } else{
                        buildButton.setDisable(true);
                        chartsButton.setDisable(true);
                    }
                }
        );

        treeViewModels.setCellFactory(new Callback<>() {
            public TreeCell<File> call(TreeView<File> tv) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((empty || item == null) ? "" : item.getName());
                        setGraphic((empty || item == null) ? null : new ImageView(fileIcon));
                        if(item!=null&&item.isDirectory()) setGraphic(new ImageView(folderIcon));
                            if (item!=null && treeViewModels.getRoot().getValue() != null) {
                                if (treeViewModels.getRoot().getValue().equals(item)) setText(item.getName() + " (" + item.getPath() + ")");
                            }
                       }};}});

        this.appendTextOnTerminal(this.guiController.getTimeString() + INFO + CREATION_MESSAGE);
        try {
            guiController.loadOpenedFile();
            this.guiController.loadSettings();
            if (guiController.isProjectLoaded()) {
                updateTreeViewModels(this.guiController.getOpenedProject(), null);
            } else disableItems();
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Loading Problems!\n"+e.getMessage());
        }
    }



    @FXML
    void buildButtonPressed(){
        Tab tab = monitorTabPane.getSelectionModel().getSelectedItem();
        if (tab.getText() != null) {
        try {
            for (Path path : this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath())) {
                if (path.toFile().getName().equals(tab.getText())) {
                    this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
                    if (guiController.isPmFile(path.toFile().getName())) {
                        this.guiController.build(path.toFile());
                        this.settingsScrollPane.setDisable(false);
                        this.updateTreeViewSimulationCases(this.guiController.getBuiltFile(), this.guiController.getSettings());
                        this.parametersTable.getItems().clear();
                        this.setParametersView();
                        this.infoTextArea.clear();
                        this.infoTextArea.appendText("-) File Built:          "+this.guiController.getBuiltFile().getName()+"\n\n");
                    }
                    this.appendTextOnTerminal(this.guiController.getTimeString() + INFO + String.format(CODE_MESSAGE, tab.getText()));
                }
            }
        } catch (IOException | CommandExecutionException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Building File Problems\n"+e.getMessage());
        }
    } else {
        this.appendTextOnTerminal("File not selected.\n\n" +
                "Select a file!");
    }
}

    @FXML
    public void chartsButtonPressed() {
        Tab tab = monitorTabPane.getSelectionModel().getSelectedItem();
        try {
            for (Path path : this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath())) {
                if (path.toFile().getName().equals(tab.getText())) {
                    chartsViewController.showTheStage(path.toFile());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Charts File Problems");
        }
    }


    @FXML
    public void modelsContextMenuShown(){
        if(!treeViewModels.getSelectionModel().getSelectedItems().isEmpty()){
            if(treeViewModels.getSelectionModel().getSelectedItem().getValue().isFile()) {
                if (guiController.isPmFile(treeViewModels.getSelectionModel().getSelectedItem().getValue().getName())) {
                   newMenuContext.setDisable(true);
                    buildContext.setDisable(false);
                    chartsContext.setDisable(true);
                    copyContext.setDisable(false);
                    deleteModContext.setDisable(false);
                    chartsContext.setDisable(true);
                }else if(guiController.isCsvFile(treeViewModels.getSelectionModel().getSelectedItem().getValue().getName())){
                    newMenuContext.setDisable(true);
                    buildContext.setDisable(true);
                    copyContext.setDisable(false);
                    deleteModContext.setDisable(false);
                    chartsContext.setDisable(false);
                }
            }else{
                newMenuContext.setDisable(false);
                buildContext.setDisable(true);
                chartsContext.setDisable(true);
                copyContext.setDisable(false);
                deleteModContext.setDisable(false);
            }
        }else{
            newMenuContext.setDisable(true);
            buildContext.setDisable(true);
            chartsContext.setDisable(true);
            copyContext.setDisable(true);
            deleteModContext.setDisable(true);
        }
    }

    @FXML
    public void simulationContextMenuShown(){
        if(!treeViewSimulationCases.getSelectionModel().getSelectedItems().isEmpty()){
               if(!treeViewSimulationCases.getRoot().getValue().equals(treeViewSimulationCases.getSelectionModel().getSelectedItem().getValue())){
                   simulateContext.setDisable(false);
                   deleteSimContext.setDisable(false);
               }
        }else{
            simulateContext.setDisable(true);
            deleteSimContext.setDisable(true);
        }
    }

    @FXML
    void deleteSimContextPressed() {
        if (!treeViewSimulationCases.getSelectionModel().getSelectedItem().getValue().isEmpty()) {
            String settingsLabel = treeViewSimulationCases.getSelectionModel().getSelectedItem().getValue();
            if(questionAlertMessage("Deleting","Are you sure you want to deleted " + settingsLabel + "?")){
                    try {
                        this.guiController.getSettings().getSettingsList().removeIf(settings -> settings.getLabel().equals(settingsLabel));
                        updateTreeViewSimulationCases(this.guiController.getBuiltFile(), this.guiController.getSettings());
                        this.guiController.saveSettings();
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.appendTextOnTerminal("ERROR: Delete Simulation Case Problems!");
                    }
                }
            }
        }


    @FXML
    void buildContextPressed() {
        try {
            this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Save Environment Problems!\n"+e.getMessage());
        }
        if (guiController.isPmFile(treeViewModels.getSelectionModel().getSelectedItem().getValue().getName())) {
            try {
                this.guiController.build(treeViewModels.getSelectionModel().getSelectedItem().getValue());
                this.settingsScrollPane.setDisable(false);
                this.updateTreeViewSimulationCases(this.guiController.getBuiltFile(), this.guiController.getSettings());
                this.parametersTable.getItems().clear();
                this.setParametersView();
                this.infoTextArea.clear();
                this.infoTextArea.appendText("-) File Built:          "+this.guiController.getBuiltFile().getName()+"\n\n");
            } catch (CommandExecutionException | IOException e) {
                e.printStackTrace();
                this.appendTextOnTerminal("ERROR: Building File Problems!\n"+e.getMessage());

            }
        }
        this.appendTextOnTerminal(this.guiController.getTimeString() + INFO + String.format(CODE_MESSAGE, treeViewModels.getSelectionModel().getSelectedItem().getValue().getName()));
    }




    @FXML
    void chartsContextPressed() {
        chartsViewController.showTheStage(treeViewModels.getSelectionModel().getSelectedItem().getValue());
    }

    @FXML
    void closeAndSaveItemPressed() {
        this.saveItemPressed();
        this.closeItemPressed();
    }

    @FXML
    void closeItemPressed() {
        String projectName = this.guiController.getOpenedProject().getName();
        this.guiController.closeProject();
        this.clearAllSettings();
        this.settingsScrollPane.setDisable(true);
        this.infoTextArea.clear();
        treeViewModels.setRoot(null);
        treeViewSimulationCases.setRoot(null);
        monitorTabPane.getTabs().clear();
        this.clearAllSettings();
        this.appendTextOnTerminal(this.guiController.getTimeString() + INFO + String.format(CLOSE_PROJECT, projectName));
        disableItems();
    }



    @FXML
    void copyItemPressed() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Copy");
        Stage window = new Stage();
        fileChooser.setInitialFileName(treeViewModels.getSelectionModel().getSelectedItem().getValue().getName());
        File dir = fileChooser.showSaveDialog(window);
        try {
            if(treeViewModels.getSelectionModel().getSelectedItem().getValue().isDirectory()){
                FileUtils.copyDirectory(treeViewModels.getSelectionModel().getSelectedItem().getValue(), dir);
            }else {
                FileUtils.copyFile(treeViewModels.getSelectionModel().getSelectedItem().getValue(),dir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Coping File Problems\n"+e.getMessage());
        }
    }

    @FXML
    void deleteItemPressed() {
        if(!treeViewModels.getSelectionModel().isEmpty()) {
            File file = treeViewModels.getSelectionModel().getSelectedItem().getValue();
            if(questionAlertMessage("Deleting", "Are you sure you want to deleted " + file.getName() + "?" )){
                    monitorTabPane.getTabs().removeIf(tab -> tab.getText().equals(file.getName()));
                    FileUtils.deleteQuietly(file);
                    if(this.guiController.getBuiltFile()!=null&&!this.guiController.getBuiltFile().exists()){
                            this.treeViewSimulationCases.setRoot(null);
                            this.clearAllSettings();
                            this.settingsScrollPane.setDisable(true);
                            this.infoTextArea.clear();
                    }
                    this.guiController.getModifiedFile().remove(file);
                    this.guiController.getReadOnlyFile().remove(file);
                    this.updateTreeViewModels(this.guiController.getOpenedProject(),null);
                    this.appendTextOnTerminal(INFO + String.format(DELETED_FILE_MESSAGE, file.getName()));
            }
        }
    }

    @FXML
    public void doubleClickModelsPressed() {
        treeViewModels.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && click.getButton().equals(MouseButton.PRIMARY)) {
                if(!treeViewModels.getSelectionModel().isEmpty()) {
                    File file = treeViewModels.getSelectionModel().getSelectedItem().getValue();

                    if (guiController.isPmFile(file.getName()) || guiController.isSibFile(file.getName())) {
                        if (!this.guiController.getModifiedFile().containsKey(file) && file.isFile()) {//!modifiedFile.containsKey(file)
                            addMonitorTab(file);
                        }
                    }else if(guiController.isCsvFile(file.getName())){
                        if (!this.guiController.getReadOnlyFile().containsKey(file) && file.isFile()) {
                            addReadOnlyTab(file);
                        }
                    }
                }
            }
        });
    }



    @FXML
    public void doubleClickCasesPressed() {
        treeViewSimulationCases.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 && click.getButton().equals(MouseButton.PRIMARY)) {
                if(!treeViewSimulationCases.getSelectionModel().isEmpty()) {
                    for (BasicSettings settings : this.guiController.getSettings().getSettingsList()) {
                        if (settings.getLabel().equals(treeViewSimulationCases.getSelectionModel().getSelectedItem().getValue())) {
                            this.settingsLabel.setText(settings.getLabel());
                            this.settingsModule.setValue(settings.getModule());
                            this.deadline.setText(String.valueOf(settings.getDeadline()));
                            this.replica.setText(String.valueOf(settings.getReplica()));
                            this.dt.setText(String.valueOf(settings.getDt()));
                            for(CheckBox checkBox:measuresMap.keySet()){
                               if(settings.getMeasures().get(measuresMap.get(checkBox)).equals(true)){
                                   checkBox.setSelected(true);
                               }else checkBox.setSelected(false);
                            }
                        }
                    }
                }

                }
            }
        );
    }

    private void addMonitorTab(File file) {
        TextArea monitor = new TextArea();
        monitor.setFont(Font.font("Verdana", 14));
        Tab tab = new Tab(file.getName(), monitor);


        tab.setOnClosed(event -> {
                    this.guiController.getModifiedFile().get(file).clear();
                    this.guiController.getModifiedFile().remove(file);
                }
        );
        monitorTabPane.getTabs().add(tab);
        monitorTabPane.getSelectionModel().select(tab);
        this.guiController.getModifiedFile().put(file, monitor);
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                this.guiController.getModifiedFile().get(file).appendText(sc.nextLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: File Not Found Exception!\n"+e.getMessage());
        }
    }


    private void addReadOnlyTab(File file) {
        TableView<TableViewRow> tableView = new TableView<>();
        TableColumn<TableViewRow, String> value = new TableColumn<>("");
        TableColumn<TableViewRow, String> mean = new TableColumn<>("Mean");
        TableColumn<TableViewRow, String> variance = new TableColumn<>("Variance");
        TableColumn<TableViewRow, String> standardDeviation = new TableColumn<>("Standard Deviation");

        value.setMaxWidth(600);
        value.setStyle("-fx-font-weight: bold");
        mean.setStyle( "-fx-alignment: CENTER");
        variance.setStyle( "-fx-alignment: CENTER");
        standardDeviation.setStyle( "-fx-alignment: CENTER");
        ObservableList<TableViewRow> data = FXCollections.observableArrayList();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setSortPolicy(param -> false);
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String a = sc.nextLine();
                String[] b = a.split(";");
                data.add(new TableViewRow(b[0], b[1], b[2], b[3]));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: File Not Found Exception!\n"+e.getMessage());
        }
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        mean.setCellValueFactory(new PropertyValueFactory<>("mean"));
        variance.setCellValueFactory(new PropertyValueFactory<>("variance"));
        standardDeviation.setCellValueFactory(new PropertyValueFactory<>("standardDeviation"));

        tableView.getItems().addAll(data);
        tableView.getColumns().add(value);
        tableView.getColumns().add(mean);
        tableView.getColumns().add(variance);
        tableView.getColumns().add(standardDeviation);
        Tab tab = new Tab(file.getName(), tableView);
        tab.setOnClosed(event -> this.guiController.getReadOnlyFile().remove(file));
        monitorTabPane.getTabs().add(tab);
        monitorTabPane.getSelectionModel().select(tab);
        this.guiController.getReadOnlyFile().put(file, tableView);
    }

    @FXML
    void editMenuPressed() {

    }

    @FXML
    void exitItemPressed() {
        Stage window = (Stage) mainMenuBar.getScene().getWindow();
        try {
            if(questionAlertMessage("Quit", "Are you sure you want to quit?")){
                window.close();
                this.guiController.saveOpenedProject();
                this.guiController.closeProject();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Exit Failed!\n"+e.getMessage());
        }
    }

    @FXML
    void helpItemPressed() {
        documentation.showHelpFile();
    }

    @FXML
    void newDirectoryItemPressed() {
        if(!treeViewModels.getSelectionModel().isEmpty()) {
            if (treeViewModels.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
                newDirectoryView.showTheStage(this.treeViewModels.getSelectionModel().getSelectedItem().getValue());
            } else
                newDirectoryView.showTheStage(this.treeViewModels.getSelectionModel().getSelectedItem().getValue().getParentFile());
        }else newDirectoryView.showTheStage(this.treeViewModels.getRoot().getValue());
        updateTreeViewModels(this.guiController.getOpenedProject(), null);
    }

    @FXML
    void newFileItemPressed() {
        if(treeViewModels.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            newFileController.showTheStage(this.treeViewModels.getSelectionModel().getSelectedItem().getValue());
        }else newFileController.showTheStage(this.treeViewModels.getSelectionModel().getSelectedItem().getValue().getParentFile());
        updateTreeViewModels(this.guiController.getOpenedProject(), null);
    }

    @FXML
    void newProjectItemPressed() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            Stage window = new Stage();
            File dir = fileChooser.showSaveDialog(window);
            this.guiController.createNewDirectory(dir);
            newDirectoryView.showTheStage(dir);
            updateTreeViewModels(dir,null);
            this.guiController.setOpenedProject(dir);
            monitorTabPane.getTabs().clear();
            if (this.guiController.isProjectLoaded()) {
                enableItems();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Creation Of a New Project Failed\n"+e.getMessage());
        }
        }

    @FXML
    void openItemPressed() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open project");
        Stage directoryStage = new Stage();
        File file = directoryChooser.showDialog(directoryStage);
        if (file != null) {
                this.guiController.getModifiedFile().clear();
                this.guiController.getReadOnlyFile().clear();
                monitorTabPane.getTabs().clear();
                this.guiController.setOpenedProject(file);
            updateTreeViewModels(file, null);
            this.treeViewSimulationCases.setRoot(null);
            enableItems();
            } else {
                this.appendTextOnTerminal("ERROR: There are too many .sib files or .sib file not exist");
                this.appendTextOnTerminal("ERROR: Open Failed!");
            }
    }

    @FXML
    void saveAsItemPressed() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        Stage window = new Stage();
        fileChooser.setInitialFileName(this.guiController.getOpenedProject().getName());
        File dir = fileChooser.showSaveDialog(window);
        try {
            this.guiController.saveAs(dir);
            updateTreeViewModels(dir, null);
            List<BasicSettings> newSettings = new ArrayList<>();
            for(BasicSettings settings:this.guiController.getSettings().getSettingsList()){
                for(Path path:this.guiController.getAllFiles(dir.getPath())){
                    if (settings.getFile().getName().equals(path.toFile().getName())){
                        BasicSettings nSettings = new BasicSettings(path.toFile(),settings.getLabel(),settings.getModule());
                        nSettings.setParameters(settings.getParameters());
                        nSettings.setMeasures(settings.getMeasures());
                        nSettings.setDeadline(nSettings.getDeadline());
                        nSettings.setDt(nSettings.getDt());
                        nSettings.setReplica(nSettings.getReplica());
                        newSettings.add(nSettings);
                    }
                }
                }
            this.guiController.getSettings().getSettingsList().addAll(newSettings);
            monitorTabPane.getTabs().clear();
            this.guiController.setOpenedProject(dir);
            if(this.guiController.isFileBuilt()) {
                String fileBuilt = this.guiController.getBuiltFile().getName();
                for (Path path : this.guiController.getAllFiles(dir.getPath())) {
                    if (fileBuilt.equals(path.toFile().getName())) {
                        this.guiController.build(path.toFile());
                        break;
                    }
                }
            }
            updateTreeViewSimulationCases(this.guiController.getBuiltFile(), this.guiController.getSettings());
            this.monitorTabPane.getTabs().clear();
        } catch (IOException | CommandExecutionException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Save Failed\n"+e.getMessage());
        }
    }

    @FXML
    void saveItemPressed() {
        try {
            this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
        } catch (IOException e) {
            e.printStackTrace();
            this.appendTextOnTerminal("ERROR: Save Failed\n"+e.getMessage());
        }
    }

    @FXML
    public void saveSettingsButtonPressed(){
        if(checkSettings()) {
            try {
                if (this.guiController.getSettings().getSettingsList().stream().noneMatch(p -> p.getLabel().equals(getSettings().getLabel()))) {
                    saveSettings();
                } else {
                    if (this.overwriteAlertMessage("Overwrite Results", "Are you sure you want to overwrite the simulation settings?", "Then change the label name")) {
                        saveSettings();
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                this.appendTextOnTerminal("ERROR: Save Failed\n"+e.getMessage());
            }
        }
    }

             private void saveSettings() throws IOException {
                 this.guiController.getSettings().getSettingsList().removeIf(settings -> settings.getLabel().equals(getSettings().getLabel()));
                 this.guiController.getSettings().getSettingsList().add(getSettings());
                     updateTreeViewSimulationCases(this.guiController.getBuiltFile(), this.guiController.getSettings());
                     this.guiController.saveSettings();
             }

    @FXML
    public void simulateButtonPressed() {
        if(checkSettings()){
            try {
                if (this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath()).stream().anyMatch(p -> p.toFile().getName().equals("results ("+this.getSettings().getLabel()+")"))){
                    if(this.overwriteAlertMessage("Overwrite Results", "Are you sure you want to overwrite the results?", "Then change the label name")){
                        this.simulate();
                    }
                }else this.simulate();
            } catch (IOException | CommandExecutionException e) {
                e.printStackTrace();
                this.appendTextOnTerminal("ERROR: Simulation Failed\n" + e.getMessage());
            }
        }
         }

         private void simulate() throws IOException, CommandExecutionException {
                 this.guiController.simulate(Objects.requireNonNull(getSettings()));
                 this.infoTextArea.appendText("-) Module Loaded:       " + this.guiController.getLastSimulatedSettings().getModule() + "\n\n");
                 this.infoTextArea.appendText("-) File Loaded:          " + this.guiController.getBuiltFile().getName() + "\n\n");
                 this.infoTextArea.appendText("-) Parameters Loaded:   " + this.guiController.getLastSimulatedSettings().getParameters() + "\n\n");
                 this.infoTextArea.appendText("-) Deadline Value:      " + this.guiController.getLastSimulatedSettings().getDeadline() + "\n\n");
                 this.infoTextArea.appendText("-) Dt Value:            " + this.guiController.getLastSimulatedSettings().getDt() + "\n\n");
                 this.infoTextArea.appendText("-) Replica Value:       " + this.guiController.getLastSimulatedSettings().getReplica() + "\n\n");
                 this.infoTextArea.appendText("-) MeasuresValue:       " + this.guiController.getLastSimulatedSettings().getMeasures() + "\n\n");
                 if (this.guiController.getAllFiles(this.guiController.getLastSimulatedSettings().getFile().getParent()).stream().noneMatch(p -> p.toFile().getName().equals("results (" + this.getSettings().getLabel() + ")"))) {
                     Path results = Paths.get(this.guiController.getLastSimulatedSettings().getFile().getParent() + "/results (" + this.getSettings().getLabel() + ")");
                     Files.createDirectory(results);
                 }else{
                     for(Path path:this.guiController.getAllFiles(this.guiController.getLastSimulatedSettings().getFile().getParent() + "/results (" + this.getSettings().getLabel() + ")")){
                         if(path.toFile().isFile()) Files.delete(path);
                     }
                 }
                 this.guiController.getSibillaRuntime().save(this.guiController.getLastSimulatedSettings().getFile().getParent() + "/results (" + this.getSettings().getLabel() + ")", this.getSettings().getLabel(), "__");
                 updateTreeViewModels(this.guiController.getOpenedProject(), null);
                 this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
         }

    @FXML
    public void clearSettingsButtonPressed(){
        this.guiController.getSibillaRuntime().clear();
        this.settingsLabel.clear();
        this.settingsModule.getSelectionModel().clearSelection();
        this.deadline.clear();
        this.dt.clear();
        this.replica.clear();
        for(CheckBox checkBox:this.measuresMap.keySet()) checkBox.setSelected(false);
    }


    public void clearAllSettings(){
       this.guiController.getSibillaRuntime().clear();
       this.clearSettingsButtonPressed();
       parametersTable.getItems().clear();
       measuresMap.clear();
    }



    private BasicSettings getSettings() {
        BasicSettings settings = new BasicSettings(this.guiController.getBuiltFile(), settingsLabel.getText(), settingsModule.getValue());
            settings.setDeadline(Double.parseDouble(deadline.getText()));
            settings.setReplica(Integer.parseInt(replica.getText()));
            settings.setDt(Double.parseDouble(dt.getText()));
            for (String key : this.guiController.getSibillaRuntime().getParameters()) {
                settings.getParameters().put(key, this.guiController.getSibillaRuntime().getParameter(key));
            }
            for (CheckBox checkBox : measuresMap.keySet()) {
                if (checkBox.isSelected()) {
                    settings.getMeasures().put(checkBox.getText(), true);
                } else settings.getMeasures().put(checkBox.getText(), false);
            }
            return settings;
    }


    private void disableItems() {
        buildButton.setDisable(true);
        closeItem.setDisable(true);
        saveItem.setDisable(true);
        closeAndSaveItem.setDisable(true);
        saveAsItem.setDisable(true);
        newFileItem.setDisable(true);
    }

    private void enableItems() {
        saveItem.setDisable(false);
        saveAsItem.setDisable(false);
        closeAndSaveItem.setDisable(false);
        closeItem.setDisable(false);
        newFileItem.setDisable(false);
    }

    public static class TableViewRow{
        private final SimpleStringProperty value;
        private final SimpleStringProperty mean;
        private final SimpleStringProperty variance;
        private final SimpleStringProperty standardDeviation;

        private TableViewRow(String value, String mean, String variance, String standardDeviation) {
            this.value = new SimpleStringProperty(value);
            this.mean = new SimpleStringProperty(mean);
            this.variance = new SimpleStringProperty(variance);
            this.standardDeviation = new SimpleStringProperty(standardDeviation);
        }
        public String getValue() {
            return value.get();
        }
        public void setValue(String value) {
            this.value.set(value);
        }

        public String getMean() {
            return mean.get();
        }
        public void setMean(String mean) {
            this.mean.set(mean);
        }

        public String getVariance() {
            return variance.get();
        }
        public void setVariance(String variance) {
            this.variance.set(variance);
        }

        public String getStandardDeviation() {
            return standardDeviation.get();
        }
        public void setStandardDeviation(String standardDeviation) {
            this.standardDeviation.set(standardDeviation);
        }
    }


    public static class ParametersRawTableView{
        private final SimpleStringProperty parameter;
        private final SimpleStringProperty value;

        private ParametersRawTableView(String parameter, String value) {
            this.parameter=new SimpleStringProperty(parameter);
            this.value = new SimpleStringProperty(value);
        }
        public String getParameter() {
            return parameter.get();
        }
        public void setParameter(String parameter) {
            this.parameter.set(parameter);
        }

        public String getValue() {
            return value.get();
        }
        public void setValue(String value) {
            this.value.set(value);
        }
    }

    private void appendTextOnTerminal(String text){
        this.terminal.appendText(text+"\n");
    }

    private void updateTreeViewModels(File dir, TreeItem<File> parent) {
        TreeItem<File> root = new TreeItem<>(dir);
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                updateTreeViewModels(file, root);
            } else if(this.guiController.isPmFile(file.getName())||this.guiController.isCsvFile(file.getName())){
                root.getChildren().add(new TreeItem<>(file));
            }
        }
        if (parent == null) {
            treeViewModels.setRoot(root);
            root.setExpanded(true);
        } else {
            parent.getChildren().add(root);
        }
    }


    private void updateTreeViewSimulationCases(File dir, BasicSettingsLedger settingsLedger) throws IOException {
        TreeItem<String> root = new TreeItem<>(dir.getName(), new ImageView(folderIcon));
            for (BasicSettings settings : settingsLedger.getSettingsList()) {
                if (dir.getPath().equals(settings.getFilePath())) {
                    root.getChildren().add(new TreeItem<>(settings.getLabel(), new ImageView(fileIcon)));
                }
            }
        this.treeViewSimulationCases.setRoot(root);
        root.setExpanded(true);
    }

    private void setParametersView(){
        if(this.guiController.isFileBuilt()){
            //module settings
         ObservableList<String> parametersModuleData = FXCollections.observableArrayList();
         parametersModuleData.addAll(Arrays.asList(this.guiController.getSibillaRuntime().getModules()));
         settingsModule.setItems(parametersModuleData);

         //parameters table settings
        ObservableList<ParametersRawTableView> parametersTableData = FXCollections.observableArrayList();
        for(String key:this.guiController.getSibillaRuntime().getParameters()) {
            parametersTableData.add(new ParametersRawTableView(key, String.valueOf(this.guiController.getSibillaRuntime().getParameter(key))));
        }
        parameterColumn.setCellValueFactory(new PropertyValueFactory<>("parameter"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        parametersTable.getItems().addAll(parametersTableData);

        //measures settings
        measuresMap.clear();
        CheckBox all = new CheckBox("Select All");
        measuresGridPane.add(all,0,1);
        for(int i=0;i<this.guiController.getSibillaRuntime().getMeasures().length;i++){
            CheckBox checkBox = new CheckBox(this.guiController.getSibillaRuntime().getMeasures()[i]);
            measuresGridPane.add(checkBox,0,i+2);
            measuresMap.put(checkBox, this.guiController.getSibillaRuntime().getMeasures()[i]);
        }

            all.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if(all.isSelected()){
                    for(CheckBox key:measuresMap.keySet()){
                        key.setSelected(true);
                    }
                }else {
                    for(CheckBox key:measuresMap.keySet()){
                        key.setSelected(false);
                    }
                }
            });

        }
    }

    public boolean checkSettings(){
        if(!this.settingsLabel.getText().isEmpty()) {
            if (!this.settingsModule.getValue().isEmpty()) {
                if (!deadline.getText().isEmpty()) {
                    if (!dt.getText().isEmpty()) {
                        if (!replica.getText().isEmpty()) {
                            if (measuresMap.keySet().stream().anyMatch(CheckBox::isSelected)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean questionAlertMessage(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle(title);
        Optional<ButtonType> answer = alert.showAndWait();
            if (answer.isPresent()&&answer.get() == ButtonType.YES) {
                return true;
            }
            return false;
    }

    private boolean overwriteAlertMessage(String title, String yesMessage, String noMessage){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, yesMessage, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle(title);
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isPresent()&&answer.get() == ButtonType.YES) {
            return true;
        }else if(answer.isPresent()&&answer.get() == ButtonType.NO){
            Alert alert2 = new Alert(Alert.AlertType.INFORMATION, noMessage, ButtonType.OK);
            alert2.showAndWait();
        }
        return false;
    }
}
