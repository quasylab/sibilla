package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.MutationResult;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractRule implements Rule {

    private OutputTransition output;
    private List<InputTransition> inputs;

    public AbstractRule(OutputTransition output, List<InputTransition> inputs) {
        this.output = output;
        this.inputs = new ArrayList<>(inputs);
    }

    @Override
    abstract public AgentState apply(AgentState state, Agent sender, RandomGenerator randomGenerator);

    @Override
    public OutputTransition getOutput() {
        return output;
    }

    @Override
    public void setOutput(OutputTransition output) {
        this.output = output;
    }

    @Override
    public List<InputTransition> getInputs() {
        return inputs;
    }

    @Override
    public void setInputs(List<InputTransition> inputs) {
        this.inputs = inputs;
    }

}
