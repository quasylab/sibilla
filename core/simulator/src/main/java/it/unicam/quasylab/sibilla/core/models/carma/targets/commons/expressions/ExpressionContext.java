package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import java.util.Collections;
import java.util.List;

public class ExpressionContext {
    private final List<SibillaValue> agentValues;
    private final List<SibillaValue> senderValues;
    private final AgentState state;

    public ExpressionContext(List<SibillaValue> agentValues, List<SibillaValue> senderValues, AgentState state) {
        this.agentValues = agentValues;
        this.senderValues = senderValues;
        this.state = state;
    }

    public ExpressionContext(List<SibillaValue> agentValues, AgentState state) {
        this.agentValues = agentValues;
        this.senderValues = Collections.emptyList();
        this.state = state;
    }

    public ExpressionContext(AgentState state) {
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

    public AgentState getState() {
        return state;
    }
}