package quasylab.sibilla.core.newserver.master;


import org.springframework.stereotype.Component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@Component
public class MonitoringServerComponent implements PropertyChangeListener {

    private MasterState state;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Master")) {
            state = (MasterState) evt.getNewValue();
        }
    }

    public MasterState getMasterState(){
        return this.state;
    }
}
