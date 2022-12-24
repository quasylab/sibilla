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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SymbolTable {

    private final ErrorCollector errorCollector = new ErrorCollector();

    private final Map<String, Token> declarationTokens = new HashMap<>();
    private final Map<String, YodaModelParser.ConstantDeclarationContext> constants = new HashMap<>();
    private final Map<String, YodaModelParser.ParameterDeclarationContext> parameters = new HashMap<>();
    private final Map<String, YodaModelParser.TypeDeclarationContext> types = new HashMap<>();
    private final Map<String, AgentDeclaration> agents = new HashMap<>();
    private final Map<String, SystemDeclaration> systems = new HashMap<>();
    private final Map<String, YodaModelParser.ConfigurationDeclarationContext> configurations = new HashMap<>();
    private final Map<String, YodaType> yodaTypes = new HashMap<>();

    private final Map<String, YodaVariable> fields = new HashMap<>();
    private final Map<String, YodaVariable> variables = new HashMap<>();


    /**
     * This method checks if the input token has been already added to the
     * declaration tokens map, and add it if it has not.
     *
     * @param token the input token to be checked
     * @return true if the token has been added, false if already exists
     */
    private boolean checkAndAdd(Token token) {
        String tokenName = token.getText();
        if (declarationTokens.containsKey(tokenName)) {
            errorCollector.record(ParseUtil.duplicatedIdentifierError(tokenName, declarationTokens.get(tokenName)));
            return false;
        } else {
            this.declarationTokens.put(tokenName, token);
            return true;
        }
    }

    /**
     * This method checks if the input name exists in the declaration map
     *
     * @param name the name to be searched
     * @return true if the name is in the declaration map
     */
    public boolean existsDeclaration(String name) {
        return this.declarationTokens.containsKey(name);
    }

    /**
     * This method returns a token in the declaration map corresponding
     * to the searched name
     *
     * @param name the name to be searched
     * @return a token in the declaration map
     */
    public Token getDeclarationToken(String name) {
        return this.declarationTokens.get(name);
    }

    public synchronized void recordVariable (String name, YodaType type) {
        if (!this.variables.containsKey(name)) {
            this.variables.put(name, new YodaVariable(variables.size(), name, type));
        }
    }

    /**
     * This method adds a constant variable to the constant map
     *
     * @param ctx the context that should be added
     */
    public void addConstants(YodaModelParser.ConstantDeclarationContext ctx, YodaType type) {
        if (checkAndAdd(ctx.name)) {
            this.constants.put(ctx.name.getText(), ctx);
            recordVariable(ctx.name.getText(), type);
        }
    }

    /**
     * This method checks if the input name exists in the constant map
     *
     * @param name the name to be searched
     * @return true if the name is in the constant map
     */
    public boolean existsConstant(String name) {
        return this.constants.containsKey(name);
    }

    /**
     * This method adds a parameter variable to the parameter map
     *
     * @param ctx the context that should be added
     */
    public void addParameter(YodaModelParser.ParameterDeclarationContext ctx, YodaType type) {
        if (checkAndAdd(ctx.name)) {
            this.parameters.put(ctx.name.getText(), ctx);
            recordVariable(ctx.name.getText(), type);
        }
    }

    /**
     * This method checks if the input name exists in the parameter map
     *
     * @param name the name to be searched
     * @return true if the name is in the parameter map
     */
    public boolean existsParameter(String name) {
        return this.parameters.containsKey(name);
    }

    /**
     * This method adds a type variable to the type map
     *
     * @param ctx the context that should be added
     */
    //TODO add custom type
    public void addType(YodaModelParser.TypeDeclarationContext ctx) {
        if (checkAndAdd(ctx.typeName)) {
            this.types.put(ctx.typeName.getText(), ctx);
        }
    }

    /**
     * This method checks if the input name exists in the parameter map
     *
     * @param name the name to be searched
     * @return true if the name is in the type map
     */
    public boolean existsType(String name) {
        return this.types.containsKey(name);
    }

    /**
     * This method adds an agent to the agent map
     *
     * @param ctx the context that should be added
     */
    public void addAgent(YodaModelParser.AgentDeclarationContext ctx) {
        if (checkAndAdd(ctx.agentName)) {
            String name = ctx.agentName.getText();
            this.agents.put(name, new AgentDeclaration(name, ctx));
        }
    }

    /**
     * This method checks if the input name exists in the agent map
     *
     * @param name the name to be searched
     * @return true if the name is in the agent map
     */
    public boolean existsAgent(String name) {
        return this.agents.containsKey(name);
    }

    /**
     * This method returns a collection of all available agent declaration contexts
     *
     * @return a collection of all available agent declaration contexts
     */
    public Collection<YodaModelParser.AgentDeclarationContext> getAgents() {
        return agents.values().stream().map(a -> a.getAgDecCtx()).collect(Collectors.toList());
    }

    /**
     * This method returns a field belonging or to an agent or to a system
     * and records the field if it has not been already added
     *
     * @param name the name of the field
     * @param type the type of the field
     * @return a field belonging or to an agent or to a system
     */
    public synchronized YodaVariable getOrRecordField(String name, YodaType type) {
        YodaVariable field = this.fields.get(name);
        if (field == null) {
            field = new YodaVariable(this.fields.size(), name, type);
            this.fields.put(name, field);
        } else {
            if (!field.getType().equals(type)) {
                //TODO add new field with same name, different type
                return null;
            }
        }
        return field;
    }

    /**
     * This method records a knowledge field to an existing agent
     *
     * @param agentName the agent to search
     * @param token the field token
     * @param fieldName the field name
     * @param type the field type
     */
    public synchronized void recordAgentKnowledge(String agentName, Token token, String fieldName, YodaType type) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            declaration.addKnowledge(fieldName, getOrRecordField(localVarName, type));
            declarationTokens.put(localVarName, token);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is not available!");
        }
    }

    /**
     * This method records an information field to an existing agent
     *
     * @param agentName the agent to search
     * @param token the field token
     * @param fieldName the field name
     * @param type the field type
     */
    public synchronized void recordAgentInformation(String agentName, Token token, String fieldName, YodaType type) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            declaration.addInformation(fieldName, getOrRecordField(localVarName, type));
            declarationTokens.put(localVarName, token);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is not available!");
        }
    }

    /**
     * This method records an observation field to an existing agent
     *
     * @param agentName the agent to search
     * @param token the field token
     * @param fieldName the field name
     * @param type the field type
     */
    public synchronized void recordAgentObservation(String agentName, Token token, String fieldName, YodaType type) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            declaration.addObservation(fieldName, getOrRecordField(localVarName, type));
            declarationTokens.put(localVarName, token);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is not available!");
        }
    }

    /**
     * This method records an action to an existing agent
     *
     * @param agentName the agent to search
     * @param token the action token
     * @param ctx the action context
     */
    public synchronized void recordAgentAction(String agentName, Token token, YodaModelParser.ActionBodyContext ctx) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            String localActionName = ParseUtil.localVariableName(agentName, ctx.actionName.getText());
            declaration.addAction(ctx);
            declarationTokens.put(localActionName, token);
        } else {
            throw new IllegalArgumentException("Agent "+agentName+" is not available!");
        }
    }

    /**
     * This method checks if the input field name exists in a certain agent knowledge
     *
     * @param agentName the agent to search
     * @param fieldName the field to search in the agent knowledge
     * @return true if the input field name exists in a certain agent knowledge
     */
    public boolean existsAgentKnowledge(String agentName, String fieldName){
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null){
            return declaration.existsKnowledge(fieldName);
        }
        return false;
    }

    /**
     * This method checks if the input field name exists in a certain agent information
     *
     * @param agentName the agent to search
     * @param fieldName the field to search in the agent information
     * @return true if the input field name exists in a certain agent information
     */
    public boolean existsAgentInformation(String agentName, String fieldName) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            return declaration.existsInformation(fieldName);
        }
        return false;
    }

    /**
     * This method checks if the input field name exists in a certain agent observation
     *
     * @param agentName the agent to search
     * @param fieldName the field to search in the agent observation
     * @return true if the input field name exists in a certain agent observation
     */
    public boolean existsAgentObservation(String agentName, String fieldName) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            return declaration.existsObservation(fieldName);
        }
        return false;
    }

    /**
     * This method checks if the input action name exists in a certain agent
     *
     * @param agentName the agent to search
     * @param actionName the action to search in the agent
     * @return true if the input action name exists in a certain agent
     */
    public boolean existsAgentAction(String agentName, String actionName) {
        AgentDeclaration declaration = agents.get(agentName);
        if (declaration != null) {
            declaration.existsAction(actionName);
        }
        return false;
    }

    /**
     * This method adds a system to the system map
     *
     * @param ctx the context that should be added
     */
    public void addSystem(YodaModelParser.SystemDeclarationContext ctx) {
        if (checkAndAdd(ctx.name)) {
            String name = ctx.name.getText();
            this.systems.put(name, new SystemDeclaration(name, ctx));
        }
    }

    /**
     * This method checks if the input name exists in the system map
     *
     * @param name the name to be searched
     * @return true if the name is in the system map
     */
    public boolean existsSystem(String name) {
        return this.systems.containsKey(name);
    }

    /**
     * This method returns a collection of all available system declaration contexts
     *
     * @return a collection of all available system declaration contexts
     */
    public Collection<YodaModelParser.SystemDeclarationContext> getSystems() {
        return systems.values().stream().map(s -> s.getSysDecCtx()).collect(Collectors.toList());
    }

    /**
     * This method records a scene field to an existing system
     *
     * @param systemName the system to search
     * @param token the field token
     * @param fieldName the field name
     * @param type the field type
     */
    public synchronized void registerSystemSceneField(String systemName, Token token, String fieldName, YodaType type) {
        SystemDeclaration systemDeclaration = systems.get(systemName);
        if (systemDeclaration != null) {
            String localVarName = ParseUtil.localVariableName(systemName, fieldName);
            systemDeclaration.addSceneField(fieldName, getOrRecordField(localVarName, type));
            declarationTokens.put(localVarName, token);
        } else {
            throw new IllegalArgumentException("System "+systemName+" is not available!");
        }
    }

    /**
     * This method records a temporary variable to an existing system
     *
     * @param systemName the system to search
     * @param token the temporary variable token
     * @param ctx the temporary variable context
     */
    public synchronized void registerSystemTemp(String systemName, Token token, YodaModelParser.AssignmentTempContext ctx) {
        SystemDeclaration systemDeclaration = systems.get(systemName);
        if (systemDeclaration != null) {
            String localVarName = ParseUtil.localVariableName(systemName, ctx.tempName.getText());
            systemDeclaration.addTemp(ctx);
            declarationTokens.put(localVarName, token);
        } else {
            throw new IllegalArgumentException("System "+systemName+" is not available!");
        }
    }

    /**
     * This method checks if the input field name exists in a certain system scene
     *
     * @param systemName the system to search
     * @param fieldName the field to search in the system scene
     * @return true if the input field name exists in a certain system scene
     */
    public boolean existsSystemSceneField(String systemName, String fieldName){
        SystemDeclaration systemDeclaration = systems.get(systemName);
        if (systemDeclaration != null){
            return systemDeclaration.existsSceneField(fieldName);
        }
        return false;
    }

    /**
     * This method checks if the input temporary name exists in a certain system
     *
     * @param systemName the system to search
     * @param tempName the temporary name to search in the system
     * @return true if the input temporary name exists in a certain system
     */
    public boolean existsSystemTemp(String systemName, String tempName) {
        SystemDeclaration systemDeclaration = systems.get(systemName);
        if (systemDeclaration != null) {
            return systemDeclaration.existsTemp(tempName);
        }
        return false;
    }


    /**
     * This method adds a configuration to the configuration map
     *
     * @param ctx the context that should be added
     */
    public void addConfiguration(YodaModelParser.ConfigurationDeclarationContext ctx) {
        if (checkAndAdd(ctx.name)) {
            this.configurations.put(ctx.name.getText(), ctx);
        }
    }

    /**
     * This method checks if the input name exists in the configuration map
     *
     * @param name the name to be searched
     * @return true if the name is in the configuration map
     */
    public boolean existsConfiguration(String name) {
        return this.configurations.containsKey(name);
    }


    public YodaType solveTypeFromConstant(String name) {
        if (existsConstant(name)) {
            return getTypeOf(this.variables.get(name));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    public YodaType solveTypeFromParameter(String name) {
        if (existsParameter(name)) {
            return getTypeOf(this.variables.get(name));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    public YodaType solveTypeFromObservation(String agentName, String fieldName) {
        if (existsAgentObservation(agentName, fieldName)) {
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            return getTypeOf(this.fields.get(localVarName));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    public YodaType solveTypeFromKnowledge(String agentName, String fieldName) {
        if (existsAgentKnowledge(agentName, fieldName)) {
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            return getTypeOf(this.fields.get(localVarName));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    public YodaType solveTypeFromInformation(String agentName, String fieldName) {
        if (existsAgentInformation(agentName, fieldName)){
            String localVarName = ParseUtil.localVariableName(agentName, fieldName);
            return getTypeOf(this.fields.get(localVarName));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    public YodaType solveTypeFromScene(String systemName, String fieldName) {
        if (existsSystemSceneField(systemName, fieldName)) {
            String localVarName = ParseUtil.localVariableName(systemName, fieldName);
            return getTypeOf(this.fields.get(localVarName));
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    private YodaType getTypeOf(YodaVariable yodaVariable) {
        if (yodaVariable == null) {
            return YodaType.NONE_TYPE;
        } else {
            return yodaVariable.getType();
        }
    }

    //TODO
    public Function<String, YodaType> getAgentTypeSolver(String name) {
        AgentDeclaration info = this.agents.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Agent " + name + " does not exist!");
        }
        //return info::;
        return null;
    }
}
