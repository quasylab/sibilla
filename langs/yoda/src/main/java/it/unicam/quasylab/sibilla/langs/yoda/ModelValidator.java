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

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.xml.validation.Validator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ModelValidator {

    private final ErrorCollector errorCollector;
    private final Map<String, Token> table;
    private final Map<String, DataType> types;


    public ModelValidator(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.table = new HashMap<>();
        this.types = new HashMap<>();
    }

    public boolean validate(ParseTree parseTree){
        if (parseTree==null){
            return false;
        }
        return parseTree.accept(new ValidatorVisitor());
    }

    public Function<String, DataType> getTypes(){ return this::getTypesOf;}

    private DataType getTypesOf(String s) {
        return getTypesOf(s, Set.of());
    }

    private DataType getTypesOf(String s, Set<String> variables) {
        if((variables!=null)&&(variables.contains(s))){
            return DataType.REAL;
        }else {
            return this.types.getOrDefault(s, DataType.NONE);
        }
    }

    private boolean checkType(YodaModelParser.ExprContext ctx, DataType expected){
        return checkType(Set.of(), ctx,expected);
    }

    private boolean checkType(Set<String> localVar, YodaModelParser.ExprContext ctx, DataType expected){
        DataType actual= ctx.accept(new TypeVisitor(n -> getTypesOf(n, localVar), errorCollector));
        if (!actual.isSubtypeOf(expected)){
            errorCollector.record(ParseUtil.wrongTypeError(expected,actual,ctx));
            return false;
        }
        return true;
    }

    public class ValidatorVisitor extends  YodaModelBaseVisitor<Boolean>{

        private final Set<String> stateVariables = new HashSet<>();
        private boolean stateVariablesAllowed = false;

        private boolean checkAndRecord(Token token){
            String name = token.getText();
            if (table.containsKey(name)){
                errorCollector.record(ParseUtil.duplicatedIdentifierError(name, token, table.get(name)));
                return false;
            }else{
                table.put(name, token);
                return true;
            }
        }

        @Override
        public Boolean visitModel(YodaModelParser.ModelContext ctx) {
            boolean flag = true;
            for (YodaModelParser.ElementContext e: ctx.element()) {
                flag &= e.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitConstant_declaration(YodaModelParser.Constant_declarationContext ctx) {
            this.stateVariablesAllowed=false;
            if (checkAndRecord(ctx.name)&&ctx.expr().accept(this)){
                types.put(ctx.name.getText(), TypeVisitor.getTypeOf(errorCollector, ModelValidator.this::getTypesOf, ctx.expr()));
                return true;
            }else{
                return false;
            }
        }

        //TODO
        @Override
        public Boolean visitParameter_declaration(YodaModelParser.Parameter_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitType_declaration(YodaModelParser.Type_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitAgent_declaration(YodaModelParser.Agent_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitState_declaration(YodaModelParser.State_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitObservation_declaration(YodaModelParser.Observation_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitAction_declaration(YodaModelParser.Action_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitAction_body(YodaModelParser.Action_bodyContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitBehaviour_declaration(YodaModelParser.Behaviour_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitBehaviour_rule(YodaModelParser.Behaviour_ruleContext ctx) {
            boolean flag = true;
            for (YodaModelParser.ExprContext guards : ctx.expr()) {
                flag &= checkType(guards, DataType.BOOLEAN);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitDef_behaviour_rule(YodaModelParser.Def_behaviour_ruleContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitSystem_declaration(YodaModelParser.System_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitGlobal_state_declaration(YodaModelParser.Global_state_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitScene_field(YodaModelParser.Scene_fieldContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitHidden_field(YodaModelParser.Hidden_fieldContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitSensing_declaration(YodaModelParser.Sensing_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitAgent_sensing(YodaModelParser.Agent_sensingContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitEnv_evolution_declaration(YodaModelParser.Env_evolution_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitDef_env_evolution_rule(YodaModelParser.Def_env_evolution_ruleContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitConfiguration_declaration(YodaModelParser.Configuration_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitAssignment_declaration(YodaModelParser.Assignment_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitCollective_declaration(YodaModelParser.Collective_declarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitCollective_body(YodaModelParser.Collective_bodyContext ctx) {
            return null;
        }
    }
}
