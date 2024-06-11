package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;

public record FPTResult(FirstPassageTime<? extends State> value) implements SuccessResult {
}
