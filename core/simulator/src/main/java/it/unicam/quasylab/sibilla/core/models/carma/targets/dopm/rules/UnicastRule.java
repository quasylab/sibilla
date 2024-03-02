package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.Mutation;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.MutationResult;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.randomgenerators.SplittableRandomGenerator;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnicastRule extends AbstractRule {

    public UnicastRule(OutputTransition output, List<InputTransition> inputs) {
        super(output, inputs);
    }

    @Override
    public AgentState apply(AgentState state, Agent sender, RandomGenerator randomGenerator) {
        record AgentMutation (Agent agent, Mutation mutation) {}
        Map<Agent, Long> newOccupancies = new HashMap<>(state.getAgents());
        newOccupancies.put(sender, newOccupancies.get(sender) - 1);
        RandomGenerator sr = new SplittableRandomGenerator(randomGenerator.nextLong());
        List<AgentMutation> agentMutations = new ArrayList<>();

        for(InputTransition inputTransition : getInputs()) {
            WeightedStructure<Agent> targets = getTargets(
                    state,
                    sender,
                    newOccupancies,
                    inputTransition
            );

            if(targets.getAll().isEmpty()) {
                return state;
            }

            double sample = sr.nextDouble() * targets.getTotalWeight();
            Agent selected = targets.select(sample).getElement();
            agentMutations.add(new AgentMutation(selected, inputTransition.post()));
        }

        agentMutations.forEach(am -> {
            newOccupancies.put(am.agent, newOccupancies.getOrDefault(am.agent,0L) - 1);
            Agent mutated = sampleMutation(
                    new ExpressionContext(
                            am.agent.values(),
                            sender.values(),
                            state
                    ),
                    am.mutation,
                    sr
            ).orElse(am.agent);
            newOccupancies.put(mutated, newOccupancies.getOrDefault(mutated,0L) + 1);
        });

        Agent newSender = sampleMutation(
                new ExpressionContext(
                        sender.values(),
                        state
                ),
                getOutput().post(),
                sr
        ).orElse(sender);

        newOccupancies.put(newSender, newOccupancies.getOrDefault(newSender,0L) + 1);
        return new AgentState(newOccupancies);
    }

    private WeightedStructure<Agent> getTargets(AgentState state, Agent sender, Map<Agent, Long> occupancies, InputTransition input) {
        WeightedStructure<Agent> targets = new WeightedLinkedList<>();

        occupancies.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0 &&
                        input.predicate().test(
                                e.getKey().species(),
                                new ExpressionContext(e.getKey().values(), state)
                        ) &&
                        input.senderPredicate().test(new ExpressionContext(e.getKey().values(), sender.values(), state))
                ).forEach(e -> targets.add(
                                input.probability().apply(
                                        new ExpressionContext(e.getKey().values(), state)
                                ) * e.getValue(),
                                e.getKey()
                        )
                );

        double totalWeight = targets.getTotalWeight();

        if(totalWeight > 0) {
            WeightedStructure<Agent> weightAdjustedTargets = new WeightedLinkedList<>();
            targets.getAll()
                    .forEach(we -> weightAdjustedTargets.add(we.getWeight() / totalWeight, we.getElement()));
            return weightAdjustedTargets;
        } else {
            return targets;
        }
    }

    private Optional<Agent> sampleMutation(ExpressionContext ectx, Mutation mutation, RandomGenerator rg) {
        MutationResult result = mutation.sampleDeltas(ectx, 1, rg);
        return result.agentDeltaStream().findFirst().map(AgentDelta::agent);
    }

}
