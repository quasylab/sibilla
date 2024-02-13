package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExpressionContext {
    private final Map<String,SibillaValue> agentValues;
    private final Map<String,SibillaValue> senderValues;
    private final AgentState state;

    public ExpressionContext(Map<String,SibillaValue> agentValues, Map<String,SibillaValue> senderValues, AgentState state) {
        this.agentValues = agentValues;
        this.senderValues = senderValues;
        this.state = state;
    }

    public ExpressionContext(Map<String,SibillaValue> agentValues, AgentState state) {
        this.agentValues = agentValues;
        this.senderValues = Collections.emptyMap();
        this.state = state;
    }

    public ExpressionContext(AgentState state) {
        this.agentValues = Collections.emptyMap();
        this.senderValues = Collections.emptyMap();
        this.state = state;
    }

    public Map<String,SibillaValue> getAgentValues() {
        return agentValues;
    }

    public Map<String,SibillaValue> getSenderValues() {
        return senderValues;
    }

    public AgentState getState() {
        return state;
    }
}