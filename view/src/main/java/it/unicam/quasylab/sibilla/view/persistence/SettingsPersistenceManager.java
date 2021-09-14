package it.unicam.quasylab.sibilla.view.persistence;

import com.google.gson.Gson;
import it.unicam.quasylab.sibilla.view.controller.BasicSettingsLedger;

import java.io.*;

public class SettingsPersistenceManager implements PersistenceManager<BasicSettingsLedger> {
    File DEFAULT_JSON_FILE = new File("view\\src\\main\\resources\\persistence\\SettingsPersistence.json");
    private Gson gson;

    public SettingsPersistenceManager(Gson gson) {
        this.gson = gson;
    }


    public SettingsPersistenceManager() {
        this(new Gson());
    }


    @Override
    public void save(BasicSettingsLedger settingsMemory, File file) throws IOException {
        String gsonLed = gson.toJson(settingsMemory);
        wrapperSave(gsonLed, file);
    }

    public void save(BasicSettingsLedger settingsMemory) throws IOException {
        this.save(settingsMemory, DEFAULT_JSON_FILE);
    }

    private void wrapperSave(String text, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.close();
    }

    @Override
    public BasicSettingsLedger load(File file) throws IOException {
        String gsonLed = wrapperLoad(file);
        return gson.fromJson(gsonLed, BasicSettingsLedger.class);
    }

    public BasicSettingsLedger load() throws IOException {
        return this.load(DEFAULT_JSON_FILE);
    }

    private String wrapperLoad(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String gsonLed = reader.readLine();
        reader.close();
        return gsonLed;
    }

}
