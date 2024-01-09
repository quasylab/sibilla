package it.unicam.quasylab.sibilla.core.models.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.OutputTransition;

import java.util.List;

public class Rule {
    private OutputTransition output;
    private List<InputTransition> inputs;

    public Rule(OutputTransition output, List<InputTransition> inputs) {
        this.output = output;
        this.inputs = inputs;
    }

    public OutputTransition getOutput() {
        return output;
    }

    public void setOutput(OutputTransition output) {
        this.output = output;
    }

    public List<InputTransition> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputTransition> inputs) {
        this.inputs = inputs;
    }
}
