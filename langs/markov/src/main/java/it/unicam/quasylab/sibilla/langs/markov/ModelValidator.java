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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ModelValidator {

    private final ErrorCollector errorList;
    private final Map<String, Token> table;
    private final Map<String, DataType> types;

    public ModelValidator(ErrorCollector errorList) {
        this.errorList = errorList;
        this.table = new HashMap<>();
        this.types = new HashMap<>();
    }

    public boolean validate(ParseTree parseTree) {
        if (parseTree == null) {
            return false;
        }
        return parseTree.accept(new ValidatorVisitor());
    }

    public Function<String, DataType> getTypes() {
        return this::getTypeOf;
    }

    public DataType getTypeOf(String name) {
        return getTypeOf(name, Set.of());
    }

    private DataType getTypeOf(String name, Set<String> localVariables) {
        if ((localVariables != null)&&(localVariables.contains(name))) {
            return DataType.REAL;
        } else {
            return this.types.getOrDefault(name, DataType.NONE);
        }
    }

    public class ValidatorVisitor extends MarkovChainModelBaseVisitor<Boolean> {

        private final Set<String> stateVariables = new HashSet<>();
        private boolean stateVariablesAllowed = false;
        private final Map<String, MarkovChainModelParser.Init_declarationsContext> initialStates = new HashMap<>();
        private Set<String> localVariables = new HashSet<>();
        private Token defaultInitState = null;


        @Override
        public Boolean visitModel(MarkovChainModelParser.ModelContext ctx) {
            boolean flag = true;
            for (MarkovChainModelParser.ElementContext e: ctx.element()) {
                flag &= e.accept(this);
            }
            flag &= ctx.state_declaration().accept(this);
            flag &= ctx.rules_declaration().accept(this);
            flag &= ctx.configuration_declaration().accept(this);
            for (MarkovChainModelParser.Measure_declarationContext m: ctx.measure_declaration()) {
                flag &= m.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitSingle_declaration(MarkovChainModelParser.Single_declarationContext ctx) {
            this.localVariables = new HashSet<>();
            this.stateVariablesAllowed = false;
            return ((ctx.variables() == null)||(ctx.variables().accept(this))) & ctx.assignments().accept(this);
        }

        @Override
        public Boolean visitAssignments(MarkovChainModelParser.AssignmentsContext ctx) {
            Set<String> usedStateVariables = new HashSet<>(this.stateVariables);
            boolean flag = true;
            for (MarkovChainModelParser.Variable_assignmentContext va: ctx.variable_assignment()) {
                String name = va.name.getText();
                DataType variableType = getTypeOf(name);
                if (!this.stateVariables.contains(name)) {
                    errorList.record(ParseUtil.unknownStateVariable(name, va.name));
                    flag = false;
                    continue;
                }
                if (variableType == DataType.NONE) {
                    errorList.record(ParseUtil.unknownSymbol(name, va));
                    flag = false;
                    continue;
                }
                if (!usedStateVariables.contains(name)) {
                    errorList.record(ParseUtil.duplicatedStateVariableAssignment(name, va));
                    flag = false;
                    return false;
                }
                if (!va.value.accept(this)) {
                    flag = false;
                    continue;
                }
                flag &= checkType(localVariables, va.value, variableType);
            }
            return flag;
        }

        @Override
        public Boolean visitVariables(MarkovChainModelParser.VariablesContext ctx) {
            boolean result = true;
            for (Token var: ctx.vars) {
                String name = var.getText();
                if (table.containsKey(name)) {
                    errorList.record(ParseUtil.duplicatedSymbolError(name, var, table.get(name)));
                    result = false ;
                }
                localVariables.add(name);
            }
            return result;
        }

        @Override
        public Boolean visitInit_declarations(MarkovChainModelParser.Init_declarationsContext ctx) {
            this.localVariables = new HashSet<>();
            this.stateVariablesAllowed = false;
            boolean result = true;
            if (ctx.defaultToken != null) {
                if (defaultInitState == null) {
                    defaultInitState = ctx.defaultToken;
                } else {
                    errorList.record(ParseUtil.duplicatedDefaultInitialState(ctx.defaultToken, defaultInitState));
                    result = false;
                }
            }
            String name = ctx.name.getText();
            if (initialStates.containsKey(name)) {
                errorList.record(ParseUtil.duplicatedInitialState(name, ctx, initialStates.get(name)));
                result = false;
            } else {
                initialStates.put(name, ctx);
            }
            return result & ((ctx.variables()==null)||ctx.variables().accept(this)) & ctx.assignments().accept(this);
        }

        @Override
        public Boolean visitState_declaration(MarkovChainModelParser.State_declarationContext ctx) {
            stateVariablesAllowed = false;
            boolean result = true;
            for (MarkovChainModelParser.Variable_declarationContext vd: ctx.variable_declaration()) {
                result &= vd.accept(this);
            }
            return result;
        }

        @Override
        public Boolean visitVariable_declaration(MarkovChainModelParser.Variable_declarationContext ctx) {
            boolean flag = checkType(ctx.min, DataType.INTEGER)&checkType(ctx.max, DataType.INTEGER);
            if (checkAndRecord(ctx.name)) {
                types.put(ctx.name.getText(), DataType.INTEGER);
                stateVariables.add(ctx.name.getText());
                return flag;
            } else {
                return false;
            }
        }


        private boolean checkAndRecord(Token token) {
            String name = token.getText();
            if (table.containsKey(name)) {
                errorList.record(ParseUtil.duplicatedSymbolError(name, token, table.get(name)));
                return false;
            } else {
                table.put(name, token);
                return true;
            }

        }

        @Override
        public Boolean visitConst_declaration(MarkovChainModelParser.Const_declarationContext ctx) {
            this.stateVariablesAllowed = false;
            if (checkAndRecord(ctx.name)&&(ctx.expr().accept(this))) {
                types.put(ctx.name.getText(), TypeVisitor.getTypeOf(errorList, ModelValidator.this::getTypeOf, ctx.expr()));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitRules_declaration(MarkovChainModelParser.Rules_declarationContext ctx) {
            this.stateVariablesAllowed = true;
            boolean flag = true;
            for (MarkovChainModelParser.Rule_caseContext ruleCase : ctx.rule_case()) {
                flag &= ruleCase.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitRule_case(MarkovChainModelParser.Rule_caseContext ctx) {
            boolean flag = checkType(ctx.guard, DataType.BOOLEAN);
            for (MarkovChainModelParser.StepContext stepContext : ctx.step()) {
                flag &= stepContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitStep(MarkovChainModelParser.StepContext ctx) {
            return ctx.updates().accept(this) & ((ctx.weight == null)||checkType(ctx.weight, DataType.REAL));
        }

        @Override
        public Boolean visitEmptyUpdate(MarkovChainModelParser.EmptyUpdateContext ctx) {
            return true;
        }

        @Override
        public Boolean visitVariable_update(MarkovChainModelParser.Variable_updateContext ctx) {
            String name = ParseUtil.getVariableNameFromNextId(ctx.target.getText());
            if (!stateVariables.contains(name)) {
                errorList.record(ParseUtil.unknownUpdateVariable(name, ctx.target));
                return false;
            }
            return ctx.expr().accept(this) & checkType(ctx.value, getTypeOf(name));
        }

        @Override
        public Boolean visitMeasure_declaration(MarkovChainModelParser.Measure_declarationContext ctx) {
            if (checkAndRecord(ctx.name)) {
                stateVariablesAllowed = true;
                return checkType(ctx.expr(), DataType.REAL);
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitParam_declaration(MarkovChainModelParser.Param_declarationContext ctx) {
            if (checkAndRecord(ctx.name)) {
                types.put(ctx.name.getText(), DataType.REAL);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
            return (getTypeOf(ctx.getText(), localVariables)!= DataType.NONE)&&(stateVariablesAllowed||!stateVariables.contains(ctx.reference.getText()));
        }

        @Override
        protected Boolean defaultResult() {
            return true;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate&&nextResult;
        }
    }

    private boolean checkType(Set<String> localVariables, MarkovChainModelParser.ExprContext ctx, DataType expected) {
        DataType actual = ctx.accept(new TypeVisitor(n -> getTypeOf(n, localVariables), errorList));
        if (!actual.isSubtypeOf(expected)) {
            errorList.record(ParseUtil.wrongTypeError(expected, actual, ctx));
            return false;
        }
        return true;
    }

    private boolean checkType(MarkovChainModelParser.ExprContext ctx, DataType expected) {
        return checkType(Set.of(), ctx, expected);
    }



}
