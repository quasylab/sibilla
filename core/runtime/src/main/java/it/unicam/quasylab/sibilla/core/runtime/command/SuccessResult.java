package it.unicam.quasylab.sibilla.core.runtime.command;

public sealed interface SuccessResult extends CommandResult permits BooleanResult, DoubleResult, FPTResult, LongResult, MapResult, StringArrayResult, StringResult, VoidResult {
}
