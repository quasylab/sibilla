package it.unicam.quasylab.sibilla.view.controller;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.shell.SibillaShellInterpreter;
import it.unicam.quasylab.sibilla.view.persistence.FilePersistenceManager;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileNotFoundException;
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

public class GUIController {
    public static final String INIT_COMMAND = "init ";
    public static final String INFO = "INFO: ";
    public static final String CREATION_MESSAGE = "Simulation environment created\n";
    public static final String MODULE_MESSAGE = "Module %s has been successfully loaded\n";
    public static final String CODE_MESSAGE =  "Code %s has been successfully loaded\n";
    public static final String PARAMETERS_MESSAGE =  "\ndeadline: %s\ndt: %s\nreplica: %s\n\nThe simulation has concluded with success!\n";
    public static final String SAVE_MESSAGE =  "save in %s OK!";
    public static final String CREATED_SUCCESSFULLY_MESSAGE =  "File %s created successfully";
    public static final String CREATED_ERROR_MESSAGE =  "File %s creation ERROR!";
    public static final String DELETED_FILE_MESSAGE =  "File %s successfully deleted\n";
    public static final String CLOSE_PROJECT = "The %s project was successfully closed\n";

    private File openedProject;
    private File loadFile;
    private String loadModule;
    private boolean projectLoaded;
    private boolean fileLoaded;
    private boolean moduleLoaded;
    private Map<File, TextArea> modifiedFile;
    private Map<File, TextArea> readOnlyFile;

    private SibillaShellInterpreter sibillaShellInterpreter;
    private FilePersistenceManager persistence;

    private static GUIController instance = null;


    private GUIController(){
        sibillaShellInterpreter = new SibillaShellInterpreter();
        persistence = new FilePersistenceManager();

        projectLoaded = false;
        moduleLoaded=false;
        fileLoaded=false;
        modifiedFile = new HashMap<>();
        readOnlyFile = new HashMap<>();
    }

    public static GUIController getInstance(){
        if (instance==null){
            instance=new GUIController();
        }
        return instance;
    }

    public void setOpenedProject(File project) {
        openedProject=project;
        projectLoaded=true;
    }

    public void closeProject(){
        openedProject=null;
        loadFile=null;
        loadModule=null;
        modifiedFile.clear();
        readOnlyFile.clear();
        projectLoaded=false;
        moduleLoaded=false;
        fileLoaded=false;
    }

    public File getOpenedProject(){
        return openedProject;
    }

    public void build(File file){

    }


    public void loadModule(String module) throws CommandExecutionException {
        sibillaShellInterpreter.getRuntime().loadModule(module);
        this.loadModule=module;
        this.moduleLoaded=true;
    }

    public String loadModule(File project) {
        try {
            if(getAllFiles(project.getPath()).stream().filter(p->p.toFile().getName().endsWith(".sib")).count() == 1){
                for(Path path: getAllFiles(project.getPath())){
                    if(path.toFile().getName().endsWith(".sib")){
                        Scanner sc = new Scanner(path.toFile());
                        while (sc.hasNextLine()){
                            String string = sc.nextLine().toLowerCase();
                            if (string.contains("module")) {
                                string = string.replaceAll("module", "");
                                string = string.replaceAll(" ", "");
                                string = string.replaceAll("\"", "");
                                loadModule(string);
                                return string;
                            }
                        }
                    }
                }
            }
        } catch (IOException | CommandExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLoadModule(){
        return this.loadModule;
    }

    public void loadFile(File file) throws CommandExecutionException {
        sibillaShellInterpreter.getRuntime().load(file);
        this.loadFile=file;
        this.fileLoaded=true;
    }

    public File getLoadFile(){
        return this.loadFile;
    }

    public boolean isProjectLoaded(){
        return projectLoaded;
    }

    public Map<File, TextArea> getModifiedFile(){
        return modifiedFile;
    }

    public Map<File,TextArea> getReadOnlyFile(){
        return readOnlyFile;
    }

    public SibillaShellInterpreter getSibillaShellInterpreter(){
        return sibillaShellInterpreter;
    }

    public void processSibCommands(String...commands){
        for(String command:commands) {
            sibillaShellInterpreter.execute(command);
        }
    }


    public void saveOpenedProject() throws IOException {
        if(isProjectLoaded()){
            persistence.save(getOpenedProject().getPath());
        }else persistence.save(null);
    }

    public void loadOpenedFile() throws IOException {
        if(persistence.load()!=null && Files.exists(Paths.get(persistence.load()))) {
            this.openedProject = new File(persistence.load());
            this.projectLoaded = true;
        }else projectLoaded=false;

    }

    public double getDeadline(){
        return this.sibillaShellInterpreter.getRuntime().getDeadline();
    }

    public double getDt(){
        return this.sibillaShellInterpreter.getRuntime().getDt();
    }

    public int getReplica(){
        return this.sibillaShellInterpreter.getRuntime().getReplica();
    }

    public boolean isModuleLoaded(){
        return this.moduleLoaded;
    }

    public boolean isFileLoaded(){
        return this.fileLoaded;
    }

    public void executeFile(String fileName) throws IOException {
        if (fileName.contains(".sib")) {
            this.sibillaShellInterpreter.executeFile(fileName);
        } else throw new IllegalArgumentException("It's not a .sib file");
    }

    public void saveAll(String filePath){
        try {
            for(Path path:getAllFiles(filePath)){
                if(this.modifiedFile.containsKey(path.toFile())) {
                    try {
                        FileWriter fw = new FileWriter(path.toFile());
                        fw.write(this.modifiedFile.get(path.toFile()).getText());
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

    public String getTimeString(){
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME)+"  ";
    }

    public void overrideSibFile() throws IOException {
        if(getAllFiles(getOpenedProject().getPath()).stream().filter(p->p.toFile().getName().endsWith(".sib")).count() == 1){
            for(Path path: getAllFiles(getOpenedProject().getPath())){
                if(path.toFile().getName().endsWith(".sib")) {
                    FileWriter fileWriter = new FileWriter(path.toFile());
                    fileWriter.write("module \""+getLoadModule()+"\"\n");
                    fileWriter.append("load \""+getLoadFile()+"\"\n");
                    fileWriter.append("init \"init\"\n");
                    fileWriter.append("add all measures\n");
                    fileWriter.append("deadline "+getDeadline()+"\n");
                    fileWriter.append("dt "+getDt()+"\n");
                    fileWriter.append("replica "+getReplica()+"\n");
                    fileWriter.append("simulate\n");
                    fileWriter.append("save output \""+getOpenedProject() + "\\results ("+getLoadFile().getName()+")"+"\" prefix \""+getLoadFile().getName()+"\" postfix \"__\"");
                    fileWriter.close();
                }
            }
        }
    }


    public List<Path> copy(File toPath) throws IOException {
        List<Path> sources = Files.walk(getOpenedProject().toPath()).collect(Collectors.toList());
        List<Path> destinations = sources.stream()
                .map(getOpenedProject().toPath()::relativize)
                .map(toPath.toPath()::resolve)
                .collect(Collectors.toList());

        for (int i = 0; i < sources.size(); i++) {
            Files.copy(sources.get(i), destinations.get(i));
        }

        return destinations;
    }


    public void saveAs(File toPath) throws IOException {
        List<Path> destinations = copy(toPath);
        Map<File,TextArea> newModifiedFile = new HashMap<>();
        for (Path pd:destinations) {
            for (File key:modifiedFile.keySet()){
                if (key.getName().equals(pd.toFile().getName())){
                    newModifiedFile.put(pd.toFile(),modifiedFile.get(key));
                }
            }
        }
        modifiedFile.clear();
        modifiedFile=newModifiedFile;
        saveAll(toPath.getPath());
        modifiedFile.clear();
    }


    public void createNewProject(File path, String module) throws IOException {
        Files.createDirectories(path.toPath());
        FileWriter fw1 = new FileWriter(path+"/"+path.getName()+".pm");
        fw1.close();
        FileWriter fw2 = new FileWriter(path+"/"+path.getName()+".sib");
        fw2.write("module \""+module+"\"");
        fw2.close();
    }

    public void refreshSibTextArea(){
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
    }

}

