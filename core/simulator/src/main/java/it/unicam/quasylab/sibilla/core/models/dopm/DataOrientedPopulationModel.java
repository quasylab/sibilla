package it.unicam.quasylab.sibilla.core.models.dopm;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.Trigger;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        WeightedStructure<StepFunction<DataOrientedPopulationState>> res = new WeightedLinkedList<>();
        this.rules.values()
                .stream()
                .flatMap(rule -> getRuleTransitions(dataOrientedPopulationState, rule, r))
                .forEach(c -> res.add(c.getTotalWeight(), c.getElement()));
        return res;
    }


    private Stream<WeightedElement<StepFunction<DataOrientedPopulationState>>> getRuleTransitions(DataOrientedPopulationState state, Rule rule, RandomGenerator r) {
        return  state
                .getAgents()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 0 && rule.getOutput().predicate().test(e.getKey().species(), new ExpressionContext(e.getKey().values(), state)))
                .map(e -> new WeightedElement<StepFunction<DataOrientedPopulationState>>(
                        rule.getOutput().rate().apply(new ExpressionContext(e.getKey().values(), state)) * e.getValue(),
                        (rnd, t, dt) ->  state.applyRule(new Trigger(e.getKey(), rule), r)
                ));
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
