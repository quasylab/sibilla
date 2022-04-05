package it.unicam.quasylab.sibilla.view.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildableFile<T extends Settings> implements Buildable<T>{
    private String file;
    private String module;
    private List<T> settingsList;

    public BuildableFile(File file, String module, List<T> settingsList){
        this.file=file.getPath();
        this.module=module;
        this.settingsList=settingsList;
    }

    public BuildableFile(File file, String module){
        this(file,module,new ArrayList<>());
    }

    @Override
    public File getFile(){return new File(file);}

    @Override
    public void setModule(String module){this.module=module;}

    @Override
    public String getModule(){return this.module;}

    @Override
    public void setSettingsList(List<T> settingsList){this.settingsList=settingsList;}

    @Override
    public List<T> getSettingsList(){return this.settingsList;}

}
