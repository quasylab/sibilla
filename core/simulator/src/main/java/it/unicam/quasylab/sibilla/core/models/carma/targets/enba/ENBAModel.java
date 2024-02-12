package it.unicam.quasylab.sibilla.core.models.carma.targets.enba;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.DataOrientedPopulationModel;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class ENBAModel implements Model<AgentState>, ContinuousTimeMarkovProcess<AgentState> {
    private final DataOrientedPopulationModel dopm;

    public ENBAModel(List<Rule> rules, Map<String, Measure<AgentState>> measures, Map<String, Predicate<AgentState>> predicates) {
        this.dopm = new DataOrientedPopulationModel(measures, predicates, rules);
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
