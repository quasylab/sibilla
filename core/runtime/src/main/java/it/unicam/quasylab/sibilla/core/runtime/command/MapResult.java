package it.unicam.quasylab.sibilla.core.runtime.command;

import java.util.Map;

public record MapResult(Map<String, double[][]> value) implements SuccessResult {
}
