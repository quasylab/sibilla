package it.unicam.quasylab.sibilla.core.models.dopm.states;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;

public class RuleApplication {
    private final Agent sender;
    private final Rule rule;

    public RuleApplication(Agent sender, Rule rule) {
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
