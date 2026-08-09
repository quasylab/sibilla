package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import java.util.stream.Stream;

public record MutationResult(Stream<AgentDelta> agentDeltaStream, long nonMutated) {
}
