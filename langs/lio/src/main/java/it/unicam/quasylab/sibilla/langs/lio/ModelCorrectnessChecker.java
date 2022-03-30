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

import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelCorrectnessChecker extends LIOModelBaseVisitor<Boolean> {

    private static final String UNKNOWN_ACTION = "Action %s is unknown.";
    private static final String UNKNOWN_STATE = "State %s is unknown.";
    private static final String DUPLICATED_SYMBOL_MESSAGE = "Duplicated symbol name %s.";


    private final List<ParseError> errors;
    private final SymbolTable table;

    public ModelCorrectnessChecker(List<ParseError> errors, SymbolTable table) {
        this.errors = errors;
        this.table = table;
    }

    public ModelCorrectnessChecker(SymbolTable table) {
        this(new LinkedList<>(),table);
    }

    @Override
    public Boolean visitParam(LIOModelParser.ParamContext ctx) {
        return checkRealExpression(ctx.value);
    }

    private Boolean checkRealExpression(LIOModelParser.ExprContext value) {
        return checkRealExpression(value,false);
    }

    private Boolean checkRealExpression(LIOModelParser.ExprContext value, boolean isPopulation) {
        return ExpressionChecker.checkRealExpression(errors,table,new HashSet<>(), isPopulation, value);
    }

    private Boolean checkIntegerExpression(LIOModelParser.ExprContext value, boolean isPopolation, Set<String> parameters) {
        return ExpressionChecker.checkIntegerExpression(errors,table,parameters, isPopolation, value);
    }

    @Override
    public Boolean visitConstant(LIOModelParser.ConstantContext ctx) {
        return checkRealExpression(ctx.value);
    }

    @Override
    public Boolean visitAction(LIOModelParser.ActionContext ctx) {
        return checkRealExpression(ctx.probability,true);
    }

    @Override
    public Boolean visitStep(LIOModelParser.StepContext ctx) {
        boolean flag = checkAction(ctx.performedAction);
        flag &= checkState(ctx.nextState);
        return flag;
    }

    private boolean checkState(Token nextState) {
        if (!table.isAState(nextState.getText())) {
            errors.add(unknownStateError(nextState));
            return false;
        }
        return true;
    }

    private ParseError unknownStateError(Token state) {
        return new ParseError(String.format(UNKNOWN_STATE,state.getText()),state.getLine(), state.getCharPositionInLine());
    }

    private boolean checkAction(Token performedAction) {
        if (!table.isAnAction(performedAction.getText())) {
            errors.add(unknownActionError(performedAction));
            return false;
        }
        return true;
    }

    private ParseError unknownActionError(Token performedAction) {
        return new ParseError(String.format(UNKNOWN_ACTION,performedAction.getText()),performedAction.getLine(), performedAction.getCharPositionInLine());
    }


    @Override
    public Boolean visitSystem(LIOModelParser.SystemContext ctx) {
        Set<String> parameters = new HashSet<>();
        boolean flag = true;
        for (Token s: ctx.args) {
            String name = s.getText();
            if (table.isDeclared(name)||parameters.contains(name)) {
                errors.add(new ParseError(String.format(DUPLICATED_SYMBOL_MESSAGE,name),s.getLine(),s.getCharPositionInLine()));
                flag = false;
            } else {
                parameters.add(name);
            }
        }
        return flag & ctx.population.stream().filter(a -> !checkAgentExpression(parameters,a)).count()==0;
    }


    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate & nextResult;
    }

    public Boolean checkAgentExpression(Set<String> parameters, LIOModelParser.Agent_exprContext ctx) {
        boolean flag = checkState(ctx.name);
        if (ctx.size != null) {
            flag &= checkIntegerExpression(ctx.size,false,parameters);
        }
        return flag;
    }

    public List<ParseError> getErrors() {
        return errors;
    }
}
