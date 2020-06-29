package quasylab.sibilla.gui.observe;

import java.util.ArrayList;
import java.util.List;

public class Sensor implements Subject {

    private List<Observer> observers = new ArrayList<>();

    public void readData(){
        //routine lettura di dati
        notifyObservers();
    }

    @Override
    public void register(Observer o) {
        observers.add(o);
    }

    @Override
    public void unregister(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer obs : observers ){
            obs.update();
        }
    }
}
