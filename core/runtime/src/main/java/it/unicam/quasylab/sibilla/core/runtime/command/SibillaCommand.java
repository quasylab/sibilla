package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;

public interface SibillaCommand {
    public void execute() throws CommandExecutionException;
}
