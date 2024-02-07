package it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.mutations;

import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.AgentDelta;

import java.util.stream.Stream;

public record MutationResult(Stream<AgentDelta> agentDeltaStream, long nonMutated) {
}
