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

package it.unicam.quasylab.sibilla.langs.lio;

import it.unicam.quasylab.sibilla.core.markov.ContinuousTimeMarkovChain;
import it.unicam.quasylab.sibilla.core.past.ds.TupleSpace;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to collect the symbols occurring in a LIOModel.
 */
public class SymbolTable {

    private final Map<String, Token> declarationTokens = new HashMap<>();
    private final Map<String, LIOModelParser.ElementParamContext> parameters = new HashMap<>();
    private final Map<String, LIOModelParser.ElementConstantContext> constants = new HashMap<>();
    private final Map<String, LIOModelParser.ElementActionContext> actions = new HashMap<>();
    private final Map<String, LIOModelParser.ElementStateContext> states = new HashMap<>();
    private final Map<String, LIOModelParser.ElementMeasureContext> measures = new HashMap<>();
    private final Map<String, LIOModelParser.ElementAtomicContext> atomic = new HashMap<>();
    private final Map<String, LIOModelParser.ElementSystemContext> systems = new HashMap<>();
    private final Map<String, LIOModelParser.ElementPredicateContext> predicates = new HashMap<>();
    private final Map<String, LIOType> types = new HashMap<>();

    private void checkAndAdd(Token declarationToken) {
        String name = declarationToken.getText();
        if (declarationTokens.containsKey(name)) {
            throw new IllegalArgumentException(ParseUtil.duplicatedNameErrorMessage(declarationToken, getDeclarationToken(name)));
        } else {
            this.declarationTokens.put(name, declarationToken);
        }
    }

    /**
     * Adds a parameter to the symbol table.
     *
     * @param ctx parameter to add.
     */
    public void addParameter(LIOModelParser.ElementParamContext ctx) {
        checkAndAdd(ctx.name);
        this.parameters.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists a parameter with the given name in this table.
     *
     * @param name parameter name.
     * @return true if there exists a parameter with the given name in this table.
     */
    public boolean isParameter(String name) {
        return this.parameters.containsKey(name);
    }

    /**
     * Adds a constant to the symbol table.
     *
     * @param ctx constant to add.
     */
    public void addConstant(LIOModelParser.ElementConstantContext ctx) {
        checkAndAdd(ctx.name);
        this.constants.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists a constant with the given name in this table.
     *
     * @param name constant name.
     * @return true if there exists a constant with the given name in this table.
     */
    public boolean isConstant(String name) {
        return this.constants.containsKey(name);
    }


    /**
     * Adds an action to the symbol table.
     *
     * @param ctx the action to add to the symbol table.
     */
    public void addAction(LIOModelParser.ElementActionContext ctx) {
        checkAndAdd(ctx.name);
        this.actions.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists an action with the given name in this table.
     *
     * @param name action name.
     * @return true if there exists an action with the given name in this table.
     */
    public boolean isAction(String name) {
        return this.actions.containsKey(name);
    }


    /**
     * Adds a measure to the symbol table.
     *
     * @param ctx the measure to add to the symbol table.
     */
    public void addMeasure(LIOModelParser.ElementMeasureContext ctx) {
        checkAndAdd(ctx.name);
        this.measures.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists an action with the given name in this table.
     *
     * @param name measure name.
     * @return true if there exists a measure with the given name in this table.
     */
    public boolean isMeasure(String name) {
        return this.measures.containsKey(name);
    }


    /**
     * Adds a state to the symbol table.
     *
     * @param ctx the state to add to the symbol table.
     */
    public void addState(LIOModelParser.ElementStateContext ctx) {
        checkAndAdd(ctx.name);
        this.states.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists a state with the given name in this table.
     *
     * @param name state name.
     * @return true if there exists a state with the given name in this table.
     */
    public boolean isState(String name) {
        return this.states.containsKey(name);
    }

    /**
     * Adds an atomic proposition to the symbol table.
     *
     * @param ctx the atomic proposition to add to the symbol table.
     */
    public void addAtomic(LIOModelParser.ElementAtomicContext ctx) {
        checkAndAdd(ctx.name);
        this.atomic.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists an atomic proposition with the given name in this table.
     *
     * @param name atomic proposition name.
     * @return true if there exists an atomic proposition with the given name in this table.
     */
    public boolean isAtomic(String name) {
        return this.atomic.containsKey(name);
    }

    /**
     * Adds a system to the symbol table.
     *
     * @param ctx the system to add to the symbol table.
     */
    public void addSystem(LIOModelParser.ElementSystemContext ctx) {
        checkAndAdd(ctx.name);
        this.systems.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists a system with the given name in this table.
     *
     * @param name atomic proposition name.
     * @return true if there exists a system with the given name in this table.
     */
    public boolean isSystem(String name) {
        return this.systems.containsKey(name);
    }



    /**
     * Adds a predicate to the symbol table.
     *
     * @param ctx the predicate to add to the symbol table.
     */
    public void addPredicate(LIOModelParser.ElementPredicateContext ctx) {
        checkAndAdd(ctx.name);
        this.predicates.put(ctx.name.getText(), ctx);
    }

    /**
     * Checks if there exists a predicate with the given name in this table.
     *
     * @param name predicate name.
     * @return true if there exists a predicate with the given name in this table.
     */
    public boolean isPredicate(String name) {
        return this.predicates.containsKey(name);
    }


    /**
     * Checks if an element with the given name is defined in this table.
     *
     * @param name name to check.
     * @return true if an element with the given name is defined in this table.
     */
    public boolean isDefined(String name) {
        return declarationTokens.containsKey(name);
    }

    /**
     * Returns the declaration token associated with the given name, if it exists.
     * @param name name of element to select.
     * @return the declaration token associated with the given name, if it exists.
     */
    public Token getDeclarationToken(String name) {
        return declarationTokens.get(name);
    }

    /**
     * Returns the type associated with the symbol with the given name.
     *
     * @param name symbol name.
     * @return the type associated with the symbol with the given name.
     */
    public LIOType getTypeOf(String name) {
        return types.getOrDefault(name, LIOType.LIO_NONE);
    }

    /**
     * Records the type associated with the given symbol.
     *
     * @param name symbol name.
     * @param type symbol type.
     */
    public void recordType(String name, LIOType type) {
        types.put(name, type);
    }
}
