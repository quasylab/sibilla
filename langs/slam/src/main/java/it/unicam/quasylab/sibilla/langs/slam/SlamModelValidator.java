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
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.function.IntFunction;


/**
 * This visitor is used to validate a model. In particular the
 */
public class SlamModelValidator extends SlamModelBaseVisitor<Boolean> {

    private final Map<String, Token> uniqueTokens = new HashMap<>();

    private final ErrorCollector errors;

    private final TypeRegistry registry = new TypeRegistry();


    private final AgentTable agentTable = new AgentTable();

    private final MessageRepository messageRepository = new MessageRepository();
    private final Map<String, AgentValidator> agentValidators = new HashMap<>();

    public SlamModelValidator(ErrorCollector errors) {
        this.errors = errors;
    }

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
        if (checkAndRecordGlobalName(ctx.name)&&ctx.value.accept(this)) {
            SlamType constType = ctx.expr().accept(new TypeInferenceVisitor(ExpressionContext.CONSTANT, registry, this.errors));
            if (!SlamType.NONE_TYPE.equals(constType)) {
                registry.add(ctx.name.getText(), constType);
                return true;
            }
        }
        return false;
    }


    @Override
    public Boolean visitDeclarationPredicate(SlamModelParser.DeclarationPredicateContext ctx) {
        SlamType predicateType = ctx.expr().accept(new TypeInferenceVisitor(
                ExpressionContext.PREDICATE,
                this.registry, errors));
        return SlamType.NONE_TYPE.equals(predicateType) || SlamType.BOOLEAN_TYPE.equals(predicateType);
    }



    @Override
    public Boolean visitDeclarationParameter(SlamModelParser.DeclarationParameterContext ctx) {
        if (checkAndRecordGlobalName(ctx.name)) {
            return new TypeInferenceVisitor(ExpressionContext.PARAMETER, registry, errors).checkType(SlamType.REAL_TYPE, ctx.expr());
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitDeclarationMeasure(SlamModelParser.DeclarationMeasureContext ctx) {
        if (checkAndRecordGlobalName(ctx.name)&&ctx.expr().accept(this)) {
            SlamType actualType = ctx.expr().accept(new TypeInferenceVisitor(ExpressionContext.MEASURE, registry,errors));
            return SlamType.REAL_TYPE.equals(actualType);
        }
        return false;
    }

    @Override
    public Boolean visitDeclarationSystem(SlamModelParser.DeclarationSystemContext ctx) {
        if (checkAndRecordGlobalName(ctx.name)) {
            return new SystemValidator(ctx).vaidate();
        }
        return false;
    }

    @Override
    public Boolean visitDeclarationMessage(SlamModelParser.DeclarationMessageContext ctx) {
        if (checkAndRecordGlobalName(ctx.tag)) {

        }
        return false;
    }

    private boolean checkAndRecordGlobalName(Token tag) {
        if (isValidName(tag)) {
            uniqueTokens.put(tag.getText(), tag);
            return true;
        } else {
            return false;
        }
    }

    private boolean isValidName(Token tag) {
        String name = tag.getText();
        if (uniqueTokens.containsKey(name)) {
            this.errors.record(new ParseError(ParseUtil.duplicatedName(tag, uniqueTokens.get(name)), tag.getLine(), tag.getCharPositionInLine()));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean visitDeclarationAgent(SlamModelParser.DeclarationAgentContext ctx) {
        if (uniqueTokens.containsKey(ctx.name.getText())) {
            AgentValidator validator = new AgentValidator(ctx);
            agentValidators.put(ctx.name.getText(), validator);
            return validator.recordAttributes();
        }
        return false;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }


    private class AgentValidator extends SlamModelBaseVisitor<Boolean> {

        private final Map<String, Token> agentParametersAndAttributes = new HashMap<>();

        private final Map<String, Token> agentStates = new HashMap<>();

        private final Map<String, SlamType> localVariables = new HashMap<>();

        private final String agentName;

        private final SlamModelParser.DeclarationAgentContext agentDeclaration;
        private boolean validationFlag = true;
        private Token initialState;

        public AgentValidator(SlamModelParser.DeclarationAgentContext ctx) {
            this.agentDeclaration = ctx;
            this.agentName = ctx.name.getText();
            agentTable.addAgent(agentName, agentDeclaration);
        }

        @Override
        protected Boolean defaultResult() {
            return true;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate&&nextResult;
        }

        public boolean validate() {
            checkStates();
            agentDeclaration.commands.forEach(this::checkViewAssignment);
            return validationFlag;
        }

        private void checkViewAssignment(SlamModelParser.AgentCommandAssignmentContext agentCommandAssignmentContext) {
            if (agentTable.isView(agentName, agentCommandAssignmentContext.name.getText())) {
                validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_VIEW, this::getTypeOf, errors).checkType(agentTable.getTypeOf(agentName, agentCommandAssignmentContext.name.getText()), agentCommandAssignmentContext.expr());
            } else {
                errors.record(ParseUtil.illegalAssignmentError(agentCommandAssignmentContext.name));
                validationFlag = false;
            }
        }

        private void checkStates() {
            agentDeclaration.states.forEach(this::recordState);
            agentDeclaration.states.forEach(this::checkState);
        }

        private void checkState(SlamModelParser.AgentStateDeclarationContext stateDeclaration) {
            if (stateDeclaration.isInit != null) {
                setInitialState(stateDeclaration.name);
            }
            stateDeclaration.handlers.forEach(h -> h.accept(this));
            if (stateDeclaration.sojournTimeExpression != null) {
                validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_SOJOURN_TIME, this::getTypeOf, errors).checkType(SlamType.REAL_TYPE, stateDeclaration.sojournTimeExpression);
                stateDeclaration.activityBlock().accept(this);
            }
            stateDeclaration.handlers.forEach(h -> h.accept(this));
            stateDeclaration.commands.forEach(this::checkAttributeAssignment);
        }

        private void checkAttributeAssignment(SlamModelParser.AgentCommandAssignmentContext agentCommandAssignmentContext) {
            if (agentTable.isAttribute(agentName, agentCommandAssignmentContext.name.getText())) {
                validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors).checkType(agentTable.getTypeOf(agentName, agentCommandAssignmentContext.name.getText()), agentCommandAssignmentContext.expr());
            } else {
                errors.record(ParseUtil.illegalAssignmentError(agentCommandAssignmentContext.name));
                validationFlag = false;
            }
        }

        @Override
        public Boolean visitNextStateBlock(SlamModelParser.NextStateBlockContext ctx) {
            if (!agentStates.containsKey(ctx.name.getText())) {
                errors.record(ParseUtil.unknownAgentStateError(agentName, ctx.name));
                validationFlag = false;
            }
            ctx.block.accept(this);
            return validationFlag;
        }


        @Override
        public Boolean visitProbabilitySelectionBlock(SlamModelParser.ProbabilitySelectionBlockContext ctx) {
            ctx.cases.forEach(c -> c.accept(this));
            return validationFlag;
        }

        @Override
        public Boolean visitSelectCase(SlamModelParser.SelectCaseContext ctx) {
            TypeInferenceVisitor visitor = new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors);
            validationFlag &= visitor.checkType(SlamType.REAL_TYPE, ctx.weight);
            if (ctx.guard != null) {
                validationFlag &= visitor.checkType(SlamType.BOOLEAN_TYPE, ctx.guard);
            }
            ctx.nextStateBlock().accept(this);
            return validationFlag;
        }

        @Override
        public Boolean visitAgentCommandBlock(SlamModelParser.AgentCommandBlockContext ctx) {
            ctx.commands.forEach(c -> c.accept(this));
            return validationFlag;
        }


        @Override
        public Boolean visitAgentCommandLet(SlamModelParser.AgentCommandLetContext ctx) {
            if (validationFlag &= isValidName(ctx.name)) {
                SlamType type = ctx.expr().accept(new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND,this::getTypeOf,errors));
                SlamType oldType = localVariables.put(ctx.name.getText(), type);
                ctx.agentCommandBlock().accept(this);
                if (oldType != null) {
                    localVariables.put(ctx.name.getText(), oldType);
                } else {
                    localVariables.remove(ctx.name.getText());
                }
            }
            return validationFlag;
        }

        @Override
        public Boolean visitAgentCommandIfThenElse(SlamModelParser.AgentCommandIfThenElseContext ctx) {
            validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors).checkType(SlamType.BOOLEAN_TYPE, ctx.expr());
            ctx.thenCommand.accept(this);
            if (ctx.elseCommand != null) {
                ctx.elseCommand.accept(this);
            }
            return validationFlag;
        }

        @Override
        public Boolean visitAgentCommandSend(SlamModelParser.AgentCommandSendContext ctx) {
            ctx.messageExpression().accept(this);
            if (ctx.agentPattern() != null) {
                ctx.agentPattern().accept(this);
            }
            validationFlag &= new  TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors).checkType(SlamType.REAL_TYPE, ctx.time);
            return validationFlag;
        }

        @Override
        public Boolean visitMessageExpression(SlamModelParser.MessageExpressionContext ctx) {
            MessageTag tag = messageRepository.getTag(ctx.tag.getText());
            if (tag == null) {
                errors.record(ParseUtil.unknownTagError(ctx.tag));
                validationFlag = false;
            } else {
                if (tag.getArity() != ctx.elements.size()) {
                    errors.record(ParseUtil.illegalNumberOfMessageElements(ctx.tag, tag.getArity(), ctx.elements.size()));
                } else {
                    TypeInferenceVisitor typeVisitor = new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors);
                    for(int i=0; i< tag.getArity(); i++) {
                        validationFlag &= typeVisitor.checkType(tag.getTypeOf(i), ctx.elements.get(i));
                    }
                }
            }
            return validationFlag;
        }


        @Override
        public Boolean visitAgentCommandAssignment(SlamModelParser.AgentCommandAssignmentContext ctx) {
            if (!agentTable.isAttribute(agentName, ctx.name.getText())) {
                errors.record(ParseUtil.illegalAssignmentError(ctx.name));
                return (validationFlag = false);
            }
            SlamType expectedType = agentTable.getTypeOf(agentName, ctx.name.getText());
            return (validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_COMMAND, this::getTypeOf, errors).checkType(expectedType, ctx.expr()));
        }

        @Override
        public Boolean visitStateMessageHandler(SlamModelParser.StateMessageHandlerContext ctx) {
            MessageTag tag = messageRepository.getTag(ctx.tag.getText());
            if (tag == null) {
                errors.record(ParseUtil.unknownTagError(ctx.tag));
                validationFlag = false;
            } else {
                if (tag.getArity() != ctx.content.size()) {
                    errors.record(ParseUtil.illegalNumberOfMessageElements(ctx.tag, tag.getArity(), ctx.content.size()));
                    validationFlag = false;
                } else {
                    String[] templateVariables = getTemplateVariables(ctx.content);
                    SlamType[] oldTypes = recordLocalVariables(templateVariables, tag::getTypeOf);
                    if (ctx.agentGuard != null) {
                        ctx.agentGuard.accept(this);
                    }
                    if (ctx.guard != null) {
                        validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_MESSAGE_HANDLER, this::getTypeOf, errors).checkType(SlamType.BOOLEAN_TYPE, ctx.guard);
                    }
                    ctx.activityBlock().accept(this);
                    removeLocalVariables(templateVariables, oldTypes);
                }
            }
            return validationFlag;

        }

        private void removeLocalVariables(String[] templateVariables, SlamType[] oldTypes) {
            for (int i = 0; i < templateVariables.length; i++) {
                if (templateVariables[i] != null) {
                    if (oldTypes[i] != null) {
                        this.localVariables.put(templateVariables[i], oldTypes[i]);
                    } else {
                        this.localVariables.remove(templateVariables[i]);
                    }
                }
            }
        }

        private SlamType[] recordLocalVariables(String[] templateVariables, IntFunction<SlamType> typeOf) {
            SlamType[] oldTypes = new SlamType[templateVariables.length];
            for (int i = 0; i < templateVariables.length; i++) {
                if (templateVariables[i] != null) {
                    oldTypes[i] = localVariables.put(templateVariables[i], typeOf.apply(i));
                }
            }
            return oldTypes;
        }

        private String[] getTemplateVariables(List<SlamModelParser.ValuePatternContext> content) {
            return content.stream().map(this::getTemplateVariable).toArray(String[]::new);
        }

        private String getTemplateVariable(SlamModelParser.ValuePatternContext valuePatternContext) {
            if (valuePatternContext instanceof SlamModelParser.PatternVariableContext) {
                Token name = ((SlamModelParser.PatternVariableContext) valuePatternContext).name;
                if (isValidName(name)) {
                    return name.getText();
                }
            }
            return null;
        }

        private void setInitialState(Token state) {
            if (initialState != null) {
                errors.record(ParseUtil.duplicatedInitialStateError(state, initialState));
            } else {
                initialState = state;
            }
        }

        private void recordState(SlamModelParser.AgentStateDeclarationContext state) {
            if (agentStates.containsKey(state.name.getText())) {
                errors.record(ParseUtil.duplicatedStateName(state.name, agentStates.get(state.name.getText())));
                validationFlag = false;
            } else {
                agentStates.put(state.name.getText(), state.name);
            }
        }

        private void checkViews() {
            for (SlamModelParser.AttributeDeclarationContext view : agentDeclaration.views) {
                if (isValidName(view.name)&&record(view.name)) {
                    SlamType currentType = agentTable.getPropertyType(view.name.getText());
                    SlamType declaredType = SlamType.getTypeOf(view.type.getText());
                    if (SlamType.NONE_TYPE.equals(currentType)||currentType.equals(declaredType)) {
                        agentTable.recordAgentView(agentName, view.name.getText(), declaredType);
                    } else {
                        validationFlag = false;
                    }
                }
            }
        }

        private void checkAttributes() {
            for (SlamModelParser.AttributeDeclarationContext attribute : agentDeclaration.attributes) {
                if (isValidName(attribute.name)&&record(attribute.name)) {
                    SlamType currentType = agentTable.getPropertyType(attribute.name.getText());
                    SlamType declaredType = SlamType.getTypeOf(attribute.type.getText());
                    if (SlamType.NONE_TYPE.equals(currentType)||currentType.equals(declaredType)) {
                        agentTable.recordAgentAttribute(agentName, attribute.name.getText(), declaredType);
                    } else {
                        validationFlag = false;
                    }
                }
            }

        }

        private SlamType getTypeOf(String name) {
            if (localVariables.containsKey(name)) {
                return localVariables.get(name);
            }
            SlamType type = agentTable.getTypeOf(agentName, name);
            if (SlamType.NONE_TYPE.equals(type)) {
                return registry.typeOf(name);
            }
            return type;
        }

        private void recordParameters() {
            for (SlamModelParser.AgentParameterContext param : agentDeclaration.params) {
                if (isValidName(param.name)&&record(param.name)) {
                    agentTable.recordAgentParameter(agentName, param.name.getText(),SlamType.getTypeOf(param.type.getText()));
                } else {
                    validationFlag = false;
                }
            }
        }

        private boolean record(Token tag) {
            if (agentParametersAndAttributes.containsKey(tag.getText())) {
                SlamModelValidator.this.errors.record(new ParseError(ParseUtil.duplicatedName(tag, agentParametersAndAttributes.get(tag.getText())), tag.getLine(), tag.getCharPositionInLine()));
                return false;
            } else {
                agentParametersAndAttributes.put(tag.getText(), tag);
                return true;
            }
        }

        @Override
        public Boolean visitAgentPatternNegation(SlamModelParser.AgentPatternNegationContext ctx) {
            return ctx.arg.accept(this);
        }

        @Override
        public Boolean visitAgentPatternBrackets(SlamModelParser.AgentPatternBracketsContext ctx) {
            return ctx.agentPattern().accept(this);
        }

        @Override
        public Boolean visitAgentPatternAny(SlamModelParser.AgentPatternAnyContext ctx) {
            return true;
        }

        @Override
        public Boolean visitAgentPatternNamed(SlamModelParser.AgentPatternNamedContext ctx) {
            if (agentTable.isDefined(ctx.name.getText())) {
                return new TypeInferenceVisitor(ExpressionContext.AGENT_VIEW, a -> agentTable.getTypeOf(ctx.name.getText(), a), a -> agentTable.getTypeOf(agentName, a), errors).checkType(SlamType.BOOLEAN_TYPE, ctx.guard);
            } else {
                errors.record(ParseUtil.unknownAgentError(ctx.name));
                return (validationFlag = false);
            }
        }

        @Override
        public Boolean visitAgentPatternProperty(SlamModelParser.AgentPatternPropertyContext ctx) {
            validationFlag &= new TypeInferenceVisitor(ExpressionContext.AGENT_PATTERN, this::getTypeOf, agentTable::getSharedAttributesTypeSolver, errors).checkType(SlamType.BOOLEAN_TYPE, ctx.guard);
            return super.visitAgentPatternProperty(ctx);
        }

        @Override
        public Boolean visitAgentPatternConjunction(SlamModelParser.AgentPatternConjunctionContext ctx) {
            return ctx.left.accept(this)&&ctx.right.accept(this);
        }

        @Override
        public Boolean visitAgentPatternDisjunction(SlamModelParser.AgentPatternDisjunctionContext ctx) {
            return ctx.left.accept(this)&&ctx.right.accept(this);
        }

        public boolean recordAttributes() {
            recordParameters();
            checkAttributes();
            checkViews();
            return validationFlag;
        }
    }

    private class SystemValidator {
        public SystemValidator(SlamModelParser.DeclarationSystemContext ctx) {
        }

        public boolean vaidate() {
            return false;
        }
    }
}
