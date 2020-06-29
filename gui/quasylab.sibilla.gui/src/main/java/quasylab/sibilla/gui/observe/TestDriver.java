package quasylab.sibilla.gui.observe;

public class TestDriver {
    public static void main(String[] args) {
        Sensor s = new Sensor();
        DataReader dr = new DataReader("reader 1");
        DataReader dr2 = new DataReader("reader 2");
        s.register(dr);
        s.readData();
        s.unregister(dr);
        s.readData();
    }
}
