package quasylab.sibilla.examples.servers.master;


import org.springframework.stereotype.Component;
import quasylab.sibilla.core.server.master.MasterState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class MonitoringServerComponent implements PropertyChangeListener {

    private Queue<MasterState> states = new PriorityQueue<MasterState>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Master")) {
            MasterState state = (MasterState) evt.getNewValue();
            if (states.size() >= 30 && !states.contains(state)) {
                states.poll();
            }
            if (!states.contains(state))
                states.add(state);
        }
    }

    public Queue<MasterState> getMasterState() {
        return this.states;
    }
}
