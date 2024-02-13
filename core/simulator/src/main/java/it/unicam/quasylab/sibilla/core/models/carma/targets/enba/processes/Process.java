package it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes;

import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.InputAction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.OutputAction;

import java.util.List;
import java.util.Map;

public record Process (int species, Map<String, List<OutputAction>> outputs, Map<String, List<InputAction>> inputs) {
}
