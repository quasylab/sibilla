package it.unicam.quasylab.sibilla.core.models.carma.targets.enba;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.DataOrientedPopulationModel;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.BroadcastRule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.Process;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.OutputAction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ENBAModel implements Model<AgentState>, ContinuousTimeMarkovProcess<AgentState> {
    private final List<Process> processes;
    private final DataOrientedPopulationModel dopm;

    public ENBAModel(List<Process> processes, Map<String, Measure<AgentState>> measures, Map<String, Predicate<AgentState>> predicates) {
        this.processes = processes;
        this.dopm = new DataOrientedPopulationModel(measures, predicates, getRules(processes));
    }

    private static List<Rule> getRules(List<Process> processes) {
        record OutputRule(OutputAction output, Rule rule) {}

        Map<String, List<OutputRule>> rules = processes.stream()
                .flatMap(p -> p.outputs().values().stream())
                .flatMap(Collection::stream)
                .map(o -> new AbstractMap.SimpleEntry<>(
                        o.channel(),
                        new OutputRule(
                            o,
                            new BroadcastRule(
                                new OutputTransition(o.predicate(),o.rate(),o.post()),
                                new ArrayList<>()
                            )
                        )
                    )
                ).collect(
                        Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(
                                        Map.Entry::getValue,
                                        Collectors.toList()
                                )
                        )
                );

        processes.stream()
                .flatMap(p -> p.inputs().values().stream())
                .flatMap(Collection::stream)
                .forEach(i -> rules.get(i.channel()).forEach(o ->
                            o.rule.getInputs().add(
                                    new InputTransition(
                                            (s, c) -> i.predicate().test(s, c) && o.output.receiverPredicate().test(c),
                                            i.senderPredicate(),
                                            i.probability(),
                                            i.post()
                                    )
                            )
                        )
                );

        return rules
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(o -> o.rule)
                .toList();
    }

    /**
     * Returns the transitions enabled in a given state at a given time. Each transition
     * is represented via a <code>StepFunction</code>, and all the enabled transitions are stored
     * in a <code>WeightedStructure</code> that associates each function with its rate.
     *
     * @param r          random generator used to sample needed random varibales.
     * @param time       current time.
     * @param agentState current state.
     * @return the weighted structure with all the enabled transitions.
     */
    @Override
    public WeightedStructure<? extends StepFunction<AgentState>> getTransitions(RandomGenerator r, double time, AgentState agentState) {
        return dopm.getTransitions(r,time,agentState);
    }

    /**
     * Returns the number of bytes needed to store model states.
     *
     * @return the number of bytes needed to store model states.
     */
    @Override
    public int stateByteArraySize() {
        return 0;
    }

    /**
     * Returns the array of bytes representing the given state.
     *
     * @param state the state to serialise.
     * @return the array of bytes representing the given state.
     */
    @Override
    public byte[] byteOf(AgentState state) throws IOException {
        return new byte[0];
    }

    /**
     * Build a state from the given array of states.
     *
     * @param bytes bute arrau
     * @return a state drepresented by the given array of states.
     * @throws IOException
     */
    @Override
    public AgentState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    /**
     * Each model is associated with a set of measures. This method returns the
     * array of measure names identified by strings.
     *
     * @return the array of measure names.
     */
    @Override
    public String[] measures() {
        return dopm.measures();
    }

    /**
     * Compute the measure <code>m</code> on the state <code>state</code>.
     *
     * @param m     name of the measure to compute.
     * @param state state to measure.
     * @return the value of measure <code>m</code> on state <code>state</code>.
     */
    @Override
    public double measure(String m, AgentState state) {
        return dopm.measure(m, state);
    }

    /**
     * Returns the measure with name <code>m</code>.
     *
     * @param m measure name.
     * @return the measure with name <code>m</code>.
     */
    @Override
    public Measure<? super AgentState> getMeasure(String m) {
        return dopm.getMeasure(m);
    }

    /**
     * Returns the predicate associated with the given name.
     *
     * @param name predicate name.
     * @return the predicate associated with the given name.
     */
    @Override
    public Predicate<? super AgentState> getPredicate(String name) {
        return dopm.getPredicate(name);
    }

    /**
     * Returns the array containing the names of predicates defined in this model.
     *
     * @return the array containing the names of predicates defined in this model.
     */
    @Override
    public String[] predicates() {
        return dopm.predicates();
    }
}
