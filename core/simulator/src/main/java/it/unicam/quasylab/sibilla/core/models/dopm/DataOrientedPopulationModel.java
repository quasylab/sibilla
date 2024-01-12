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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class DataOrientedPopulationModel implements Model<DataOrientedPopulationState>,ContinuousTimeMarkovProcess<DataOrientedPopulationState> {

    private final Map<String, Measure<DataOrientedPopulationState>> measures;
    private final Map<String, Predicate<DataOrientedPopulationState>> predicates;
    private final Map<String, Rule> rules;

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
                senderResults.add(r.getOutput().getRate().apply(dataOrientedPopulationState), new DataOrientedPopulationState(r.getOutput().getPost().apply(sender)));

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
                //result.add(senderResults);
                for(WeightedElement<DataOrientedPopulationState> e : senderResults.getAll()) {
                    result.add(e.getTotalWeight(), e.getElement());
                }
            }
        }
        System.out.println("======" + r.getName() + "=====");
        for(WeightedElement<DataOrientedPopulationState> c : result.getAll()) {
            System.out.println(c.getTotalWeight() + "-" + c.getElement().toString());
        }
        return result;
    }


    private WeightedStructure<Agent> getTransitions(DataOrientedPopulationState dataOrientedPopulationState, List<InputTransition> inputs, Agent sender, Agent receiver) {
        WeightedLinkedList<Agent> result = new WeightedLinkedList<>();
        for(InputTransition input : inputs) {
            if(input.getPredicate().test(receiver) && input.getSender_predicate().test(sender)) {
                double receivingProbability = input.getProbability().apply(dataOrientedPopulationState);
                if(receivingProbability > 0) {
                    WeightedElement<Agent> received = new WeightedElement<>(
                            input.getProbability().apply(dataOrientedPopulationState),
                            input.getPost().apply(sender, receiver)
                    );
                    result.add(received);
                }
                double notReceivingProbability = 1-receivingProbability;
                if(notReceivingProbability > 0) {
                    WeightedElement<Agent> notReceived = new WeightedElement<>(
                            notReceivingProbability,
                            new Agent(new String(receiver.getSpecies()), new HashMap<>(receiver.getValues()))
                    );
                    result.add(notReceived);
                }
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
