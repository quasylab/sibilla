package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;

import static it.unicam.quasylab.sibilla.core.runtime.command.RuntimeMessage.UNKNOWN_MODULE_MESSAGE;

public class LoadModuleCommand extends AbstractSibillaCommand{
    String name;

    public LoadModuleCommand(RuntimeContext runtimeContext, String name) {
        super(runtimeContext);
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        SibillaModule module = rc.getModuleIndex().get(name);
        if (module == null) {
            throw new CommandExecutionException(String.format(UNKNOWN_MODULE_MESSAGE,name));
        }
        rc.getCurrentModule().clear();
        rc.setCurrentModule(module);
    }
}
