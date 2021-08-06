package it.unicam.quasylab.sibilla.view.gui;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.shell.SibillaShellInterpreter;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SibillaJavaFXMainController implements Initializable {
    private JavaFXSecurityQuestion securityQuestion;
    public static final String INIT_COMMAND = "init ";
    public static final String INFO = "INFO: ";
    public static final String CREATION_MESSAGE = "Simulation environment created\n";
    public static final String MODULE_MESSAGE = "Module %s has been successfully loaded\n";
    public static final String CODE_MESSAGE =  "Code %s has been successfully loaded\n";
    public static final String PARAMETERS_MESSAGE =  "\ndeadline: %s\ndt: %s\nreplica: %s\n\nThe simulation has concluded with success!\n";
    public static final String SAVE_MESSAGE =  "save in %s OK!";
    public static final String CREATED_SUCCESSFULLY_MESSAGE =  "File %s created successfully";
    public static final String CREATED_ERROR_MESSAGE =  "File %s creation ERROR!";
    public static final String DELETED_FILE_MESSAGE =  "File %s successfully deleted";
    public static final String CLOSE_PROJECT = "The %s project was successfully closed";

    private SibillaShellInterpreter sibillaShellInterpreter;
    private SibillaJavaFXChooseTheModule chooseTheModule;
    private SibillaJavaFXSetTheParameters setTheParameters;
    private SibillaJavaFXNewFileController newFileController;
    private File openedFile;


    private Map<File, TextArea> modifiedFile;
    private Map<File, TextArea> readOnlyFile;

    @FXML
    private MenuBar mainMenuBar;

    @FXML
    private TextArea terminal;

    @FXML
    private MenuItem closeItem;

    @FXML
    private MenuItem newFileItem;

    @FXML
    private MenuItem saveItem;

    @FXML
    private MenuItem saveAsItem;

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
    private VBox main;

    private boolean loadingFile;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sibillaShellInterpreter = new SibillaShellInterpreter();
        chooseTheModule = new SibillaJavaFXChooseTheModule();
        setTheParameters = new SibillaJavaFXSetTheParameters();
        newFileController = new SibillaJavaFXNewFileController();
        this.modifiedFile = new HashMap<>();
        this.readOnlyFile = new HashMap<>();

        simulate.setDisable(true);
        loadingFile = false;

        main.addEventFilter(MouseEvent.ANY, event ->{
            Tab tab = monitorTabPane.getSelectionModel().getSelectedItem();
            if (!monitorTabPane.getTabs().isEmpty() && loadingFile) {
                if(tab.getText().endsWith(".pm")){
                    simulate.setDisable(false);
                }else simulate.setDisable(true);
            }else simulate.setDisable(true);

            if (!monitorTabPane.getTabs().isEmpty() && tab.getText().endsWith(".pm")) {
                buildButton.setDisable(false);
            }else buildButton.setDisable(true);
        }
        );

        disableItems();

        terminal.setStyle("-fx-text-inner-color: red;");
        terminal.setText(getTimeString()+INFO+CREATION_MESSAGE);
    }




    @FXML
    public void buildButtonPressed() throws CommandExecutionException {

        Tab tab = monitorTabPane.getSelectionModel().getSelectedItem();
        if (tab.getText() != null) {
            try {
                for(Path path: allFiles(this.openedFile.getPath())){
                    if(path.toFile().getName().equals(tab.getText())){
                        saveAll(this.openedFile.getPath());
                        sibillaShellInterpreter.getRuntime().load(path.toFile());
                        this.loadingFile=true;
                        terminal.appendText(getTimeString()+INFO+String.format(CODE_MESSAGE,tab.getText()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {terminal.setText("File not selected.\n\n" +
                "Select a file!");
        }
    }

    @FXML
    public void simulateButtonPressed() {
        sibillaShellInterpreter.execute(INIT_COMMAND+"\"init\"");
        sibillaShellInterpreter.getRuntime().addAllMeasures();
        setTheParameters.showTheStage(this.sibillaShellInterpreter);
        if(!Double.isNaN(sibillaShellInterpreter.getRuntime().getDeadline()) && !Double.isNaN(sibillaShellInterpreter.getRuntime().getDt())){
            try {
                sibillaShellInterpreter.getRuntime().simulate("");
                this.readOnlyFile.clear();
                this.readOnlyTabPane.getTabs().clear();
                terminal.appendText(getTimeString()+INFO+String.format(PARAMETERS_MESSAGE, sibillaShellInterpreter.getRuntime().getDeadline(), sibillaShellInterpreter.getRuntime().getDt(), sibillaShellInterpreter.getRuntime().getReplica()));
            } catch (CommandExecutionException e) {
                e.printStackTrace();
            }
        }
        //TODO: mettere la possibilità di scegliere se sovrascrivere il file results oppure crearne
        // uno nuovo
        try {
            if (allFiles(this.openedFile.getPath()).stream().filter(p -> p.toFile().getName().equals("results ("+this.monitorTabPane.getSelectionModel().getSelectedItem().getText()+")")).count() != 1) {
                Path results = Paths.get(this.openedFile + "/results ("+this.monitorTabPane.getSelectionModel().getSelectedItem().getText()+")");
                Files.createDirectory(results);
            }
            sibillaShellInterpreter.getRuntime().save(this.openedFile.getPath()+"/results ("+this.monitorTabPane.getSelectionModel().getSelectedItem().getText()+")", this.monitorTabPane.getSelectionModel().getSelectedItem().getText(), "__");
            updateTreeViewProject(this.openedFile, null);
        } catch (IOException | CommandExecutionException e) {
            e.printStackTrace();
        }

    }


    @FXML
    public void helpItemPressed(){
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

    @FXML
    public void openItemPressed(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open project");
        Stage directoryStage = new Stage();
        File file = directoryChooser.showDialog(directoryStage);
        if(file!=null){
            this.openedFile=file;
            updateTreeViewProject(this.openedFile, null);
             monitorTabPane.getTabs().clear();
            try {
                if(allFiles(this.openedFile.getPath()).stream().filter(p->p.toFile().getName().endsWith(".sib")).count() == 1){
                   for(Path path: allFiles(this.openedFile.getPath())){
                       if(path.toFile().getName().endsWith(".sib")){
                           Scanner sc = new Scanner(path.toFile());
                           while (sc.hasNextLine()){
                               String string = sc.nextLine().toLowerCase();
                                   if (string.contains("module")) {
                                       string = string.replaceAll("module", "");
                                       string = string.replaceAll(" ", "");
                                       string = string.replaceAll("\"", "");
                                       sibillaShellInterpreter.getRuntime().loadModule(string);
                                       terminal.appendText(getTimeString()+INFO+String.format(MODULE_MESSAGE,string));
                                       break;
                                   }
                           }
                       }
                   }
                }else terminal.setText("ERROR: there are too many .sib files");
               enableItems();
            } catch (IOException | CommandExecutionException e) {
                e.printStackTrace();
            }
        }
        }


    @FXML
    public void saveAsItemPressed(){
        //TODO: da completare.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        Stage window = new Stage();
        fileChooser.setInitialFileName(openedFile.getName());
        File dir = fileChooser.showSaveDialog(window);
            try {
                saveAs(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*try {
                for(Path path:allFiles(dir.getPath())){
                    if(modifiedFile.containsKey(path.toFile())) {
                        try {
                            FileWriter fw = new FileWriter(path.toFile());
                            fw.write(modifiedFile.get(path.toFile()).getText());
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }*/


           /* try {
                FileWriter fw = new FileWriter(dir);

                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        //}else terminal.setText("ERROR: Directory "+dir.getPath()+" not created!");

       /* FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sibilla", "*.sibilla"));
        Stage window = new Stage();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text", "*.txt"));
        File file = fileChooser.showSaveDialog(window);
        try {
            FileWriter fw = new FileWriter(file);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    @FXML
    public void exitItemPressed(){
        Stage window =  (Stage) mainMenuBar.getScene().getWindow();
        window.close();
    }


    @FXML
    public void newProjectItemPressed(){
        chooseTheModule.showTheStage(this.sibillaShellInterpreter);
        if(chooseTheModule.getChosenModule()!=null) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save");
                Stage window = new Stage();
                File dir = fileChooser.showSaveDialog(window);
                createNewProject(dir);
                this.openedFile=dir;
                updateTreeViewProject(this.openedFile, null);
                monitorTabPane.getTabs().clear();
                if(this.openedFile!=null){
                    enableItems();
                }
                this.terminal.appendText(getTimeString()+INFO + String.format(MODULE_MESSAGE, chooseTheModule.getChosenModule()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    @FXML
    public void newFileItemPressed(){
        newFileController.showTheStage(this.openedFile);
        updateTreeViewProject(this.openedFile,null);
    }




    @FXML
    public void doubleClickPressed(){
        treeViewProject.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
             File file = treeViewProject.getSelectionModel().getSelectedItem().getValue();
             if(file.getName().endsWith(".pm")||file.getName().endsWith(".sib")) {
                    if (!modifiedFile.containsKey(file) && file.isFile()) {
                        addMonitorTab(file);
                    }//TODO: else: vai al tab che contiene il monitor del file
                }else{
                    if (!readOnlyFile.containsKey(file) && file.isFile()) {
                        addReadOnlyTab(file);
                    }
                }
            }
        });
    }



    @FXML
    public void saveItemPressed(){
        saveAll(this.openedFile.getPath());
    }





    @FXML
    public void closeItemPressed(){
        String projectName = openedFile.getName();
        this.openedFile=null;
        treeViewProject.setRoot(null);
        monitorTabPane.getTabs().clear();
        readOnlyTabPane.getTabs().clear();
        modifiedFile.clear();
        readOnlyFile.clear();
        terminal.appendText(getTimeString()+INFO+String.format(CLOSE_PROJECT, projectName));
        disableItems();
    }





    /**
     * Returns all files present in the path folder
     * @param path String
     * @return all files present in the path folder
     * @throws IOException
     */
    private List<Path> allFiles(String path) throws IOException {
        Stream<Path> paths = Files.walk(Paths.get(path));
        return paths
               //.filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }



    private void updateTreeViewProject(File dir, TreeItem<File> parent) {
        TreeItem<File> root = new TreeItem<>(dir);
        root.setExpanded(true);
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                updateTreeViewProject(file, root);
            } else {
                root.getChildren().add(new TreeItem<>(file));
            }

        }
        if (parent == null) {
            treeViewProject.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }
    }


    private void addMonitorTab(File file){
        TextArea monitor = new TextArea();
        monitor.setFont(Font.font("Verdana", 14));
        Tab tab = new Tab(file.getName(), monitor);
        tab.setOnClosed(event -> {
            modifiedFile.get(file).clear();
            modifiedFile.remove(file);
            }
        );
        monitorTabPane.getTabs().add(tab);
        monitorTabPane.getSelectionModel().select(tab);
        modifiedFile.put(file, monitor);
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()){
                modifiedFile.get(file).appendText(sc.nextLine()+"\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void addReadOnlyTab(File file){
        //TODO: trovare un modo per migliorare la grafica di visualizzazione dei file
        // .csv (textarea non è la migliore)
        TextArea textArea = new TextArea();
        textArea.setFont(Font.font(12));
        textArea.setEditable(false);
        Tab tab = new Tab(file.getName(), textArea);
        tab.setOnClosed(event -> readOnlyFile.remove(file));
        readOnlyTabPane.getTabs().add(tab);
        readOnlyTabPane.getSelectionModel().select(tab);
        readOnlyFile.put(file,textArea);
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


            while (sc.hasNext()){
                String[] a = sc.next().split(";");

                for (int i=0; i<a.length;i++) {
                    //tableView.getItems().set(i,"prova");
                    textArea.appendText(a[i]+"                             ");
                }
                textArea.appendText("\n");

                //readOnlyFile.get(file).appendText(sc.nextLine()+"\n");

                //System.out.println(sc.next());


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void saveAll(String filePath){
        try {
            for(Path path:allFiles(filePath)){
                if(modifiedFile.containsKey(path.toFile())) {
                    try {
                        FileWriter fw = new FileWriter(path.toFile());
                        fw.write(modifiedFile.get(path.toFile()).getText());
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void saveAs(File toPath) throws IOException {

    }


    private void copy(File toPath) throws IOException {
        List<Path> sources = Files.walk(openedFile.toPath()).collect(Collectors.toList());
        List<Path> destinations = sources.stream()
                .map(openedFile.toPath()::relativize)
                .map(toPath.toPath()::resolve)
                .collect(Collectors.toList());
        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), destinations.get(i));
        }
    }

    private void createNewProject(File path) throws IOException {
        Files.createDirectories(path.toPath());
        FileWriter fw1 = new FileWriter(path+"/"+path.getName()+".pm");
        fw1.close();
        FileWriter fw2 = new FileWriter(path+"/"+path.getName()+".sib");
        fw2.write("module \""+chooseTheModule.getChosenModule()+"\"");
        fw2.close();
    }


    private void disableItems(){
        buildButton.setDisable(true);
        closeItem.setDisable(true);
        saveItem.setDisable(true);
        saveAsItem.setDisable(true);
        newFileItem.setDisable(true);
    }

    private void enableItems(){
        saveItem.setDisable(false);
        saveAsItem.setDisable(false);
        closeItem.setDisable(false);
        newFileItem.setDisable(false);
    }


   private String getTimeString(){
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME)+"  ";
   }

}

