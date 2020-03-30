package quasylab.sibilla.core.server.master;


import org.springframework.stereotype.Component;
import quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

@Component
public class MonitoringServerComponent implements PropertyChangeListener {

    private MasterState state;
    private List<SimulationTimeSeries> results;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Master")) {
            state = (MasterState) evt.getNewValue();
        }
        if (evt.getPropertyName().equals("Results")) {
            results = (List<SimulationTimeSeries>) evt.getNewValue();
        }
    }

    public MasterState getMasterState() {
        return this.state;
    }

    public List<SimulationTimeSeries> getLastResults() {
        return this.results;
    }
}
