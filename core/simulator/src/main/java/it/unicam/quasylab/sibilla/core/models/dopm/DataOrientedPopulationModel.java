package it.unicam.quasylab.sibilla.core.models.dopm;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.models.dopm.states.RuleApplication;
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
            WeightedStructure<RuleApplication> ruleApplications = getTransitions(rule, dataOrientedPopulationState);
            for(WeightedElement<RuleApplication> ruleApplicationWeightedElement : ruleApplications.getAll()) {
                result.add(ruleApplicationWeightedElement.getTotalWeight(), (rnd, t, dt) ->
                    dataOrientedPopulationState.applyRule(ruleApplicationWeightedElement.getElement(), rnd)
                );
            }
        }
        return result;
    }

    private WeightedStructure<RuleApplication> getTransitions(Rule r, DataOrientedPopulationState dataOrientedPopulationState) {
        WeightedStructure<RuleApplication> result = new WeightedLinkedList<>();
        for(Map.Entry<Agent, Long> e : dataOrientedPopulationState.getAgents().entrySet()) {
            if(r.getOutput().getPredicate().test(e.getKey())) {
                result.add(
                        r.getOutput().getRate().apply(dataOrientedPopulationState, e.getKey()) * e.getValue(),
                        new RuleApplication(e.getKey(), r)
                );
            }
        }
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
