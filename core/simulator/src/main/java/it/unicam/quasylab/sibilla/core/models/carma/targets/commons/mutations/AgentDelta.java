package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;

public record AgentDelta(Agent agent, long delta) {
}
