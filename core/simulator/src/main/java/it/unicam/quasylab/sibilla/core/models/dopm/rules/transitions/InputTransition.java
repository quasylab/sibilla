package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputTransition {
    private Predicate<Agent> predicate;
    private Predicate<Agent> sender_predicate;
    private Function<DataOrientedPopulationState, Double> probability;
    private BiFunction<Agent, Agent, Agent> post;

    public InputTransition(Predicate<Agent> predicate, Predicate<Agent> sender_predicate, Function<DataOrientedPopulationState, Double> probability, BiFunction<Agent, Agent, Agent> post) {
        this.predicate = predicate;
        this.sender_predicate = sender_predicate;
        this.probability = probability;
        this.post = post;
    }

    public Predicate<Agent> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<Agent> predicate) {
        this.predicate = predicate;
    }

    public Predicate<Agent> getSender_predicate() {
        return sender_predicate;
    }

    public void setSender_predicate(Predicate<Agent> sender_predicate) {
        this.sender_predicate = sender_predicate;
    }

    public Function<DataOrientedPopulationState, Double> getProbability() {
        return probability;
    }

    public void setProbability(Function<DataOrientedPopulationState, Double> probability) {
        this.probability = probability;
    }

    public BiFunction<Agent, Agent, Agent> getPost() {
        return post;
    }

    public void setPost(BiFunction<Agent, Agent, Agent> post) {
        this.post = post;
    }
}
