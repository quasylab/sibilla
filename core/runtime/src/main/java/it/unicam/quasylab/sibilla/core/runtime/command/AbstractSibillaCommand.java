package it.unicam.quasylab.sibilla.core.runtime.command;

public abstract class AbstractSibillaCommand implements SibillaCommand {

    RuntimeContext rc;

    public AbstractSibillaCommand(RuntimeContext runtimeContext) {
        this.rc = runtimeContext;
    }


}
