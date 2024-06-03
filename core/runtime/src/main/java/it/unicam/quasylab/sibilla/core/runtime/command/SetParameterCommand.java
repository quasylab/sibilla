package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;

public class SetParameterCommand extends AbstractSibillaCommand{
    String name;
    double value;

    public SetParameterCommand(RuntimeContext runtimeContext,String name, double value) {
        super(runtimeContext);
        this.name = name;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        rc.getCurrentModule().setParameter(name,value);
    }
}
