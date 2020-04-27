package quasylab.sibilla.examples.servers.master;


import org.springframework.stereotype.Component;
import quasylab.sibilla.core.server.master.MasterState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class MonitoringServerComponent implements PropertyChangeListener {

    private MasterState state;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.state = (MasterState) evt.getNewValue();


    }

    public MasterState getMasterState() {
        return this.state;
    }
}
