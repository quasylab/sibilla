package it.unicam.quasylab.sibilla.core.models.carma.targets.enba;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.Process;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ENBAModelDefinition implements ModelDefinition<AgentState> {
    private Map<String, Function<RandomGenerator, AgentState>> states;
    private Map<String, Measure<AgentState>> measures;
    private Map<String, Predicate<AgentState>> predicates;
    private ENBAModel model;

    public ENBAModelDefinition(Map<String, Function<RandomGenerator, AgentState>> states, Map<String, Measure<AgentState>> measures, Map<String, Predicate<AgentState>> predicates, List<Process> processes) {
        this.states = states;
        this.measures = measures;
        this.predicates = predicates;
        this.model = new ENBAModel(processes, this.measures, this.predicates);
    }

    /**
     * Reset all the model parameters to their default values.
     */
    @Override
    public void reset() {

    }

    /**
     * Resets the parameter having the given name to its default value.
     *
     * @param name the name of parameter to reset.
     */
    @Override
    public void reset(String name) {

    }

    /**
     * Return the value associated with the given parameter.
     *
     * @param name name of parameter whose value is returned.
     * @return the value associated with the given parameter.
     */
    @Override
    public SibillaValue getParameterValue(String name) {
        return SibillaValue.ERROR_VALUE;
    }

    /**
     * Returns the {@link EvaluationEnvironment} containing all the parameters in this model.
     *
     * @return the {@link EvaluationEnvironment} containing all the parameters in this model.
     */
    @Override
    public EvaluationEnvironment getEnvironment() {
        return new EvaluationEnvironment();
    }

    /**
     * Returns the number of parameters needed to build default initial state.
     *
     * @return the number of parameters needed to build default initial state.
     */
    @Override
    public int defaultConfigurationArity() {
        return 0;
    }

    /**
     * Returns the number of parameters needed to build initial configuration having the given name.
     *
     * @param name the name of the configuration whose number of parameters is returned.
     * @return the number of parameters needed to build initial configuration having the given name.
     */
    @Override
    public int configurationArity(String name) {
        return 0;
    }

    /**
     * Returns the array of possible initial states defined in the model.
     *
     * @return the array of possible initial states defined in the model.
     */
    @Override
    public String[] configurations() {
        if(this.states != null) {
            return states.keySet().toArray(new String[0]);
        } else{
            return new String[0];
        }
    }

    /**
     * Returns the configuration associated with the given name by using the given arguments.
     *
     * @param name
     * @param args arguments to use in state creation.
     * @return the default state associated the given arguments.
     */
    @Override
    public Function<RandomGenerator, AgentState> getConfiguration(String name, double... args) {
        if(this.states != null && states.containsKey(name)) {
            return states.get(name);
        } else {
            return (r) -> new AgentState();
        }
    }

    /**
     * Create the default state (that is the first one in the array) with
     * the given arguments.
     *
     * @param args arguments to use in state creation.
     * @return the default state associated the given arguments.
     */
    @Override
    public Function<RandomGenerator, AgentState> getDefaultConfiguration(double... args) {
        if(this.states != null) {
            return states.entrySet().iterator().next().getValue();
        } else {
            return (r) -> new AgentState();
        }
    }

    /**
     * Returns a string describing the state having the given name.
     *
     * @param name the name of the state whose info are provided
     * @return a string describing the state having the given name.
     */
    @Override
    public String getStateInfo(String name) {
        if(this.states != null && states.containsKey(name)) {
            return "State exists";
        } else {
            return "Non-existent state";
        }
    }

    /**
     * Creates a new {@link Model}.
     *
     * @return a model built from a given set of parameters.
     */
    @Override
    public Model<AgentState> createModel() {
        return this.model;
    }

    /**
     * Returns true if the given name is associated with an initial configuration.
     *
     * @param name the name is tested the association with an initial configuration.
     * @return true if the given name is associated with an initial configuration.
     */
    @Override
    public boolean isAnInitialConfiguration(String name) {
        return states != null && states.containsKey(name);
    }
}
