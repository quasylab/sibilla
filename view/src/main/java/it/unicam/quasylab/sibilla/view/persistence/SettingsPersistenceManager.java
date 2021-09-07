package it.unicam.quasylab.sibilla.view.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import it.unicam.quasylab.sibilla.view.controller.Settings;
import it.unicam.quasylab.sibilla.view.controller.SettingsLedger;

import java.io.*;
import java.time.LocalDate;
import java.util.Map;

public class SettingsPersistenceManager implements PersistenceManager<SettingsLedger>{
    File DEFAULT_JSON_FILE = new File("view\\src\\main\\resources\\persistence\\SettingsPersistence.json");
    private Gson gson;

    public SettingsPersistenceManager(Gson gson) {
        this.gson = gson;
    }


    public SettingsPersistenceManager() {
        this(new Gson());
    }


    @Override
    public void save(SettingsLedger settingsMemory, File file) throws IOException {
        String gsonLed = gson.toJson(settingsMemory);
        wrapperSave(gsonLed, file);
    }

    public void save(SettingsLedger settingsMemory) throws IOException {
        this.save(settingsMemory, DEFAULT_JSON_FILE);
    }

    private void wrapperSave(String text, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.close();
    }

    @Override
    public SettingsLedger load(File file) throws IOException {
        String gsonLed = wrapperLoad(file);
        return gson.fromJson(gsonLed, SettingsLedger.class);
    }

    public SettingsLedger load() throws IOException {
        return this.load(DEFAULT_JSON_FILE);
    }

    private String wrapperLoad(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String gsonLed = reader.readLine();
        reader.close();
        return gsonLed;
    }

}
