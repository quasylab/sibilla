package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public interface Rule {
    AgentState apply(AgentState state, Agent sender, RandomGenerator randomGenerator);

    OutputTransition getOutput();

    void setOutput(OutputTransition output);

    List<InputTransition> getInputs();

    void setInputs(List<InputTransition> inputs);
}
