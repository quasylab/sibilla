package it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes;

import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.InputAction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.OutputAction;

import java.util.List;
import java.util.Map;

public class Process {
    private int species;
    private Map<String, List<OutputAction>> broadcastOutputs;
    private Map<String, List<InputAction>> broadcastInputs;

    private Map<String, List<OutputAction>> unicastOutputs;
    private Map<String, List<InputAction>> unicastInputs;

    public Process(
            int species,
            Map<String, List<OutputAction>> broadcastOutputs,
            Map<String, List<InputAction>> broadcastInputs,
            Map<String, List<OutputAction>> unicastOutputs,
            Map<String, List<InputAction>> unicastInputs
    ) {
        this.species = species;
        this.broadcastOutputs = broadcastOutputs;
        this.broadcastInputs = broadcastInputs;
        this.unicastOutputs = unicastOutputs;
        this.unicastInputs = unicastInputs;
    }

    public int getSpecies() {
        return species;
    }

    public void setSpecies(int species) {
        this.species = species;
    }

    public Map<String, List<OutputAction>> getBroadcastOutputs() {
        return broadcastOutputs;
    }

    public void setBroadcastOutputs(Map<String, List<OutputAction>> broadcastOutputs) {
        this.broadcastOutputs = broadcastOutputs;
    }

    public Map<String, List<InputAction>> getBroadcastInputs() {
        return broadcastInputs;
    }

    public void setBroadcastInputs(Map<String, List<InputAction>> broadcastInputs) {
        this.broadcastInputs = broadcastInputs;
    }

    public Map<String, List<OutputAction>> getUnicastOutputs() {
        return unicastOutputs;
    }

    public void setUnicastOutputs(Map<String, List<OutputAction>> unicastOutputs) {
        this.unicastOutputs = unicastOutputs;
    }

    public Map<String, List<InputAction>> getUnicastInputs() {
        return unicastInputs;
    }

    public void setUnicastInputs(Map<String, List<InputAction>> unicastInputs) {
        this.unicastInputs = unicastInputs;
    }
}
