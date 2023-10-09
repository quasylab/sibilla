package it.unicam.quasylab.sibilla.view.controller;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.view.gui.MainView;
import it.unicam.quasylab.sibilla.view.persistence.FilePersistenceManager;
import it.unicam.quasylab.sibilla.view.persistence.SettingsPersistenceManager;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.awt.geom.IllegalPathStateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class containing fundamental methods for the GUI
 * @author LorenzoSerini
 */
public class GUIController {
    public static final String INFO = "INFO: ";
    public static final String CREATION_MESSAGE = "Simulation environment created";
    public static final String CODE_MESSAGE =  "Code %s has been successfully loaded";
    public static final String SIMULATION_SUCCESSFUL = "Simulation successful";
    public static final String SAVE_MESSAGE =  "Save in %s OK!";
    public static final String DELETED_FILE_MESSAGE =  "%s successfully deleted";
    public static final String CLOSE_PROJECT = "The %s project was successfully closed";

    private File openedProject;
    private BuildableFile<BasicSettings> builtFile;
    private boolean projectLoaded;
    private boolean fileBuilt;
    private File persistenceFile;
    private BasicSettings lastSimulatedSettings;
    private Map<File, TextArea> modifiedFile;
    private final Map<File, TableView<MainView.TableViewRow>> readOnlyFile;

    private final SibillaRuntime sibillaRuntime;
    private final FilePersistenceManager filePersistenceManager;
    private final SettingsPersistenceManager settingsPersistence;
    private BasicBuildableLedger buildableLedger;

    private static GUIController instance = null;


    private GUIController(){
        sibillaRuntime = new SibillaRuntime();
        filePersistenceManager = new FilePersistenceManager();
        buildableLedger = new BasicBuildableLedger();
        settingsPersistence = new SettingsPersistenceManager();

        projectLoaded = false;
        builtFile=null;
        fileBuilt=false;
        modifiedFile = new HashMap<>();
        readOnlyFile = new HashMap<>();
    }

    public static GUIController getInstance(){
        if (instance==null){
            instance=new GUIController();
        }
        return instance;
    }

    /**
     * Set the opened project
     * @param project The opened project
     */
    public void setOpenedProject(File project) throws IOException {
        closeProject();
        openedProject=project;
        projectLoaded=true;
        boolean jsonPersistence = false;
        for(Path file:this.getAllFiles(project.getPath())){
            if(isJsonFile(file.toFile().getName())) {
                this.persistenceFile=file.toFile();
                this.loadSettings();
                jsonPersistence=true;
                break;
            }
        }
        if(!jsonPersistence) throw new IllegalPathStateException("persistence file not present!");
    }

    /**
     * Close the opened project
     */
    public void closeProject(){
        openedProject=null;
        builtFile=null;
        modifiedFile.clear();
        readOnlyFile.clear();
        projectLoaded=false;
        fileBuilt=false;
        persistenceFile=null;
    }

    /**
     * Returns the opened project
     * @return opened project
     */
    public File getOpenedProject(){
        return openedProject;
    }


    /**
     * Build a file
     * @param file The file to build
     * @throws CommandExecutionException
     */
    public void build(File file) throws CommandExecutionException {
        for(BuildableFile<BasicSettings> buildableFile:this.buildableLedger.getBuildableList()){
            if(buildableFile.getFile().equals(file)){
                this.sibillaRuntime.clear();
                sibillaRuntime.loadModule(buildableFile.getModule());
                sibillaRuntime.load(file);
                this.builtFile=buildableFile;
                this.fileBuilt=true;
                break;
            } else this.fileBuilt=false;
        }
        if(!fileBuilt) throw new CommandExecutionException("Build of the file "+file.getName()+"failed!");
    }


    /**
     * Returns built file
     * @return The built file
     */
    public File getBuiltFile(){
        if (fileBuilt) {
            return this.builtFile.getFile();
        }else return null;
    }

    /**
     * If the file built returns true else false
     * @return true if the file built else false
     */
    public boolean isFileBuilt() {
        return this.fileBuilt;
    }

    /**
     * If the project loaded returns true else false
     * @return true if the project loaded else false
     */
    public boolean isProjectLoaded(){
        return projectLoaded;
    }

    /**
     * Returns text area associated to a modified files
     * @return text area associated to a modified files
     */
    public Map<File, TextArea> getModifiedFile(){
        return modifiedFile;
    }

    /**
     * Returns text area associated to a read only files (es. csv files)
     * @return text area associated to a read only files
     */
    public Map<File,TableView<MainView.TableViewRow>> getReadOnlyFile(){
        return readOnlyFile;
    }

    /**
     * Returns SibillaRuntime
     * @return SibillaRuntime
     */
    public SibillaRuntime getSibillaRuntime(){
        return sibillaRuntime;
    }


    /**
     * Save in the persistence file the opened project name.
     * It is a persistence command
     * @throws IOException
     */
    public void saveOpenedProject() throws IOException {
        if(isProjectLoaded()){
            filePersistenceManager.save(getOpenedProject().getPath());
        }else filePersistenceManager.save(null);
    }

    /**
     * Load opened file
     * @throws IOException
     */
    public void loadOpenedFile() throws IOException {
        if(filePersistenceManager.load()!=null && Files.exists(Paths.get(filePersistenceManager.load()))) {
            this.setOpenedProject(new File(filePersistenceManager.load()));
            projectLoaded=true;
        }else projectLoaded=false;
    }

    /**
     * returns settings ledger.
     * The settings ledger contains all settings saved
     * @return Settings ledger
     */
    public List<BasicSettings> getSettings(File buildableFile){
        for(BuildableFile<BasicSettings> file:this.buildableLedger.getBuildableList()){
            if (file.getFile().equals(buildableFile)) return file.getSettingsList();
        }
        return null;
    }

    /**
     * Save settings
     * @throws IOException
     */
    public void saveSettings() throws IOException {
        if(this.persistenceFile!=null) settingsPersistence.save(this.buildableLedger, this.persistenceFile);
    }

    /**
     * Load settings ledger
     * @throws IOException
     */
    public void loadSettings() throws IOException {
        if(persistenceFile!=null) this.buildableLedger = this.settingsPersistence.load(this.persistenceFile);
        if(buildableLedger==null) this.buildableLedger = new BasicBuildableLedger();
    }


    /**
     * It is used to delete data associated with files that no longer exist
     */
    public void refreshPersistenceFile() throws IOException {
        if(persistenceFile!=null ) {
            this.buildableLedger.getBuildableList().removeIf(buildableFile -> Files.notExists(buildableFile.getFile().toPath()));
            saveSettings();
        }
    }


   /* public void executeFile(String fileName) throws IOException {
        if (fileName.contains(".sib")) {
            this.sibillaShellInterpreter.executeFile(fileName);
        } else throw new IllegalArgumentException("It's not a .sib file");
    }*/

    /**
     * Save all files
     * @param filePath Save all files contained in the file path
     * @throws IOException
     */
    public void saveAll(String filePath) throws IOException {
            for(Path path:getAllFiles(filePath)){
                if(this.modifiedFile.containsKey(path.toFile())) {
                        FileWriter fw = new FileWriter(path.toFile());
                        fw.write(this.modifiedFile.get(path.toFile()).getText());
                        fw.close();
                }
            }
    }

    /**
     * Returns all files present in the path folder
     * @param path String
     * @return all files present in the path folder
     * @throws IOException
     */
    public List<Path> getAllFiles(String path) throws IOException {
        Stream<Path> paths = Files.walk(Paths.get(path));
        return paths
                //.filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    /**
     * Get time string
     * @return time string
     */
    public String getTimeString(){
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME)+"  ";
    }

 /*   public void overrideSibFile() throws IOException {
        if(getAllFiles(getOpenedProject().getPath()).stream().filter(p->p.toFile().getName().endsWith(".sib")).count() == 1){
            for(Path path: getAllFiles(getOpenedProject().getPath())){
                if(path.toFile().getName().endsWith(".sib")) {
                    FileWriter fileWriter = new FileWriter(path.toFile());
                    fileWriter.write("module \""+getLoadModule()+"\"\n");
                    fileWriter.append("load \""+getLoadFile()+"\"\n");
                    fileWriter.append("init \"init\"\n");
                    fileWriter.append("add all measures\n");
                    fileWriter.append("deadline "+this.sibillaRuntime.getDeadline()+"\n");
                    fileWriter.append("dt "+this.sibillaRuntime.getDt()+"\n");
                    fileWriter.append("replica "+this.sibillaRuntime.getReplica()+"\n");
                    fileWriter.append("simulate\n");
                    fileWriter.append("save output \""+getOpenedProject() + "\\results ("+getLoadFile().getName()+")"+"\" prefix \""+getLoadFile().getName()+"\" postfix \"__\"");
                    fileWriter.close();
                }
            }
        }
    }*/


    /**
     * Copy all files from path to path
     * @param fromPath the file path to copy the files from
     * @param toPath the file path to copy the files to
     * @return files list conteined in toPath
     * @throws IOException
     */
    public List<Path> copy(File fromPath, File toPath) throws IOException {
        List<Path> sources = Files.walk(fromPath.toPath()).collect(Collectors.toList());
        List<Path> destinations = sources.stream()
                .map(getOpenedProject().toPath()::relativize)
                .map(toPath.toPath()::resolve)
                .collect(Collectors.toList());

        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), destinations.get(i));
        }

        return destinations;
    }

    /**
     * Save all files in toPath
     * @param toPath the file path to copy the files to
     * @throws IOException
     */
    public void saveAs(File toPath) throws IOException, CommandExecutionException {
        List<Path> destinations = copy(getOpenedProject(),toPath);
        Map<File,TextArea> newModifiedFile = new HashMap<>();
        BasicBuildableLedger bb = new BasicBuildableLedger();
        for (Path pd:destinations) {
            for (File key:modifiedFile.keySet()){
                if (key.getName().equals(pd.toFile().getName())) newModifiedFile.put(pd.toFile(),modifiedFile.get(key));
            }
            for(BuildableFile<BasicSettings> buildableFile:buildableLedger.getBuildableList()){
                if(buildableFile.getFile().getName().equals(pd.toFile().getName())){
                    BuildableFile<BasicSettings> bf = new BuildableFile<>(pd.toFile(), buildableFile.getModule(), buildableFile.getSettingsList());
                    bb.getBuildableList().add(bf);
                }
            }
        }
        File ff = getBuiltFile();
        this.setOpenedProject(toPath);
        modifiedFile.clear();
        buildableLedger=bb;
        saveSettings();
        modifiedFile=newModifiedFile;
        saveAll(toPath.getPath());
        modifiedFile.clear();
        readOnlyFile.clear();
        if(ff!=null){
            for (Path path : getAllFiles(toPath.getPath())) {
                if (ff.getName().equals(path.toFile().getName())) {
                    build(path.toFile());
                    break;
                }
            }
        }
    }

    /**
     * Create new directory
     * @param path the file to create a new directory
     * @throws IOException
     */
    public void createNewDirectory(File path) throws IOException {
        Files.createDirectories(path.toPath());
    }

    /**
     * Create a new pm file
     * @param path the file to create a new pm file
     * @throws IOException
     */
    public void createNewPmFile(File path) throws IOException {
        FileWriter fw1 = new FileWriter(path.getPath()+"/"+path.getName()+".pm");
        fw1.close();
    }

    /**
     * Create a new persistence file
     * @param path directory path
     * @param fileName file name
     * @throws IOException
     */
    public void createNewPersistenceFile(File path, String fileName) throws IOException {
            FileWriter fw1 = new FileWriter(path.getPath() + "/" + fileName + ".json");
            fw1.close();
    }

    /**
     * Returns buildable files list
     * @return buildable files list
     */
    public List<BuildableFile<BasicSettings>> getBuildableFilesList(){
        return this.buildableLedger.getBuildableList();
    }


    /*public void refreshSibTextArea(){
        for(File key:getModifiedFile().keySet()){
            if(key.getName().endsWith(".sib")){
                try {
                    Scanner sc = new Scanner(key);
                    getModifiedFile().get(key).clear();
                    while (sc.hasNextLine()){
                       getModifiedFile().get(key).appendText(sc.nextLine()+"\n");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    /**
     * Simulate with this settings
     * @param settings The settings with which to do the simulation
     * @throws CommandExecutionException
     */
    public void simulate(BasicSettings settings) throws CommandExecutionException {
        this.sibillaRuntime.reset();
        this.lastSimulatedSettings=settings;
        this.sibillaRuntime.setConfiguration("init");
        this.sibillaRuntime.setDeadline(settings.getDeadline());
        this.sibillaRuntime.setReplica(settings.getReplica());
        this.sibillaRuntime.setDt(settings.getDt());
        for(String measure:settings.getMeasures().keySet()){
            if(settings.getMeasures().get(measure).equals(true)) this.sibillaRuntime.addMeasure(measure);
        }
        this.sibillaRuntime.simulate(settings.getLabel());
    }

    /**
     * Returns last simulated settings
     * @return simulated settings
     */
    public BasicSettings getLastSimulatedSettings(){
        return this.lastSimulatedSettings;
    }

    /**
     * If it's a pm file return true else false
     * @param fileName the file name
     * @return If it's a pm file return true else false
     */
    public boolean isPmFile(String fileName){
        return fileName.endsWith(".pm");
    }

    /**
     * If it's a sib file return true else false
     * @param fileName the file name
     * @return If it's a sib file return true else false
     */
    public boolean isSibFile(String fileName){
        return fileName.endsWith(".sib");
    }

    /**
     * If it's a csv file return true else false
     * @param fileName the file name
     * @return If it's a csv file return true else false
     */
    public boolean isCsvFile(String fileName){
        return fileName.endsWith(".csv");
    }

    /**
     * If it's a json file return true else false
     * @param fileName the file name
     * @return If it's a json file return true else false
     */
    public boolean isJsonFile(String fileName){
        return fileName.endsWith(".json");
    }

    /**
     * If it's a xlsx file return true else false
     * @param fileName the file name
     * @return If it's a xlsx file return true else false
     */
    public boolean isXlsxFile(String fileName){
        return fileName.endsWith(".xlsx");
    }


    public void addNewBuildableFile(File buildableFile, String module){
        this.buildableLedger.getBuildableList().add(new BuildableFile<>(buildableFile, module));
    }
}

