package it.unicam.quasylab.sibilla.core.models.dopm.expressions;

import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import java.util.Collections;
import java.util.List;

public class ExpressionContext {
    private final List<SibillaValue> agentValues;
    private final List<SibillaValue> senderValues;
    private final DataOrientedPopulationState state;
    public ExpressionContext(List<SibillaValue> agentValues, List<SibillaValue> senderValues, DataOrientedPopulationState state) {
        this.agentValues = agentValues;
        this.senderValues = senderValues;
        this.state = state;
    }

    public ExpressionContext(List<SibillaValue> agentValues, DataOrientedPopulationState state) {
        this.agentValues = agentValues;
        this.senderValues = Collections.emptyList();
        this.state = state;
    }

    public ExpressionContext(DataOrientedPopulationState state) {
        this.agentValues = Collections.emptyList();
        this.senderValues = Collections.emptyList();
        this.state = state;
    }

    public List<SibillaValue> getAgentValues() {
        return agentValues;
    }

    public List<SibillaValue> getSenderValues() {
        return senderValues;
    }

    public DataOrientedPopulationState getState() {
        return state;
    }
}