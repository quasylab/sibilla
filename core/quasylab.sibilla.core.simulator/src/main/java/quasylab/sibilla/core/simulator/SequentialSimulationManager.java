package quasylab.sibilla.core.simulator;

import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class SequentialSimulationManager<S> implements SimulationManager<S> {

    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(property, listener);
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function, boolean enableGUI) {
        SimulationSession<S> newSession = new SimulationSession<S>(expectedTasks, sampling_function);
        if(enableGUI)
            new SimulationView<>(newSession, this);
        return newSession;
    }

    @Override
    public long reach() {
        return 0;
    }

    private void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    @Override
    public void run(SimulationSession<S> session, SimulationTask<S> task) {
        doSample(session.getSamplingFunction(), task.get());
        session.taskCompleted();
        propertyChange("progress"+session.toString(), session.getExpectedTasks());
    }

    @Override
    public void waitTermination(SimulationSession<S> session) throws InterruptedException {
        return;
    }

    private void propertyChange(String property, Object value){
        pcs.firePropertyChange(property, null, value);
    }
    
}