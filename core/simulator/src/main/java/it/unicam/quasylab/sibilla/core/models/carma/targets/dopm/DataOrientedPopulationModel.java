package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.BroadcastRule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DataOrientedPopulationModel implements Model<AgentState>,ContinuousTimeMarkovProcess<AgentState> {

    private final Map<String, Measure<AgentState>> measures;
    private final Map<String, Predicate<AgentState>> predicates;
    private final List<Rule> rules;

    public DataOrientedPopulationModel(Map<String, Measure<AgentState>> measures, Map<String, Predicate<AgentState>> predicates, List<Rule> rules) {
        this.measures = measures;
        this.predicates = predicates;
        this.rules = rules;
    }

    @Override
    public WeightedStructure<StepFunction<AgentState>> getTransitions(RandomGenerator r, double time, AgentState dataOrientedPopulationState) {
        WeightedStructure<StepFunction<AgentState>> res = new WeightedLinkedList<>();
        this.rules
                .stream()
                .flatMap(rule -> getRuleTransitions(dataOrientedPopulationState, rule, r))
                .forEach(c -> res.add(c.getTotalWeight(), c.getElement()));
        return res;
    }


    private Stream<WeightedElement<StepFunction<AgentState>>> getRuleTransitions(AgentState state, Rule rule, RandomGenerator r) {
        return  state
                .getAgents()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 0 && rule.getOutput().predicate().test(e.getKey().species(), new ExpressionContext(e.getKey().values(), state)))
                .map(e -> new WeightedElement<StepFunction<AgentState>>(
                        rule.getOutput().rate().apply(new ExpressionContext(e.getKey().values(), state)) * e.getValue(),
                        (rnd, t, dt) ->  rule.apply(state, e.getKey(), r)
                ));
    }
    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(AgentState state) throws IOException {
        return new byte[0];
    }

    @Override
    public AgentState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return measures.keySet().toArray(new String[0]);
    }

    @Override
    public double measure(String m, AgentState state) {
        return measures.get(m).measure(state);
    }

    @Override
    public Measure<? super AgentState> getMeasure(String m) {
        return measures.get(m);
    }

    @Override
    public Predicate<? super AgentState> getPredicate(String name) {
        return predicates.get(name);
    }

    @Override
    public String[] predicates() {
        return predicates.keySet().toArray(new String[0]);
    }

    public List<Rule> getRules() {
        return rules;
    }

}
