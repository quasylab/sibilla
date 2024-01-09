package it.unicam.quasylab.sibilla.core.models.dopm;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DataOrientedPopulationModel implements Model<DataOrientedPopulationState>,ContinuousTimeMarkovProcess<DataOrientedPopulationState> {

    private Map<String, Measure<DataOrientedPopulationState>> measures;
    private Map<String, Predicate<DataOrientedPopulationState>> predicates;

    private Map<String, Rule> rules;

    public DataOrientedPopulationModel(Map<String, Measure<DataOrientedPopulationState>> measures, Map<String, Predicate<DataOrientedPopulationState>> predicates, Map<String, Rule> rules) {
        this.measures = measures;
        this.predicates = predicates;
        this.rules = rules;
    }


    @Override
    public WeightedStructure<StepFunction<DataOrientedPopulationState>> getTransitions(RandomGenerator r, double time, DataOrientedPopulationState dataOrientedPopulationState) {
        WeightedLinkedList<StepFunction<DataOrientedPopulationState>> result = new WeightedLinkedList<>();
        for(Rule rule : this.rules.values()) {
            WeightedStructure<DataOrientedPopulationState> ruleTransitions = getTransitions(rule, dataOrientedPopulationState);
            for(WeightedElement<DataOrientedPopulationState> ruleTransition : ruleTransitions.getAll()) {
                result.add(ruleTransition.getTotalWeight(), (rnd, t, dt) -> ruleTransition.getElement());
            }
        }
        return result;
    }

    private WeightedStructure<DataOrientedPopulationState> getTransitions(Rule r, DataOrientedPopulationState dataOrientedPopulationState) {
        WeightedStructure<DataOrientedPopulationState> result = new WeightedLinkedList<>();
        for(Agent sender : dataOrientedPopulationState.getAgents()) {
            if(r.getOutput().getPredicate().test(sender)) {
                WeightedStructure<DataOrientedPopulationState> senderResults = new WeightedLinkedList<>();
                WeightedElement<DataOrientedPopulationState> outputRes = new WeightedElement<>(
                        r.getOutput().getRate().apply(dataOrientedPopulationState),
                        new DataOrientedPopulationState(r.getOutput().getPost().apply(sender))
                );
                senderResults.add(outputRes);
                for(Agent receiver : dataOrientedPopulationState.getAgents()) {
                    if(receiver != sender) {
                        WeightedStructure<DataOrientedPopulationState> newSenderResults = new WeightedLinkedList<>();
                        WeightedStructure<Agent> inputResults = getTransitions(dataOrientedPopulationState, r.getInputs(), sender, receiver);

                        for(WeightedElement<Agent> inputResult : inputResults.getAll()) {
                            for (WeightedElement<DataOrientedPopulationState> oldSenderResult : senderResults.getAll()) {
                                newSenderResults.add(
                                        oldSenderResult.getTotalWeight() * inputResult.getTotalWeight(),
                                        oldSenderResult.getElement().addAgent(inputResult.getElement())
                                );
                            }
                        }
                        senderResults = newSenderResults;
                    }
                }
                result.add(senderResults);
            }
        }
        return result;
    }


    private WeightedStructure<Agent> getTransitions(DataOrientedPopulationState dataOrientedPopulationState, List<InputTransition> inputs, Agent sender, Agent receiver) {
        WeightedLinkedList<Agent> result = new WeightedLinkedList<>();
        for(InputTransition input : inputs) {
            if(input.getPredicate().test(receiver) && input.getSender_predicate().test(sender)) {
                WeightedElement<Agent> received = new WeightedElement<>(
                        input.getProbability().apply(dataOrientedPopulationState),
                        input.getPost().apply(sender,receiver)
                );
                WeightedElement<Agent> notReceived = new WeightedElement<>(
                        1-input.getProbability().apply(dataOrientedPopulationState),
                        new Agent(new String(receiver.getSpecies()), new HashMap<>(receiver.getValues()))
                );
                result.add(received);
                result.add(notReceived);
                return result;
            }
        }
        result.add(new WeightedElement<>(
                1,
                new Agent(new String(receiver.getSpecies()), new HashMap<>(receiver.getValues()))
        ));
        return result;
    }


    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(DataOrientedPopulationState state) throws IOException {
        return new byte[0];
    }

    @Override
    public DataOrientedPopulationState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return measures.keySet().toArray(new String[0]);
    }

    @Override
    public double measure(String m, DataOrientedPopulationState state) {
        return measures.get(m).measure(state);
    }

    @Override
    public Measure<? super DataOrientedPopulationState> getMeasure(String m) {
        return measures.get(m);
    }

    @Override
    public Predicate<? super DataOrientedPopulationState> getPredicate(String name) {
        return predicates.get(name);
    }

    @Override
    public String[] predicates() {
        return predicates.keySet().toArray(new String[0]);
    }
}
