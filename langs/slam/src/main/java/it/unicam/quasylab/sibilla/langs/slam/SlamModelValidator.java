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

import it.unicam.quasylab.sibilla.core.models.slam.MessageTag;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * This visitor is used to validate a model. In particular the
 */
public class SlamModelValidator extends SlamModelBaseVisitor<Boolean> {

    private final SymbolTable table = new SymbolTable();

    private final List<ParseError> errors = new LinkedList<>();

    private final TypeRegistry registry = new TypeRegistry();

    private String agentContext;

    private ExpressionContext context = ExpressionContext.NONE;

    /**
     * A model is valid if all its elements are valid.
     *
     * @param ctx the parse tree
     * @return true if the model is valid, false otherwise.
     */
    @Override
    public Boolean visitModel(SlamModelParser.ModelContext ctx) {
        boolean flag = true;
        for(SlamModelParser.ModelElementContext element: ctx.elements) {
            flag &= element.accept(this);
        }
        return flag;
    }

    /**
     * A constant declaration is valid if:
     * - its name has not used before;
     * - its value is a typeable expression.
     *
     * @param ctx the parse tree
     * @return true if the constant is valid, false otherwise.
     */
    @Override
    public Boolean visitDeclarationConstant(SlamModelParser.DeclarationConstantContext ctx) {
        try {
            String constantName = ctx.name.getText();
            if (table.isDeclaredElementWithName(constantName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.name, table.getDeclarationToken(constantName)), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
                return false;
            }
            if (ctx.value.accept(this)) {
                SlamType constType = ctx.expr().accept(new TypeInferenceVisitor(ExpressionContext.CONSTANT, table, table.getGlobalTypeSolver(), this.errors));
                if (!SlamType.NONE_TYPE.equals(constType)) {
                    table.addConstant(ctx, constType);
                    return true;
                }
            }
            return false;
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }

    private boolean recordAgentViews(List<SlamModelParser.DeclarationAgentContext> agents) {
        boolean result = true;
        for (SlamModelParser.DeclarationAgentContext agent : agents) {
            result &= recordAgentViews(agent);
        }
        return result;
    }

    private boolean recordAgentAttributes(List<SlamModelParser.DeclarationAgentContext> agents) {
        boolean result = true;
        for (SlamModelParser.DeclarationAgentContext agent : agents) {
            result &= recordAgentAttributes(agent);
        }
        return result;
    }

    private boolean recordAgentAttributes(SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        String agentName = agent.name.getText();
        for (SlamModelParser.AttributeDeclarationContext attribute : agent.attributes) {
            result &= recordAgentAttribute(agentName, attribute);
        }
        return result;
    }

    private boolean recordAgentViews(SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        String agentName = agent.name.getText();
        for (SlamModelParser.AttributeDeclarationContext view : agent.views) {
            result &= recordAgentView(agentName, view);
        }
        return result;
    }

    private boolean recordAgentAttribute(String agentName, SlamModelParser.AttributeDeclarationContext attribute) {
        String attributeName = attribute.name.getText();
        if (table.isDeclaredElementWithName(attributeName)) {
            this.errors.add(new ParseError(ParseUtil.duplicatedName(attribute.name, table.getDeclarationToken(agentName)), attribute.name.getLine(), attribute.name.getCharPositionInLine()));
            return false;
        }
        if (table.isAParameterOf(agentName, attributeName) || table.isAgentAttribute(agentName, attributeName)) {
            this.errors.add(ParseUtil.illegalAttributeDeclarationError(attribute.name));
            return false;
        }
//        SlamType attributeType = attribute.expr().accept(new TypeInferenceVisitor(ExpressionContext.AGENT_ATTRIBUTE,
//                table.attributeDeclarationTypeSolver(agentName),
//                this.errors
//        ));
//        if (SlamType.NONE_TYPE.equals(attributeType)) {
//            return false;
//        } else {
//            table.recordAgentAttribute(agentName, attributeName, attributeType);
//            return true;
//        }
        return false;
    }

    private boolean recordAgentView(String agentName, SlamModelParser.AttributeDeclarationContext view) {
        String attributeName = view.name.getText();
        if (table.isDeclaredElementWithName(attributeName)) {
            this.errors.add(new ParseError(ParseUtil.duplicatedName(view.name, table.getDeclarationToken(agentName)), view.name.getLine(), view.name.getCharPositionInLine()));
            return false;
        }
        if (table.isAParameterOf(agentName, attributeName) || table.isAgentAttribute(agentName, attributeName) || table.isAgentView(agentName, attributeName)) {
            this.errors.add(ParseUtil.illegalAttributeDeclarationError(view.name));
            return false;
        }
//        SlamType attributeType = view.expr().accept(new TypeInferenceVisitor(ExpressionContext.AGENT_ATTRIBUTE,
//                table,
//                Map.of(),
//                table.viewDeclarationTypeSolver(agentName),
//                this.errors
//        ));
//        if (SlamType.NONE_TYPE.equals(attributeType)) {
//            return false;
//        } else {
//            table.recordAgentView(agentName, attributeName, attributeType);
//            return true;
//        }
        return false;
    }

    private boolean recordAgents(List<SlamModelParser.DeclarationAgentContext> agents) {
        boolean result = true;
        for (SlamModelParser.DeclarationAgentContext agent : agents) {
            String agentName = agent.name.getText();
            if (table.isDeclaredElementWithName(agentName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(agent.name, table.getDeclarationToken(agentName)), agent.name.getLine(), agent.name.getCharPositionInLine()));
                result = false;
            } else {
//                table.addAgent(agent);
                result &= checkAttributesAndParamDeclaration(agent) && recordAgentParameter(agent);
            }
        }
        return result;
    }

    private boolean checkAttributesAndParamDeclaration(SlamModelParser.DeclarationAgentContext agent) {
        Map<String, SlamModelParser.AgentParameterContext> parameters = agent.params.stream().collect(Collectors.toMap(p -> p.name.getText(), p -> p));
        Map<String, SlamModelParser.AttributeDeclarationContext> attributes = agent.attributes.stream().collect(Collectors.toMap(p -> p.name.getText(), p -> p));
        Map<String, SlamModelParser.AttributeDeclarationContext> views = agent.attributes.stream().collect(Collectors.toMap(p -> p.name.getText(), p -> p));
        boolean result = checkForDuplicatedParameters(parameters, agent);
        result &= checkForDuplicatedAttribute(attributes, agent);
        result &= checkForDuplicatedView(views, agent);
        result &= checkIfParametersAreDistinctFromAttributesAndViews(parameters, attributes, views);
        result &= checkIfAttributesAndViewsAreDistinct(attributes, views);
        return result;
    }

    private boolean checkForDuplicatedParameters(Map<String, SlamModelParser.AgentParameterContext> parameters, SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        for (String name : parameters.keySet()) {
            Optional<SlamModelParser.AgentParameterContext> otherDeclaration = agent.params.stream().filter(parameters.get(name)::equals).filter(p -> name.equals(p.name.getText())).findFirst();
            if (otherDeclaration.isPresent()) {
                result = false;
                errors.add(ParseUtil.duplicatedParameterError(parameters.get(name).name, otherDeclaration.get().name));
            }
        }
        return result;
    }

    private boolean checkForDuplicatedAttribute(Map<String, SlamModelParser.AttributeDeclarationContext> attributes, SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        for (String name : attributes.keySet()) {
            Optional<SlamModelParser.AttributeDeclarationContext> otherDeclaration = agent.attributes.stream().filter(attributes.get(name)::equals).filter(p -> name.equals(p.name.getText())).findFirst();
            if (otherDeclaration.isPresent()) {
                result = false;
                errors.add(ParseUtil.duplicatedAttributeError(attributes.get(name).name, otherDeclaration.get().name));
            }
        }
        return result;
    }

    private boolean checkForDuplicatedView(Map<String, SlamModelParser.AttributeDeclarationContext> views, SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        for (String name : views.keySet()) {
            Optional<SlamModelParser.AttributeDeclarationContext> otherDeclaration = agent.views.stream().filter(views.get(name)::equals).filter(p -> name.equals(p.name.getText())).findFirst();
            if (otherDeclaration.isPresent()) {
                result = false;
                errors.add(ParseUtil.duplicatedAttributeError(views.get(name).name, otherDeclaration.get().name));
            }
        }
        return result;
    }


    private boolean checkIfAttributesAndViewsAreDistinct(Map<String, SlamModelParser.AttributeDeclarationContext> attributes, Map<String, SlamModelParser.AttributeDeclarationContext> views) {
        boolean result = true;
        for (String attributeName : attributes.keySet()) {
            if (views.containsKey(attributeName)) {
                this.errors.add(ParseUtil.duplicatedAttributeError(attributes.get(attributeName).name, views.get(attributeName).name));
                result = false;
            }
        }
        return result;
    }

    private boolean checkIfParametersAreDistinctFromAttributesAndViews(Map<String, SlamModelParser.AgentParameterContext> parameters, Map<String, SlamModelParser.AttributeDeclarationContext> attributes, Map<String, SlamModelParser.AttributeDeclarationContext> views) {
        boolean result = true;
        for (String parameterName : parameters.keySet()) {
            if (parameters.containsKey(parameterName)) {
                this.errors.add(ParseUtil.illegalParameterDeclarationError(parameters.get(parameterName), attributes.get(parameterName)));
                result = false;
            }
            if (views.containsKey(parameterName)) {
                this.errors.add(ParseUtil.illegalParameterDeclarationError(parameters.get(parameterName), views.get(parameterName)));
                result = false;
            }
        }
        return result;
    }

    private boolean recordAgentParameter(SlamModelParser.DeclarationAgentContext agent) {
        boolean result = true;
        String agentName = agent.name.getText();
        for (SlamModelParser.AgentParameterContext par : agent.params) {
            //table.recordAgentParameter(agentName, par.start, par.name.getText(), SlamType.getTypeOf(par.type.getText()));
        }
        return result;
    }


    @Override
    public Boolean visitDeclarationPredicate(SlamModelParser.DeclarationPredicateContext ctx) {
        try {
            String predicateName = ctx.name.getText();
            if (table.isDeclaredElementWithName(predicateName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.name, table.getDeclarationToken(predicateName)), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
                return false;
            }
            this.context = ExpressionContext.PREDICATE;
//            if (ctx.value.accept(this)) {
//                table.addPredicate(ctx);
//                SlamType predicateType = ctx.expr().accept(new TypeInferenceVisitor(
//                        ExpressionContext.PREDICATE,
//                        this.registry, errors));
//                return SlamType.NONE_TYPE.equals(predicateType) || SlamType.BOOLEAN_TYPE.equals(predicateType);
//            }
            return false;
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }



    @Override
    public Boolean visitDeclarationParameter(SlamModelParser.DeclarationParameterContext ctx) {
        try {
            String parameterName = ctx.name.getText();
            if (table.isDeclaredElementWithName(parameterName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.name, table.getDeclarationToken(parameterName)), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
                return false;
            }
            if (new TypeInferenceVisitor(ExpressionContext.PARAMETER, table, table.getGlobalTypeSolver(), errors).checkType(SlamType.REAL_TYPE, ctx.expr())) {
                table.addParameter(ctx);
                return true;
            }
            return false;
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }

    @Override
    public Boolean visitDeclarationMeasure(SlamModelParser.DeclarationMeasureContext ctx) {
        try {
            String parameterName = ctx.name.getText();
            if (table.isDeclaredElementWithName(parameterName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.name, table.getDeclarationToken(parameterName)), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
                return false;
            }
            if (ctx.expr().accept(this)) {
                table.addMeasure(ctx);
                this.context = ExpressionContext.MEASURE;
                //SlamType actualType = ctx.expr().accept(new TypeInferenceVisitor(ExpressionContext.PREDICATE, table, Map.of(), table::solveTypeFromMeasuresAndPredicates, errors));
                //return SlamType.NONE_TYPE.equals(actualType) || SlamType.REAL_TYPE.equals(actualType);
            }
            return false;
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }

    @Override
    public Boolean visitDeclarationSystem(SlamModelParser.DeclarationSystemContext ctx) {
        try {
            String systemName = ctx.name.getText();
            if (table.isDeclaredElementWithName(systemName)) {
                this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.name, table.getDeclarationToken(systemName)), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
                return false;
            }
            table.addSystem(ctx);
            this.context = ExpressionContext.SYSTEM;
            return ctx.agentExpression.accept(this);
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }

    @Override
    public Boolean visitDeclarationMessage(SlamModelParser.DeclarationMessageContext ctx) {
        String tagName = ctx.tag.getText();
        if (table.isDeclaredElementWithName(tagName)) {
            this.errors.add(new ParseError(ParseUtil.duplicatedName(ctx.tag, table.getDeclarationToken(tagName)), ctx.tag.getLine(), ctx.tag.getCharPositionInLine()));
            return false;
        }
        table.addMessage(ctx);
        return true;
    }

    @Override
    public Boolean visitDeclarationAgent(SlamModelParser.DeclarationAgentContext ctx) {
        try {
            this.agentContext = ctx.name.getText();
            return validateAgentStateDeclaration(ctx.states) && validateTimePassingFunction(ctx.commands);
        } finally {
            this.agentContext = null;
        }
    }

    private boolean validateTimePassingFunction(List<SlamModelParser.AssignmentCommandContext> commands) {
        try {
            this.context = ExpressionContext.AGENT_TIME_UPDATE;
            boolean result = true;
            for (SlamModelParser.AssignmentCommandContext command : commands) {
                result &= command.accept(this);
            }
            return result;
        } finally {
            this.context = ExpressionContext.NONE;
        }
    }


    private boolean validateAgentStateDeclaration(List<SlamModelParser.AgentStateDeclarationContext> states) {
        SlamModelParser.AgentStateDeclarationContext initialState = null;
        boolean result = true;
        for (SlamModelParser.AgentStateDeclarationContext s : states) {
            if (s.isInit != null) {
                if (initialState != null) {
                    this.errors.add(ParseUtil.duplicatedInitialStateError(s.isInit));
                    result = false;
                } else {
                    initialState = s;
                }
            }
            result &= s.accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitAgentStateDeclaration(SlamModelParser.AgentStateDeclarationContext ctx) {
        boolean result = true;
        for (SlamModelParser.StateMessageHandlerContext mh : ctx.handlers) {
            result &= mh.accept(this);
        }
        result &= ctx.sojournTimeExpression.accept(this);
        if (agentContext != null) {
            result &= hasType(ExpressionContext.AGENT_SOJOURN_TIME, Map.of(), table.getAgentStateTypeSolver(agentContext), SlamType.REAL_TYPE, ctx.sojournTimeExpression);
        }
        result &= ctx.activityBlock().accept(this);
        result &= validateTimePassingFunction(ctx.commands);
        return result;
    }


    @Override
    public Boolean visitAgentExpression(SlamModelParser.AgentExpressionContext ctx) {
        boolean result = true;
        String agentName = ctx.name.getText();
        if (!table.isAnAgent(agentName)) {
            this.errors.add(ParseUtil.unknownAgentError(ctx.name));
            return false;
        }
        SlamType[] agentParameters = table.getAgentParameters(agentName);
        if (ctx.args.size() != agentParameters.length) {
            this.errors.add(ParseUtil.illegalNumberOfParameters(ctx.name, agentParameters.length, ctx.args.size()));
            return false;
        }
//        for (int i = 0; i < agentParameters.length; i++) {
//            SlamType argType = ctx.args.get(i).accept(new TypeInferenceVisitor(ExpressionContext.SYSTEM, table, Map.of(), table::solveTypeFromConstants, errors));
//            if (!SlamType.NONE_TYPE.equals(argType) && !agentParameters[i].equals(argType)) {
//                //this.errors.add(ParseUtil.typeError(agentParameters[i], argType, ctx.args.get(i).start));
//                result = false;
//            }
//        }
        if (ctx.copies != null) {
            //result &= SlamType.INTEGER_TYPE.equals(ctx.copies.accept(new TypeInferenceVisitor(ExpressionContext.SYSTEM, table, Map.of(), table::solveTypeFromConstants, errors)));
        }
        return result;
    }

    private Boolean hasType(ExpressionContext context, Map<String, SlamType> localVariables, Function<String, SlamType> typeSolver, SlamType expected, SlamModelParser.ExprContext expr) {
//        SlamType actual = expr.accept(new TypeInferenceVisitor(context, table, localVariables, typeSolver, errors));
//        if (!SlamType.NONE_TYPE.equals(actual) && !expected.equals(actual)) {
//            this.errors.add(ParseUtil.typeError(expected, actual, expr.start));
//            return false;
//        } else {
//            return true;
//        }
        return false;
    }

    @Override
    public Boolean visitStateMessageHandler(SlamModelParser.StateMessageHandlerContext ctx) {
        String tagName = ctx.tag.getText();
        if (!table.isAMessageTag(tagName)) {
            this.errors.add(ParseUtil.unknownTagError(ctx.tag));
            return false;
        }
        this.context = ExpressionContext.AGENT_MESSAGE_HANDLER;
        boolean result = recordHandlerVariables(ctx.tag, ctx.content, table.getMessageTag(tagName));
        result &= ctx.agentGuard.accept(this);
        result &= ctx.guard.accept(this);
        result &= ctx.activityBlock().accept(this);
        this.context = ExpressionContext.NONE;
        //this.localVariables = new HashMap<>();
        return result;
    }

    private boolean recordHandlerVariables(Token tag, List<SlamModelParser.ValuePatternContext> patterns, MessageTag messageTag) {
        if (patterns.size() != messageTag.getArity()) {
            this.errors.add(ParseUtil.illegalNumberOfMessageElements(tag, messageTag.getArity(), patterns.size()));
            return false;
        }
        int counter = 0;
        boolean result = true;
        for (SlamModelParser.ValuePatternContext p : patterns) {
            if (p instanceof SlamModelParser.PatternVariableContext) {
                SlamModelParser.PatternVariableContext patternVariableContext = (SlamModelParser.PatternVariableContext) p;
                SlamType type = messageTag.getTypeOf(counter);
                String localVariableName = patternVariableContext.name.getText();
//                if (localVariables.containsKey(localVariableName)) {
//                    this.errors.add(ParseUtil.nameAlreadyUsedInMessagePattern(patternVariableContext.name));
//                    result = false;
//                } else {
//                    localVariables.put(localVariableName, type);
//                }
            }
            counter++;
        }
        return result;
    }

    @Override
    public Boolean visitProbabilitySelectionBlock(SlamModelParser.ProbabilitySelectionBlockContext ctx) {
        boolean result = true;
        for (SlamModelParser.SelectCaseContext selectCase : ctx.cases) {
            result &= selectCase.accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitSelectCase(SlamModelParser.SelectCaseContext ctx) {
//        boolean result = hasType(ExpressionContext.AGENT_COMMAND,localVariables,table.getAgentStateTypeSolver(agentContext),SlamType.REAL_TYPE, ctx.weight);
//        result &= hasType(ExpressionContext.AGENT_COMMAND,localVariables,table.getAgentStateTypeSolver(agentContext),SlamType.BOOLEAN_TYPE, ctx.guard);
//        result &= ctx.nextStateBlock().accept(this);
//        return result;
        return false;
    }

    @Override
    public Boolean visitSkipBlock(SlamModelParser.SkipBlockContext ctx) {
        return true;
    }

    @Override
    public Boolean visitNextStateBlock(SlamModelParser.NextStateBlockContext ctx) {
        if (!table.hasState(agentContext, ctx.name.getText())) {
            this.errors.add(ParseUtil.unknownAgentStateError(agentContext, ctx.name));
            return false;
        }
        return ctx.agentCommandBlock().accept(this);
    }

    @Override
    public Boolean visitAgentCommandBlock(SlamModelParser.AgentCommandBlockContext ctx) {
        boolean result = true;
        for (SlamModelParser.AgentCommandContext cmd: ctx.commands) {
            result &= ctx.agentCommand.accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitIfThenElseCommand(SlamModelParser.IfThenElseCommandContext ctx) {
//        boolean result = hasType(context, localVariables, table.getAgentStateTypeSolver(agentContext), SlamType.BOOLEAN_TYPE, ctx.guard);
//        result &= ctx.thenCommand.accept(this);
//        result &= ctx.elseCommand.accept(this);
//        return result;
        return false;
    }

    @Override
    public Boolean visitSendCommand(SlamModelParser.SendCommandContext ctx) {
        SlamModelParser.MessageExpressionContext message = ctx.content;
        String tagName = message.tag.getText();
        if (!table.isAMessageTag(tagName)) {
            this.errors.add(ParseUtil.unknownTagError(message.tag));
            return false;
        }
        MessageTag messageTag = table.getMessageTag(tagName);
        if (message.elements.size() != messageTag.getArity()) {
            this.errors.add(ParseUtil.illegalNumberOfMessageElements(message.tag, messageTag.getArity(), message.elements.size()));
            return false;
        }
        boolean result = true;
//        for(int i=0; i< messageTag.getArity(); i++) {
//            result = hasType(context, localVariables, table.getAgentStateTypeSolver(agentContext), messageTag.getTypeOf(i), message.elements.get(i));
//        }
//        result &= ctx.target.accept(this);
//        result &= hasType(context, localVariables, table.getAgentStateTypeSolver(agentContext), SlamType.REAL_TYPE, ctx.time);
        return result;
    }

    @Override
    public Boolean visitAssignmentCommand(SlamModelParser.AssignmentCommandContext ctx) {
        if (!table.isAssignable(context, agentContext, ctx.name.getText())) {
            this.errors.add(ParseUtil.illegalAssignmentError(ctx.name));
            return false;
        }
        SlamType targetType = table.getTypeOf(agentContext, ctx.name.getText());
//        return hasType(context, localVariables, table.getAgentStateTypeSolver(agentContext), targetType, ctx.expr());
        return false;
    }

}
