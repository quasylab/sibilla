package it.unicam.quasylab.sibilla.core.models.dopm.states.transitions;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;

public class Trigger {
    private final Agent sender;
    private final Rule rule;

    public Trigger(Agent sender, Rule rule) {
        this.sender = sender;
        this.rule = rule;
    }
    public Agent getSender() {
        return sender;
    }

    public Rule getRule() {
        return rule;
    }
}
