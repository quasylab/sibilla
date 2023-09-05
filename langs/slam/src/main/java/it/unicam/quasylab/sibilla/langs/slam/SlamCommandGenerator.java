/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.MessageRepository;
import it.unicam.quasylab.sibilla.core.models.slam.MessageTag;
import it.unicam.quasylab.sibilla.core.models.slam.SlamInternalRuntimeException;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentMessage;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentCommand;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import it.unicam.quasylab.sibilla.core.models.slam.data.VariableRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is used to generate the {@link SlamAgentCommand} from a spceficiation.
 */
public class SlamCommandGenerator extends SlamModelBaseVisitor<SlamAgentCommand> {

    private final VariableRegistry variableRegistry;
    private final Function<String, Optional<SibillaValue>> globalAssignments;

    private final MessageRepository messageRepository;

    private final SlamAgentDefinitions agentDefinitions;

    public SlamCommandGenerator(VariableRegistry variableRegistry, Function<String, Optional<SibillaValue>> globalAssignments, MessageRepository messageRepository, SlamAgentDefinitions agentDefinitions) {
        this.variableRegistry = variableRegistry;
        this.globalAssignments = globalAssignments;
        this.messageRepository = messageRepository;
        this.agentDefinitions = agentDefinitions;
    }


    @Override
    public SlamAgentCommand visitAgentCommandBlock(SlamModelParser.AgentCommandBlockContext ctx) {
        return SlamAgentCommand.combine(ctx.commands.stream().map(c -> c.accept(this)).toArray(SlamAgentCommand[]::new));
    }

    @Override
    public SlamAgentCommand visitAgentCommandIfThenElse(SlamModelParser.AgentCommandIfThenElseContext ctx) {
        Function<SlamExpressionEvaluationParameters, SibillaValue> guardEvaluationFunction = ctx.guard.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
        SlamAgentCommand thenCommand = ctx.thenCommand.accept(this);
        if (ctx.elseCommand == null) {
            return SlamAgentCommand.ifCommand((rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, guardEvaluationFunction), thenCommand);
        }
        SlamAgentCommand elseCommand = ctx.elseCommand.accept(this);
        return SlamAgentCommand.ifThenElse((rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, guardEvaluationFunction), thenCommand, elseCommand);
    }

    @Override
    public SlamAgentCommand visitAgentCommandSend(SlamModelParser.AgentCommandSendContext ctx) {
        MessageTag messageTag = getAgentMessageTag(ctx.messageExpression());
        List<Function<SlamExpressionEvaluationParameters, SibillaValue>> agentMessageElements = getAgentMessageElements(ctx.messageExpression());
        Function<SlamExpressionEvaluationParameters, SibillaValue> timeFunction = ctx.time.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
        BiPredicate<AgentStore, SlamAgent> targetPredicate;
        if (ctx.target != null) {
            targetPredicate = ctx.target.accept(new AgentPatternGenerator(globalAssignments, variableRegistry, agentDefinitions));
        } else {
            targetPredicate = (store, agent) -> true;
        }
        return SlamAgentCommand.send((rg, store) ->
                        new AgentMessage(
                                messageTag,
                                agentMessageElements.stream().map(f -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, f)).toArray(SibillaValue[]::new),
                                a -> targetPredicate.test(store, a)
                        ),
                (rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, timeFunction).doubleOf()
        );
    }

    private MessageTag getAgentMessageTag(SlamModelParser.MessageExpressionContext messageExpressionContext) {
        MessageTag messageTag = messageRepository.getTag(messageExpressionContext.tag.getText());
        if (messageTag == null) {
            throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(messageExpressionContext.tag));
        }
        if (messageTag.getArity() != messageExpressionContext.elements.size()) {
            throw new SlamInternalRuntimeException(ParseUtil.illegalNumberOfMessageElementsMessage(messageExpressionContext.tag, messageTag.getArity(), messageExpressionContext.elements.size()));
        }
        return messageTag;
    }

    private List<Function<SlamExpressionEvaluationParameters, SibillaValue>> getAgentMessageElements(SlamModelParser.MessageExpressionContext messageExpressionContext) {
        return messageExpressionContext.elements.stream().map(e -> e.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions))).collect(Collectors.toList());
    }


    @Override
    public SlamAgentCommand visitAgentCommandAssignment(SlamModelParser.AgentCommandAssignmentContext ctx) {
        Optional<AgentVariable> optionalAgentVariable = variableRegistry.getVariable(ctx.name.getText());
        if (optionalAgentVariable.isEmpty()) {
            throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(ctx.name));
        }
        Function<SlamExpressionEvaluationParameters, SibillaValue> valueEvaluationFunction = ctx.expr().accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
        return SlamAgentCommand.setCommand(optionalAgentVariable.get(), (rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, valueEvaluationFunction));
    }

    @Override
    public SlamAgentCommand visitAgentCommandLet(SlamModelParser.AgentCommandLetContext ctx) {
        Optional<AgentVariable> variable = variableRegistry.getVariable(ctx.name.getText());
        if (variable.isEmpty()) {
            throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(ctx.name));
        }
        Function<SlamExpressionEvaluationParameters, SibillaValue> valueEvaluationFunction = ctx.expr().accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
        return SlamAgentCommand.letIn(variable.get(), (rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, valueEvaluationFunction), ctx.agentCommandBlock().accept(this));
    }
}
