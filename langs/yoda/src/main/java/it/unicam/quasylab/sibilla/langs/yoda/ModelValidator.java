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
        return checkType(Set.of(), ctx, expected);
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


        private final Set<String> agentSysList = new HashSet<>();
        private final Set<String> stateList = new HashSet<>();
        //private final Set<String> observationList = new HashSet<>();
        private boolean stateVariablesAllowed = false;
        private final Set<String> actionList = new HashSet<>();
        private final Set<String> behavList = new HashSet<>();

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
        public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
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
        public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitTypeDeclaration(YodaModelParser.TypeDeclarationContext ctx) {
            return null;
        }

        @Override
        public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.agentName)){
                agentSysList.add(ctx.agentName.getText());
                flag = true;
            } else {
                return false;
            }
            flag &= ctx.knowledgeDeclaration().accept(this);
            flag &= ctx.informationDeclaration().accept(this);
            flag &= ctx.observationDeclaration().accept(this);
            for (YodaModelParser.ActionBodyContext actionBodyContext : ctx.actionBody()) {
                flag &= actionBodyContext.accept(this);
            }
            flag &= ctx.behaviourDeclaration().accept(this);
            return flag;


        }

        @Override
        public Boolean visitKnowledgeDeclaration(YodaModelParser.KnowledgeDeclarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.NewFieldContext newFieldContext : ctx.newField()) {
                flag &= newFieldContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitInformationDeclaration(YodaModelParser.InformationDeclarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.NewFieldContext newFieldContext : ctx.newField()) {
                flag &= newFieldContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitObservationDeclaration(YodaModelParser.ObservationDeclarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.NewFieldContext newFieldContext : ctx.newField()) {
                flag &= newFieldContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitNewField(YodaModelParser.NewFieldContext ctx) {
            //Check e save del type
            if (checkAndRecord(ctx.fieldName)){
                //inserimento nella map di types con string e
                stateList.add(ctx.fieldName.getText());
                return true;
            }else{
                return false;
            }
        }

        @Override
        public Boolean visitActionBody(YodaModelParser.ActionBodyContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.actionName)){
                actionList.add(ctx.actionName.getText());
                flag &= true;
            } else {
                return false;
            }
            for (YodaModelParser.FieldUpdateContext fieldUpdateContext : ctx.fieldUpdate()){
                flag &= fieldUpdateContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitFieldUpdate(YodaModelParser.FieldUpdateContext ctx) {
            String name = ctx.fieldName.getText();
            if(!stateList.contains(name)){
                errorCollector.record(ParseUtil.unknownVariableError(name, ctx.fieldName));
                return false;
            }
            return ctx.expr().accept(this) & (checkType(ctx.value, DataType.REAL)||checkType(ctx.value, DataType.INTEGER));
        }

        @Override
        public Boolean visitBehaviourDeclaration(YodaModelParser.BehaviourDeclarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.RuleDeclarationContext ruleDeclarationContext : ctx.ruleDeclaration()) {
                flag &= ruleDeclarationContext.accept(this);
            }
            flag &= ctx.defaultRule().accept(this);
            return flag;
        }

        @Override
        public Boolean visitRuleDeclaration(YodaModelParser.RuleDeclarationContext ctx) {
            boolean flag = true;
            flag &= ctx.expr().accept(this) & (checkType(ctx.boolExpr, DataType.BOOLEAN));
            for (YodaModelParser.WeightedRuleContext weightedRuleContext : ctx.weightedRule()) {
                flag &= weightedRuleContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitDefaultRule(YodaModelParser.DefaultRuleContext ctx) {
            boolean flag = true;
            for (YodaModelParser.WeightedRuleContext weightedRuleContext : ctx.weightedRule()) {
                flag &= weightedRuleContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitWeightedRule(YodaModelParser.WeightedRuleContext ctx) {
            String name = ctx.actionName.getText();
            if (!actionList.contains(name)) {
                errorCollector.record(ParseUtil.unknownActionError(name, ctx.actionName));
                return false;
            }
            return ctx.expr().accept(this) & (checkType(ctx.weight, DataType.REAL)||checkType(ctx.weight, DataType.INTEGER));
        }


        @Override
        public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.name)) {
                agentSysList.add(ctx.name.getText());
                flag = true;
            } else {
                return false;
            }
            for (YodaModelParser.NewFieldContext newFieldContext : ctx.newField()) {
                flag &= newFieldContext.accept(this);
            }
            flag &= (ctx.assignmentTemp()==null || ctx.assignmentTemp().accept(this));
            for (YodaModelParser.AgentSensingContext agentSensingContext : ctx.agentSensing()) {
                flag &= agentSensingContext.accept(this);
            }
            for (YodaModelParser.EvolutionDeclarationContext evolutionDeclarationContext : ctx.evolutionDeclaration()) {
                flag &= evolutionDeclarationContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitAssignmentTemp(YodaModelParser.AssignmentTempContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitSelectionBlock(YodaModelParser.SelectionBlockContext ctx) {
            return null;
        }

        @Override
        public Boolean visitAgentSensing(YodaModelParser.AgentSensingContext ctx) {
            boolean flag = true;
            String name = ctx.agentName.getText();
            if (!agentSysList.contains(name)) {
                errorCollector.record(ParseUtil.unknownAgentError(name, ctx.agentName));
                return false;
            }
            for (YodaModelParser.FieldUpdateContext fieldUpdateContext : ctx.fieldUpdate()) {
                flag &= fieldUpdateContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitEvolutionDeclaration(YodaModelParser.EvolutionDeclarationContext ctx) {
            boolean flag = true;
            String name = ctx.entityName.getText();
            if (!agentSysList.contains(name) || !stateList.contains(name)) {
                errorCollector.record(ParseUtil.unknownEntityError(name, ctx.entityName));
                return false;
            }
            for (YodaModelParser.FieldUpdateContext fieldUpdateContext : ctx.fieldUpdate()) {
                flag &= fieldUpdateContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitConfigurationDeclaration(YodaModelParser.ConfigurationDeclarationContext ctx) {
            return super.visitConfigurationDeclaration(ctx);
        }

        //TODO
        @Override
        public Boolean visitAssignmentDeclaration(YodaModelParser.AssignmentDeclarationContext ctx) {
            return super.visitAssignmentDeclaration(ctx);
        }

        //TODO
        @Override
        public Boolean visitCollectiveDeclaration(YodaModelParser.CollectiveDeclarationContext ctx) {
            return super.visitCollectiveDeclaration(ctx);
        }

        //TODO
        @Override
        public Boolean visitFieldInit(YodaModelParser.FieldInitContext ctx) {
            return super.visitFieldInit(ctx);
        }

        /*
                                                                                        //TODO
                                                                                        // vogliamo lasciare tutto in REAL?
                                                                                        @Override
                                                                                        public Boolean visitState_declaration(YodaModelParser.State_declarationContext ctx) {
                                                                                            boolean flag = true;
                                                                                            //check type function
                                                                                            if (checkAndRecord(ctx.name)){
                                                                                                types.put(ctx.name.getText(), DataType.REAL);
                                                                                                stateList.add(ctx.name.getText());
                                                                                                flag = true;
                                                                                            }else{
                                                                                                return false;
                                                                                            }
                                                                                            flag = (ctx.value == null)||checkType(ctx.value, DataType.REAL);
                                                                                            return flag;
                                                                                        }

                                                                                        //TODO
                                                                                        @Override
                                                                                        public Boolean visitObservation_declaration(YodaModelParser.Observation_declarationContext ctx) {
                                                                                            //check type function
                                                                                            if (checkAndRecord(ctx.name)){
                                                                                                types.put(ctx.name.getText(), DataType.BOOLEAN);
                                                                                                observationVariables.add(ctx.name.getText());
                                                                                                return true;
                                                                                            }else{
                                                                                                return false;
                                                                                            }
                                                                                        }


                                                                                        @Override
                                                                                        public Boolean visitAction_declaration(YodaModelParser.Action_declarationContext ctx) {
                                                                                            boolean flag = true;
                                                                                            if (checkAndRecord(ctx.action_name)){
                                                                                                actionList.add(ctx.action_name.getText());
                                                                                                flag=true;
                                                                                            } else {
                                                                                                return false;
                                                                                            }
                                                                                            for (YodaModelParser.Action_bodyContext action_bodyContext : ctx.action_body()){
                                                                                                flag &= action_bodyContext.accept(this);
                                                                                            }
                                                                                            return flag;
                                                                                        }



                                                                                        @Override
                                                                                        public Boolean visitTerminal_action_body(YodaModelParser.Terminal_action_bodyContext ctx) {
                                                                                            String name = ctx.state_name.getText();
                                                                                            if (!stateList.contains(name)){
                                                                                                errorCollector.record(ParseUtil.unknownVariableError(name, ctx.state_name));
                                                                                                return false;
                                                                                            }
                                                                                            return ctx.expr().accept(this) & (checkType(ctx.value, DataType.REAL)||checkType(ctx.value, DataType.INTEGER));
                                                                                        }

        @Override
        public Boolean visitAgent_ref_action_body(YodaModelParser.Agent_ref_action_bodyContext ctx) {
            boolean flag = agentSysList.contains(ctx.agent_reference.getText());
            for (YodaModelParser.Terminal_action_bodyContext terminal_actionContext : ctx.terminal_action_body()) {
                flag &= terminal_actionContext.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitBehaviour_declaration(YodaModelParser.Behaviour_declarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.name)){
                behavList.add(ctx.name.getText());
                flag = true;
            }else{
                return false;
            }
            for (YodaModelParser.Behaviour_ruleContext behaviour_ruleContext : ctx.behaviour_rule()) {
                flag &= behaviour_ruleContext.accept(this);
            }
            flag = ctx.def_behaviour_rule().accept(this);
            return flag;
        }

        //TODO
        // risolvere il for che prende tutte le expr
        @Override
        public Boolean visitBehaviour_rule(YodaModelParser.Behaviour_ruleContext ctx) {
            boolean flag = true;
            String name = ctx.action_name.getText();
            for (YodaModelParser.ExprContext guards : ctx.expr()) {
                flag &= checkType(guards, DataType.BOOLEAN);
            }
            if (!actionList.contains(name)){
                errorCollector.record(ParseUtil.unknownActionError(name,ctx.action_name));
                flag = false;
            }
            flag &= checkType(ctx.weight, DataType.REAL);
            return flag;
        }

        @Override
        public Boolean visitDef_behaviour_rule(YodaModelParser.Def_behaviour_ruleContext ctx) {
            String name = ctx.action_name.getText();
            if (!actionList.contains(name)){
                errorCollector.record(ParseUtil.unknownActionError(name, ctx.action_name));
                return false;
            }
            return ctx.expr().accept(this) & checkType(ctx.weight, DataType.REAL);
        }

        //TODO
        // constr_params chiama anche gli altri
        @Override
        public Boolean visitSystem_declaration(YodaModelParser.System_declarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.name)){
                agentSysList.add(ctx.name.getText());
                flag = true;
            }else{
                return false;
            }
            for (YodaModelParser.Constr_paramsContext constr_paramsContext : ctx.constr_params()){
                flag &= constr_paramsContext.accept(this);
            }
            flag &= ctx.global_state_declaration().accept(this);
            flag &= ctx.sensing_declaration().accept(this);
            flag &= ctx.action_declaration().accept(this);
            flag &= ctx.env_evolution_declaration().accept(this);
            return flag;
        }

        @Override
        public Boolean visitGlobal_state_declaration(YodaModelParser.Global_state_declarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.Global_field_declarationContext global_field_declarationContext : ctx.global_field_declaration()){
                flag &= global_field_declarationContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitScene_field(YodaModelParser.Scene_fieldContext ctx) {
            return null;
        }

        //TODO
        @Override
        public Boolean visitHidden_field(YodaModelParser.Hidden_fieldContext ctx) {
            String name = ctx.agent_name.getText();
            boolean flag = true;
            if (!agentSysList.contains(name)){
                errorCollector.record(ParseUtil.unknownAgentError(name, ctx.agent_name));
                return false;
            }
            //Check type
            return null;
        }

        @Override
        public Boolean visitSensing_declaration(YodaModelParser.Sensing_declarationContext ctx) {
            boolean flag = true;
            for (YodaModelParser.Agent_sensingContext agent_sensingContext : ctx.agent_sensing()) {
                flag &= agent_sensingContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitAgent_sensing(YodaModelParser.Agent_sensingContext ctx) {
            String name = ctx.agent_name.getText();
            if (!agentSysList.contains(name)){
                errorCollector.record(ParseUtil.unknownAgentError(name, ctx.agent_name));
                return false;
            }
            return null;
        }

        @Override
        public Boolean visitEnv_evolution_declaration(YodaModelParser.Env_evolution_declarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.name)){
                behavList.add(ctx.name.getText());
                flag = true;
            }else{
                return false;
            }
            flag = ctx.def_env_evolution_rule().accept(this);
            return flag;
        }

        @Override
        public Boolean visitDef_env_evolution_rule(YodaModelParser.Def_env_evolution_ruleContext ctx) {
            String name = ctx.env_rule.getText();
            if (!actionList.contains(name)){
                errorCollector.record(ParseUtil.unknownActionError(name, ctx.env_rule));
                return false;
            }
            return ctx.expr().accept(this) & checkType(ctx.weight, DataType.REAL);
        }

        @Override
        public Boolean visitConfiguration_declaration(YodaModelParser.Configuration_declarationContext ctx) {
            boolean flag = true;
            if (checkAndRecord(ctx.name)){
                agentSysList.add(ctx.name.getText());
                flag=true;
            }else{
                return false;
            }
            flag &= ctx.assignment_declaration()==null||ctx.assignment_declaration().accept(this);
            flag &= ctx.collective_declaration().accept(this);
            return flag;
        }

        //TODO
        @Override
        public Boolean visitAssignment_declaration(YodaModelParser.Assignment_declarationContext ctx) {
            return null;
        }

        @Override
        public Boolean visitCollective_declaration(YodaModelParser.Collective_declarationContext ctx) {
            boolean flag = true;
            String name = ctx.collective_name.getText();
            if (!agentSysList.contains(name)){
                errorCollector.record(ParseUtil.unknownAgentError(name, ctx.collective_name));
                return false;
            }
            for (YodaModelParser.Collective_bodyContext collective_bodyContext : ctx.collective_body()) {
                flag &= collective_bodyContext.accept(this);
            }
            return flag;
        }

        //TODO
        @Override
        public Boolean visitCollective_body(YodaModelParser.Collective_bodyContext ctx) {
            return null;
        }*/
    }
}
