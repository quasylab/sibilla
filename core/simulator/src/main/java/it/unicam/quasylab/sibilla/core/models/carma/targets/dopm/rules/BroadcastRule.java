package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.MutationResult;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.reactions.InputReaction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.reactions.NoReaction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.reactions.Reaction;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BroadcastRule extends AbstractRule {

    public BroadcastRule(OutputTransition output, List<InputTransition> inputs) {
        super(output, inputs);
    }

    @Override
    public AgentState apply(AgentState state, Agent sender, RandomGenerator randomGenerator) {
        Map<Agent, Long> newOccupancies = new HashMap<>(state.getAgents());
        newOccupancies.put(sender, newOccupancies.get(sender) - 1);
        return new AgentState(
                Stream.concat(
                                newOccupancies
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getValue() > 0)
                                        .map(entry -> getAgentReaction(state, entry.getKey(), sender, entry.getValue()))
                                        .flatMap(reaction -> reaction.sampleDeltas(sender, state, randomGenerator)),
                                sampleSenderDeltas(state, sender, randomGenerator)
                        )
                        .collect(Collectors.groupingBy(AgentDelta::agent, Collectors.summingLong(AgentDelta::delta)))
        );
    }

    private Reaction getAgentReaction(AgentState state, Agent agent, Agent sender, Long numberOf) {
        return this.getInputs()
                .stream()
                .filter(i ->
                        i.senderPredicate().test(new ExpressionContext(agent.values(), sender.values(), state)) &&
                        i.predicate().test(agent.species(), new ExpressionContext(agent.values(), state))
                )
                .findFirst()
                .map(i -> (Reaction)new InputReaction(agent, numberOf, i))
                .orElse(new NoReaction(agent, numberOf));
    }

    public Stream<AgentDelta> sampleSenderDeltas(AgentState state, Agent sender, RandomGenerator rg) {
        MutationResult result = this.getOutput()
                .post()
                .sampleDeltas(new ExpressionContext(sender.values(), state), 1, rg);
        return result.nonMutated() < 1 ? result.agentDeltaStream() : Stream.of(new AgentDelta(sender, 1));
    }
}
