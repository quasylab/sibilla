package it.unicam.quasylab.sibilla.core.models.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.OutputTransition;

import java.util.List;

public class Rule {
    private String name;
    private OutputTransition output;
    private List<InputTransition> inputs;

    public Rule(String name, OutputTransition output, List<InputTransition> inputs) {
        this.name = name;
        this.output = output;
        this.inputs = inputs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
