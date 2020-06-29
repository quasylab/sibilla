package quasylab.sibilla.gui.observe;

public class DataReader implements Observer {


    private final String name;


    public DataReader(String readerName){
        name = readerName;
    }

    @Override
    public void update() {
        System.out.println(name + "\nReceived data from sensor");
    }

}
