package it.unicam.quasylab.sibilla.core.models.dopm.states.transitions;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations.MutationResult;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.AgentDelta;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.Stream;

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

    public Stream<AgentDelta> sampleDeltas(DataOrientedPopulationState state, RandomGenerator rg) {
        MutationResult result =  rule.getOutput()
                                    .post()
                                    .sampleDeltas(new ExpressionContext(sender.values(), state), 1, rg);
        return result.nonMutated() < 1 ? result.agentDeltaStream() : Stream.of(new AgentDelta(sender, 1));
    }

}
