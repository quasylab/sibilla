package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;


import it.unicam.quasylab.sibilla.view.controller.GUIController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static it.unicam.quasylab.sibilla.view.controller.GUIController.*;

public class SibillaJavaFXMain implements Initializable {
    private JavaFXSecurityQuestion securityQuestion;
    private GUIController guiController;

    private SibillaJavaFXChooseTheModule chooseTheModule;
    private SibillaJavaFXSetTheParameters setTheParameters;
    private SibillaJavaFXNewFile newFileController;
    private SibillaJavaFXChartsView chartsViewController;

    private final Image folderIcon = new Image(getClass().getResourceAsStream("/view/icon/folder.png"));
    private final Image fileIcon = new Image(getClass().getResourceAsStream("/view/icon/file.png"));
    private final Image saveIcon = new Image(getClass().getResourceAsStream("/view/icon/saveAll.png"));
    private final Image saveAsIcon = new Image(getClass().getResourceAsStream("/view/icon/saveAs.png"));
    private final Image closeProjectIcon = new Image(getClass().getResourceAsStream("/view/icon/closeProject.png"));
    private final Image closeAndSaveProjectIcon = new Image(getClass().getResourceAsStream("/view/icon/closeAndSave.png"));
    private final Image openProjectIcon = new Image(getClass().getResourceAsStream("/view/icon/openProjectIcon.png"));
    private final Image exitIcon = new Image(getClass().getResourceAsStream("/view/icon/exitIcon.png"));
    private final Image newProjectIcon = new Image(getClass().getResourceAsStream("/view/icon/newProject.png"));
    private final Image newFileIcon = new Image(getClass().getResourceAsStream("/view/icon/newFile.png"));

    @FXML
    private MenuBar mainMenuBar;

    @FXML
    private TextArea terminal;

    @FXML
    private MenuItem newProjectItem;

    @FXML
    private MenuItem openItem;

    @FXML
    private MenuItem closeItem;

    @FXML
    private MenuItem newFileItem;

    @FXML
    private MenuItem exitItem;

    @FXML
    private MenuItem saveItem;

    @FXML
    private MenuItem saveAsItem;

    @FXML
    private MenuItem closeAndSaveItem;

    @FXML
    private MenuItem deleteItem;

    @FXML
    private TreeView<File> treeViewProject;

    @FXML
    private TabPane monitorTabPane;

    @FXML
    private TabPane readOnlyTabPane;

    @FXML
    private Button simulate;

    @FXML
    private Button buildButton;

    @FXML
    private Button chartsButton;

    @FXML
    private VBox main;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chooseTheModule = new SibillaJavaFXChooseTheModule();
        setTheParameters = new SibillaJavaFXSetTheParameters();
        newFileController = new SibillaJavaFXNewFile();
        securityQuestion = new JavaFXSecurityQuestion();
        chartsViewController = new SibillaJavaFXChartsView();
        guiController = GUIController.getInstance();

        main.addEventFilter(MouseEvent.ANY, event -> {
                    Tab monitorTab = monitorTabPane.getSelectionModel().getSelectedItem();
                    if (!monitorTabPane.getTabs().isEmpty() && guiController.isProjectLoaded()) {
                        if (monitorTab.getText().endsWith(".pm") && guiController.isFileLoaded()) {
                            simulate.setDisable(false);
                        } else simulate.setDisable(true);
                    } else simulate.setDisable(true);

                    if (!readOnlyTabPane.getTabs().isEmpty()) {
                        chartsButton.setDisable(false);
                    } else chartsButton.setDisable(true);

                    if (!monitorTabPane.getTabs().isEmpty() && (monitorTab.getText().endsWith(".pm") || monitorTab.getText().endsWith(".sib"))) {
                        buildButton.setDisable(false);
                    } else buildButton.setDisable(true);
                }
        );

        ImageView icon = new ImageView(saveIcon);
        saveItem.setGraphic(icon);

        icon = new ImageView(saveAsIcon);
        saveAsItem.setGraphic(icon);

        icon = new ImageView(closeProjectIcon);
        closeItem.setGraphic(icon);

        icon = new ImageView(closeAndSaveProjectIcon);
        closeAndSaveItem.setGraphic(icon);

        icon = new ImageView(openProjectIcon);
        openItem.setGraphic(icon);


        icon = new ImageView(exitIcon);
        exitItem.setGraphic(icon);

        icon = new ImageView(newFileIcon);
        newFileItem.setGraphic(icon);

        icon = new ImageView(newProjectIcon);
        newProjectItem.setGraphic(icon);


        terminal.setStyle("-fx-text-inner-color: red;");
        terminal.setText(this.guiController.getTimeString() + INFO + CREATION_MESSAGE);
        try {
            guiController.loadOpenedFile();
            if (guiController.isProjectLoaded()) {
                updateTreeViewProject(this.guiController.getOpenedProject(), null);
                this.guiController.loadModule(guiController.getOpenedProject());
                terminal.appendText(this.guiController.getTimeString() + INFO + String.format(MODULE_MESSAGE, guiController.getLoadModule()));
            } else disableItems();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void buildButtonPressed() throws CommandExecutionException {

        Tab tab = monitorTabPane.getSelectionModel().getSelectedItem();
        if (tab.getText() != null) {
            try {
                for (Path path : this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath())) {//allFiles(this.guiController.getOpenedProject().getPath())){//this.openedFile.getPath())){
                    if (path.toFile().getName().equals(tab.getText())) {
                        this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
                        // sibillaShellInterpreter.getRuntime().load(path.toFile());
                        if (path.toFile().getName().endsWith(".pm")) {
                            this.guiController.loadFile(path.toFile());
                        } else if (path.toFile().getName().endsWith(".sib")) {
                            this.guiController.executeFile(path.toFile().getPath());
                        }
                        // appendSibFile("load \""+path.toFile().getPath()+"\"");


                        // this.loadingFile=true;
                        terminal.appendText(this.guiController.getTimeString() + INFO + String.format(CODE_MESSAGE, tab.getText()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            terminal.setText("File not selected.\n\n" +
                    "Select a file!");
        }
    }

    @FXML
    public void simulateButtonPressed() {
        this.guiController.getSibillaShellInterpreter().execute(INIT_COMMAND + "\"init\"");
        this.guiController.getSibillaShellInterpreter().getRuntime().addAllMeasures();
        setTheParameters.showTheStage();
        if (!Double.isNaN(this.guiController.getSibillaShellInterpreter().getRuntime().getDeadline()) && !Double.isNaN(this.guiController.getSibillaShellInterpreter().getRuntime().getDt())) {
            try {
                this.guiController.getSibillaShellInterpreter().getRuntime().simulate("");
                //this.readOnlyFile.clear();
                this.guiController.getReadOnlyFile().clear();
                this.readOnlyTabPane.getTabs().clear();
                terminal.appendText(this.guiController.getTimeString() + INFO + String.format(PARAMETERS_MESSAGE, this.guiController.getSibillaShellInterpreter().getRuntime().getDeadline(), this.guiController.getSibillaShellInterpreter().getRuntime().getDt(), this.guiController.getSibillaShellInterpreter().getRuntime().getReplica()));
            } catch (CommandExecutionException e) {
                e.printStackTrace();
            }
        }
        try {//this.openedFile.getPath()
            if (this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath()).stream().filter(p -> p.toFile().getName().equals("results (" + this.monitorTabPane.getSelectionModel().getSelectedItem().getText() + ")")).count() != 1) {
                Path results = Paths.get(this.guiController.getOpenedProject() + "/results (" + this.monitorTabPane.getSelectionModel().getSelectedItem().getText() + ")");
                Files.createDirectory(results);
            }
            this.guiController.getSibillaShellInterpreter().getRuntime().save(this.guiController.getOpenedProject().getPath() + "/results (" + this.monitorTabPane.getSelectionModel().getSelectedItem().getText() + ")", this.monitorTabPane.getSelectionModel().getSelectedItem().getText(), "__");
            updateTreeViewProject(this.guiController.getOpenedProject(), null);

            this.guiController.overrideSibFile();


            this.guiController.refreshSibTextArea();
            this.guiController.saveAll(this.guiController.getOpenedProject().getPath());

        } catch (IOException | CommandExecutionException e) {
            e.printStackTrace();
        }


    }


    @FXML
    public void chartsButtonPressed() {
        Tab tab = readOnlyTabPane.getSelectionModel().getSelectedItem();
        try {
            for (Path path : this.guiController.getAllFiles(this.guiController.getOpenedProject().getPath())) {
                if (path.toFile().getName().equals(tab.getText())) {
                    chartsViewController.showTheStage(path.toFile());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void helpItemPressed() {
        try {
            Parent tableViewParent = FXMLLoader.load(getClass().getResource("/view/fxml/sibillaHelpView.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            Stage window = (Stage) mainMenuBar.getScene().getWindow();
            window.setScene(tableViewScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openItemPressed() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open project");
        Stage directoryStage = new Stage();
        File file = directoryChooser.showDialog(directoryStage);
        if (file != null) {
            String moduleLoaded = guiController.loadModule(file);
            if (moduleLoaded != null) {
                this.guiController.setOpenedProject(file);
                updateTreeViewProject(file, null);
                monitorTabPane.getTabs().clear();
                enableItems();
                terminal.appendText(this.guiController.getTimeString() + INFO + String.format(MODULE_MESSAGE, moduleLoaded));
            } else {
                terminal.appendText("ERROR: There are too many .sib files or .sib file not exist\n");
                terminal.appendText("ERROR: Open Failed!\n");
            }
        }
    }

    @FXML
    public void saveAsItemPressed() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        Stage window = new Stage();
        fileChooser.setInitialFileName(this.guiController.getOpenedProject().getName());
        File dir = fileChooser.showSaveDialog(window);
        try {
            this.guiController.saveAs(dir);
            updateTreeViewProject(dir, null);
            this.guiController.setOpenedProject(dir);
            this.monitorTabPane.getTabs().clear();
            this.readOnlyTabPane.getTabs().clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void exitItemPressed() {
        Stage window = (Stage) mainMenuBar.getScene().getWindow();
        try {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to quit?",
                    ButtonType.YES,
                    ButtonType.NO);
            Optional<ButtonType> confirm = a.showAndWait();
            if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
                window.close();
                this.guiController.saveOpenedProject();
                this.guiController.closeProject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void newProjectItemPressed() {
        chooseTheModule.showTheStage();
        if (chooseTheModule.getChosenModule() != null) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save");
                Stage window = new Stage();
                File dir = fileChooser.showSaveDialog(window);
                this.guiController.createNewProject(dir, chooseTheModule.getChosenModule());
                this.guiController.setOpenedProject(dir);
                updateTreeViewProject(this.guiController.getOpenedProject(), null);
                monitorTabPane.getTabs().clear();
                if (this.guiController.isProjectLoaded()) {
                    enableItems();
                }
                this.guiController.loadModule(chooseTheModule.getChosenModule());
                this.terminal.appendText(this.guiController.getTimeString() + INFO + String.format(MODULE_MESSAGE, chooseTheModule.getChosenModule()));
            } catch (IOException | CommandExecutionException e) {
                e.printStackTrace();
            }

        }
    }


    @FXML
    public void newFileItemPressed() {
        newFileController.showTheStage(this.guiController.getOpenedProject());
        updateTreeViewProject(this.guiController.getOpenedProject(), null);
    }


    @FXML
    public void doubleClickPressed() {
        treeViewProject.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                File file = treeViewProject.getSelectionModel().getSelectedItem().getValue();

                if (file.getName().endsWith(".pm") || file.getName().endsWith(".sib")) {
                    if (!this.guiController.getModifiedFile().containsKey(file) && file.isFile()) {//!modifiedFile.containsKey(file)
                        addMonitorTab(file);
                    }
                } else if (file.getName().endsWith(".csv") || file.getName().endsWith(".xlsx")) {
                    if (!this.guiController.getReadOnlyFile().containsKey(file) && file.isFile()) {
                        addReadOnlyTab(file);
                    }
                }
            }
        });
    }


    @FXML
    public void saveItemPressed() {
        this.guiController.saveAll(this.guiController.getOpenedProject().getPath());
    }


    @FXML
    public void closeItemPressed() {
        String projectName = this.guiController.getOpenedProject().getName();
        this.guiController.closeProject();
        treeViewProject.setRoot(null);
        monitorTabPane.getTabs().clear();
        readOnlyTabPane.getTabs().clear();
        terminal.appendText(this.guiController.getTimeString() + INFO + String.format(CLOSE_PROJECT, projectName));
        disableItems();
    }

    @FXML
    public void closeAndSaveItemPressed() {
        this.saveItemPressed();
        this.closeItemPressed();
    }

    private void updateTreeViewProject(File dir, TreeItem<File> parent) {
        TreeItem<File> root = new TreeItem<>(dir, new ImageView(folderIcon));
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                updateTreeViewProject(file, root);
            } else {
                root.getChildren().add(new TreeItem<>(file, new ImageView(fileIcon)));
            }
        }
        if (parent == null) {
            treeViewProject.setRoot(root);
            root.setExpanded(true);
        } else {
            parent.getChildren().add(root);
        }
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
        }
    }


    private void addReadOnlyTab(File file) {
        TextArea textArea = new TextArea();
        textArea.setFont(Font.font(12));
        textArea.setEditable(false);
        Tab tab = new Tab(file.getName(), textArea);
        tab.setOnClosed(event -> this.guiController.getReadOnlyFile().remove(file));
        readOnlyTabPane.getTabs().add(tab);
        readOnlyTabPane.getSelectionModel().select(tab);
        this.guiController.getReadOnlyFile().put(file, textArea);
        try {
            Scanner sc = new Scanner(file);

         /*   int ll=0;
            if(sc.hasNext()) ll = sc.next().split(";").length;

            for(int x=0;x<ll;x++) {
                TableColumn<String, String> tableColumn = new TableColumn<>(" ");
                tableColumn.setCellValueFactory(new PropertyValueFactory<>(" "));

                tableView.getColumns().add(tableColumn);
                tableView.getItems().add("prova");
            }*/


            while (sc.hasNext()) {
                String[] a = sc.next().split(";");

                for (int i = 0; i < a.length; i++) {
                    //tableView.getItems().set(i,"prova");
                    textArea.appendText(a[i] + "                             ");
                }
                textArea.appendText("\n");

                //readOnlyFile.get(file).appendText(sc.nextLine()+"\n");

                //System.out.println(sc.next());


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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


    @FXML
    public void deleteButtonPressed() {
        File file = treeViewProject.getSelectionModel().getSelectedItem().getValue();
            securityQuestion.showSecurityQuestion(file.toPath());
            if (securityQuestion.isDeleted(file.toPath())) {
                this.guiController.getModifiedFile().remove(file);
                this.guiController.getReadOnlyFile().remove(file);
                monitorTabPane.getTabs().removeIf(tab -> tab.getText().equals(file.getName()));
                readOnlyTabPane.getTabs().removeIf(tab -> tab.getText().equals(file.getName()));
                terminal.appendText(INFO + String.format(DELETED_FILE_MESSAGE, treeViewProject.getSelectionModel().getSelectedItem().getValue().getName()));
                updateTreeViewProject(this.guiController.getOpenedProject(), null);
            }
    }

    @FXML
    public void editMenuPressed() {
        if (!treeViewProject.getSelectionModel().getSelectedItems().isEmpty()) {
            File file = treeViewProject.getSelectionModel().getSelectedItem().getValue();
         if(file.isFile() && !file.getName().endsWith(".sib")){
             deleteItem.setDisable(false);
            }else deleteItem.setDisable(true);
        }else deleteItem.setDisable(true);
    }


}

