package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class OutputTransition {
    private Predicate<Agent> predicate;
    private BiFunction<DataOrientedPopulationState, Agent, Double> rate;
    private Function<Agent, Agent> post;

    public OutputTransition(Predicate<Agent> predicate, BiFunction<DataOrientedPopulationState, Agent, Double> rate, Function<Agent, Agent> post) {
        this.predicate = predicate;
        this.rate = rate;
        this.post = post;
    }


    public Predicate<Agent> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<Agent> predicate) {
        this.predicate = predicate;
    }

    public BiFunction<DataOrientedPopulationState, Agent, Double> getRate() {
        return rate;
    }

    public void setRate(BiFunction<DataOrientedPopulationState, Agent, Double> rate) {
        this.rate = rate;
    }

    public Function<Agent, Agent> getPost() {
        return post;
    }

    public void setPost(Function<Agent, Agent> post) {
        this.post = post;
    }
}
