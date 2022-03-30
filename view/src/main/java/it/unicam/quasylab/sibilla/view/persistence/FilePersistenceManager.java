package it.unicam.quasylab.sibilla.view.persistence;

import com.google.gson.Gson;

import java.io.*;

public class FilePersistenceManager implements PersistenceManager<String>{
    File DEFAULT_JSON_FILE = new File("view\\src\\main\\resources\\persistence\\ProjectLoadedPersistence.json");
    private Gson gson;

    public FilePersistenceManager(Gson gson) {
        this.gson = gson;
    }


    public FilePersistenceManager() {
        this.gson = new Gson();
    }


    @Override
    public void save(String filePath, File file) throws IOException {
        String gsonS = gson.toJson(filePath);
        wrapperSave(gsonS, file);
    }


    /**
     * Saves ledger in a default file
     * @param filePath
     * @throws IOException
     */
    public void save(String filePath) throws IOException {
        if(filePath!=null) {
            this.save(filePath, DEFAULT_JSON_FILE);
        }else {
            FileWriter writer = new FileWriter(DEFAULT_JSON_FILE);
            writer.close();
        };
    }

    @Override
    public String load(File file) throws IOException {
        String gsonLed = wrapperLoad(file);
        return gson.fromJson(gsonLed, String.class);
    }

    /**
     * Loads ledger from default file
     * @return filePath
     * @throws IOException
     */
    public String load() throws IOException {
        return this.load(DEFAULT_JSON_FILE);
    }


    private void wrapperSave(String text, File file) throws IOException {
        BufferedWriter writer = new  BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.close();
    }

    private String wrapperLoad(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String gsonLed = reader.readLine();
        reader.close();
        return gsonLed;
    }

}
