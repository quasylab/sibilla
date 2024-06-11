/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandAdapter implements CommandHandler {

    //private final Map<CommandName, List<CommandHandler>> handlers;

    private final Map<CommandName, CommandHandler> handlers;


    public CommandAdapter() {
        this.handlers = new HashMap<>();
    }


    public void recordHandler(CommandName commandName, CommandHandler handler) {
        handlers.put(commandName, handler);
    }

//    public void recordHandler(CommandName commandName, CommandHandler handler) {
//        List<CommandHandler> handlers = this.handlers.computeIfAbsent(commandName, k -> new LinkedList<>());
//        handlers.add(handler);
//    }

//    @Override
//    public boolean handle(Command command) throws CommandExecutionException {
//        List<CommandHandler> commandHandlers = handlers.get(command.name());
//        boolean flag = false;
//        if (commandHandlers != null) {
//            for (CommandHandler commandHandler : commandHandlers) {
//                flag |= commandHandler.handle(command);
//            }
//        }
//        return flag;
//    }

    public boolean isCommandHandable(CommandName commandName) {
        return handlers.containsKey(commandName);
    }

    @Override
    public CommandResult handle(Command command) throws CommandExecutionException {
        if(isCommandHandable(command.name()))
            return handlers.get(command.name()).handle(command);
        throw new CommandExecutionException("No handler found for command " + command.name());
    }
}
