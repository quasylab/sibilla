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

import it.unicam.quasylab.sibilla.core.models.slam.AgentVariable;
import it.unicam.quasylab.sibilla.core.models.slam.MessageRepository;
import it.unicam.quasylab.sibilla.core.models.slam.MessageTag;
import it.unicam.quasylab.sibilla.core.models.slam.SlamType;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class SymbolTable {

    private final Map<String, Token> declarationTokens = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationPredicateContext> predicates = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationConstantContext> constants = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationParameterContext> parameters = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationMessageContext> messages = new HashMap<>();
    private final Map<String, AgentInfo> agents = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationMeasureContext> measures = new HashMap<>();
    private final Map<String, SlamModelParser.DeclarationSystemContext> systems = new HashMap<>();

    private final Map<String, AgentVariable> variables = new HashMap<>();
    private final Set<String>                attributes = new HashSet<>();
    private final Set<String>                views = new HashSet();

    private final MessageRepository repository = new MessageRepository();


    public boolean isDeclaredElementWithName(String name) {
        return declarationTokens.containsKey(name);
    }

    public Token getDeclarationToken(String name) {
        return declarationTokens.get(name);
    }

    public Token getDeclarationToken(String agentName, String parameterName) {
        return declarationTokens.get(ParseUtil.localVariableName(agentName,parameterName));
    }

    public void addPredicate(SlamModelParser.DeclarationPredicateContext ctx) {
        if (declarationTokens.containsKey(ctx.name.getText())) {
            return ;
        }
        declarationTokens.put(ctx.name.getText(), ctx.name);
        predicates.put(ctx.name.getText(), ctx);
    }

    public Collection<SlamModelParser.DeclarationPredicateContext> getPredicates() {
        return predicates.values();
    }

    private void checkForDuplicatedName(Token token) {
        if (declarationTokens.containsKey(token.getText())) {
            throw new IllegalArgumentException(ParseUtil.duplicatedName(token, declarationTokens.get(token.getText())));
        }
    }

    public void addConstant(SlamModelParser.DeclarationConstantContext ctx, SlamType type) {
        checkForDuplicatedName(ctx.name);
        declarationTokens.put(ctx.name.getText(), ctx.name);
        constants.put(ctx.name.getText(), ctx);
        recordVariable(ctx.name.getText(), type);
    }

    public Collection<SlamModelParser.DeclarationConstantContext> getConstants() {
        return constants.values();
    }

    public void addParameter(SlamModelParser.DeclarationParameterContext ctx) {
        checkForDuplicatedName(ctx.name);
        declarationTokens.put(ctx.name.getText(), ctx.name);
        parameters.put(ctx.name.getText(), ctx);
        recordVariable(ctx.name.getText(), SlamType.REAL_TYPE);
    }

    private synchronized  void recordVariable(String name, SlamType type) {
        this.variables.put(name, new AgentVariable(name, variables.size(), type));
    }

    public Collection<SlamModelParser.DeclarationParameterContext> getParameters() {
        return parameters.values();
    }

    public void addMessage(SlamModelParser.DeclarationMessageContext ctx) {
        checkForDuplicatedName(ctx.tag);
        declarationTokens.put(ctx.tag.getText(), ctx.tag);
        messages.put(ctx.tag.getText(), ctx);
        repository.addTag(ctx.tag.getText(), ctx.content.stream().map(RuleContext::getText).map(SlamType::getTypeOf).toArray(SlamType[]::new));
    }

    public Collection<SlamModelParser.DeclarationMessageContext> getMessages() {
        return messages.values();
    }

    public void addAgent(SlamModelParser.DeclarationAgentContext ctx) {
        checkForDuplicatedName(ctx.name);
        declarationTokens.put(ctx.name.getText(), ctx.name);
        agents.put(ctx.name.getText(), new AgentInfo(ctx.name.getText(), ctx));
    }

    public Collection<SlamModelParser.DeclarationAgentContext> getAgents() {
        return agents.values().stream().map(a -> a.declarationAgentContext).collect(Collectors.toList());
    }

    public void addMeasure(SlamModelParser.DeclarationMeasureContext ctx) {
        checkForDuplicatedName(ctx.name);
        declarationTokens.put(ctx.name.getText(), ctx.name);
        measures.put(ctx.name.getText(), ctx);
    }

    public Collection<SlamModelParser.DeclarationMeasureContext> getMeasures() {
        return measures.values();
    }

    public void addSystem(SlamModelParser.DeclarationSystemContext ctx) {
        checkForDuplicatedName(ctx.name);
        declarationTokens.put(ctx.name.getText(), ctx.name);
        systems.put(ctx.name.getText(), ctx);
    }

    public Collection<SlamModelParser.DeclarationSystemContext> getSystems() {
        return systems.values();
    }

    private synchronized AgentVariable getOrRecordVariable(String name, SlamType type) {
        AgentVariable var = this.variables.get(name);
        if (var == null) {
            var = new AgentVariable(name, this.variables.size(), type);
            this.variables.put(name, var);
        } else {
            if (!var.getType().equals(type)) {
                return null;
            }
        }
        return var;
    }

    public synchronized void recordAgentParameter(String agentName, Token start, String parameterName, SlamType type) {
        AgentInfo info = agents.get(agentName);
        if (agentName != null) {
            String variableName = ParseUtil.localVariableName(agentName, parameterName);
            info.addParameter(parameterName, getOrRecordVariable(variableName, type));
            declarationTokens.put(variableName, start);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
    }

    public synchronized void recordAgentAttribute(String agentName, String attributeName, SlamType type) {
        AgentInfo info = agents.get(agentName);
        if (agentName != null) {
            info.addAttribute(attributeName, getOrRecordVariable(attributeName, type));
            attributes.add(attributeName);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
    }

    public synchronized void recordAgentView(String agentName, String viewName, SlamType type) {
        AgentInfo info = agents.get(agentName);
        if (agentName != null) {
            info.addView(viewName, getOrRecordVariable(viewName, type));
            views.add(viewName);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
    }

    public boolean isAgentParameter(String agentName, String parameterName) {
        AgentInfo info = agents.get(agentName);
        if (info != null) {
            return info.isAParameter(parameterName);
        }
        return false;
    }

    public boolean isAgentView(String agentName, String viewName) {
        AgentInfo info = agents.get(agentName);
        if (info != null) {
            return info.isAView(viewName);
        }
        return false;
    }

    public boolean isAgentView(String name) {
        return this.views.contains(name);
    }

    public boolean isAgentAttribute(String agentName, String viewName) {
        AgentInfo info = agents.get(agentName);
        if (info != null) {
            return info.isAnAttribute(viewName);
        }
        return false;
    }

    public boolean isAgentAttribute(String name) {
        return this.attributes.contains(name);
    }


    public SlamType[] getAgentParameters(String agentName) {
        AgentInfo info = agents.get(agentName);
        if (info == null) {
            return null;
        }
        return info.declarationAgentContext.params.stream().map(p -> info.parameters.get(p.name.getText())).toArray(SlamType[]::new);
    }

    public boolean isAParameter(String name) {
        return parameters.containsKey(name);
    }

    public boolean isAnAgent(String agentName) {
        return agents.containsKey(agentName);
    }

    public synchronized void addAgentState(String agentName, SlamModelParser.AgentStateDeclarationContext state) {
        AgentInfo info = agents.get(agentName);
        if (info != null) {
            info.addState(state);
        }
    }

    public synchronized boolean hasState(String agentName, String stateName) {
        AgentInfo info = agents.get(agentName);
        if (info != null) {
            return info.hasState(stateName);
        }
        return false;
    }

    public Set<String> getAgentNames() {
        return new HashSet<>(agents.keySet());
    }

    public boolean isAMessageTag(String tagName) {
        return repository.exists(tagName);
    }

    public MessageTag getMessageTag(String tagName) {
        return repository.getTag(tagName);
    }

    private boolean isAConstant(String name) {
        return constants.containsKey(name);
    }

    public SlamType solveTypeFromParameters(String name) {
        if (isAParameter(name)) {
            return getTypeOf(this.variables.get(name));
        } else {
            return SlamType.NONE_TYPE;
        }
    }

    public SlamType solveTypeFromConstants(String name) {
        if (isAConstant(name)) {
            return getTypeOf(this.variables.get(name));
        } else {
            return solveTypeFromParameters(name);
        }
    }

    public SlamType solveTypeFromMeasuresAndPredicates(String name) {
        return getTypeOf(this.variables.get(name));
    }

    public SlamType solveTypeFromViews(String name) {
        if (isAgentView(name)) {
            return SlamType.NONE_TYPE;
        } else {
            return this.variables.get(name).getType();
        }
    }

    public SlamType getExternalAgentTypeSolver(String agentName, String name) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            return solveTypeFromConstants(name);
        }
        return info.resolveTypeFromMeasuresAndPredicates(name);
    }


    public static Function<String, SlamType> combine(Function<String, SlamType> outerFunciton, Function<String, SlamType> innerfunction) {
        return name -> {
            SlamType type = innerfunction.apply(name);
            if (SlamType.NONE_TYPE.equals(type)) {
                return outerFunciton.apply(name);
            } else {
                return type;
            }
        };
    }

    private SlamType getTypeOf(AgentVariable variable) {
        if (variable == null) {
            return SlamType.NONE_TYPE;
        } else {
            return variable.getType();
        }
    }

    public Function<String, SlamType> attributeDeclarationTypeSolver(String agentName) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
        return info::attributeDeclarationTypeSolver;
    }

    public boolean isAParameterOf(String agentName, String attributeName) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
        return info.isAParameter(attributeName);
    }

    public Function<String, SlamType> viewDeclarationTypeSolver(String agentName) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
        return info::viewDeclarationTypeSolver;
    }

    public Function<String, SlamType> getAgentStateTypeSolver(String agentName) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
        return info::viewDeclarationTypeSolver;
    }

    public boolean isAssignable(ExpressionContext context, String agentName, String name) {
        switch (context) {
            case AGENT_ATTRIBUTE:
            case AGENT_COMMAND:
            case AGENT_TIME_UPDATE:
                return isAgentAttribute(agentName, name);
            case AGENT_VIEW:
                return isAgentView(name);
            default:
                return false;
        }
    }

    public SlamType getTypeOf(String agentName, String attribute) {
        AgentInfo info = this.agents.get(agentName);
        if (info == null) {
            throw new IllegalArgumentException("Agent "+agentName+" is unknown!");
        }
        return info.getTypeOf(attribute);
    }

    public class AgentInfo {

        private final String name;

        private final SlamModelParser.DeclarationAgentContext declarationAgentContext;

        private final Map<String, AgentVariable> parameters = new HashMap<>();

        private final Map<String, AgentVariable> views = new HashMap<>();

        private final Map<String, AgentVariable> attributes = new HashMap<>();

        private final Map<String, SlamModelParser.AgentStateDeclarationContext> states = new HashMap<>();

        public AgentInfo(String name, SlamModelParser.DeclarationAgentContext declarationAgentContext) {
            this.name = name;
            this.declarationAgentContext = declarationAgentContext;
        }

        public void addParameter(String name, AgentVariable variable) {
            this.parameters.put(name, variable);
        }

        public void addView(String name, AgentVariable variable) {
            this.views.put(name, variable);
        }

        public void addAttribute(String name, AgentVariable variable) {
            this.attributes.put(name, variable);
        }

        public void addState(SlamModelParser.AgentStateDeclarationContext state) {
            this.states.put(state.name.getText(), state);
        }

        public boolean isDeclared(String name) {
            return parameters.containsKey(name)||attributes.containsKey(name)||views.containsKey(name);
        }

        public boolean isAnAttribute(String name) {
            return this.attributes.containsKey(name);
        }

        public boolean isAView(String name) {
            return this.views.containsKey(name);
        }

        public boolean isAParameter(String name) {
            return this.parameters.containsKey(name);
        }


        public boolean hasState(String stateName) {
            return this.states.containsKey(stateName);
        }

        public AgentVariable resolveVariableFromMeasuresAndPredicates(String name) {
            if (this.isAView(name)) {
                return this.views.get(name);
            }
            if (this.isAnAttribute(name)) {
                return this.attributes.get(name);
            }
            return null;
        }

        public SlamType resolveTypeFromMeasuresAndPredicates(String name) {
            AgentVariable variable = resolveVariableFromMeasuresAndPredicates(name);
            if (variable == null) {
                return SlamType.NONE_TYPE;
            } else {
                return variable.getType();
            }
        }

        public AgentVariable resolveVariableFromViews(String name) {
            return this.attributes.get(name);
        }

        public SlamType resolveTypeFromViews(String name) {
            AgentVariable variable = resolveVariableFromViews(name);
            if (variable == null) {
                return SlamType.NONE_TYPE;
            } else {
                return variable.getType();
            }
        }

        public AgentVariable resolveFromStateCode(String name) {
            return resolveVariableFromMeasuresAndPredicates(name);
        }

        public SlamType resolveTypeFromStateCode(String name) {
            AgentVariable variable = resolveFromStateCode(name);
            if (variable == null) {
                return SlamType.NONE_TYPE;
            } else {
                return variable.getType();
            }
        }


        public SlamType attributeDeclarationTypeSolver(String name) {
            AgentVariable var = this.parameters.get(name);
            if (var != null) return var.getType();
            var = this.attributes.get(name);
            if (var != null) return var.getType();
            return solveTypeFromConstants(name);
        }

        //TODO: CHECK THIS!!!
        public SlamType viewDeclarationTypeSolver(String s) {
            AgentVariable var = this.parameters.get(name);
            if (var != null) return var.getType();
            var = this.attributes.get(name);
            if (var != null) return var.getType();
            var = this.views.get(name);
            if (var != null) return var.getType();
            return solveTypeFromConstants(name);
        }

        public SlamType getTypeOf(String attribute) {
            AgentVariable var = this.parameters.get(attribute);
            if (var != null) return var.getType();
            var = this.attributes.get(attribute);
            if (var != null) return var.getType();
            var = this.views.get(attribute);
            if (var != null) return var.getType();
            return solveTypeFromConstants(attribute);
        }
    }
}
