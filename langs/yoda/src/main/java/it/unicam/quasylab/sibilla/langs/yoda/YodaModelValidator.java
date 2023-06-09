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
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * This visitor class is used to validate the model
 */
public class YodaModelValidator extends YodaModelBaseVisitor<Boolean> {


    private final SymbolTable table;
    private final ErrorCollector errors;

    private ExpressionContext expressionContext = ExpressionContext.NONE;
    private String entityContext;

    private Map<String, YodaType> constantsAndParametersTypes = new HashMap<>();

    public YodaModelValidator(ErrorCollector errors) {
        this.errors = errors;
        this.table = new SymbolTable();
    }

    public SymbolTable getTable() {
        return table;
    }

    public boolean hasType(YodaType expectedType, YodaModelParser.ExprContext expr, Function<String, YodaType>  typeSolver) {
        YodaType actual = expr.accept(new TypeInferenceVisitor(errors, typeSolver));
        if (!actual.equals(YodaType.NONE_TYPE) && !actual.equals(expectedType)) {
            errors.record(ParseUtil.wrongTypeError(expectedType, actual, expr));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean visitModel(YodaModelParser.ModelContext ctx) {
        boolean res = true;
        for(YodaModelParser.ElementContext e : ctx.element()) {
            res &= e.accept(this);
        }
        return res && !errors.withErrors();
    }

    /*
    //TODO
    @Override
    public Boolean visitElement(YodaModelParser.ElementContext ctx) {
        return super.visitElement(ctx);
    }

     */

    @Override
    public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
        String name = ctx.name.getText();
        if (table.existsDeclaration(name)) {
            errors.record(ParseUtil.duplicatedIdentifierError(name, table.getDeclarationToken(name)));
            return false;
        }
        this.expressionContext = ExpressionContext.CONSTANT;
        TypeInferenceVisitor tiv = new TypeInferenceVisitor(this.errors, s -> this.constantsAndParametersTypes.getOrDefault(s, YodaType.NONE_TYPE));
        YodaType constType = ctx.value.accept(tiv);
        if (!constType.equals(YodaType.NONE_TYPE)) {
            table.addConstants(ctx, constType);
            this.constantsAndParametersTypes.put(name, constType);
            return true;
        }
        return false;
    }

    @Override
    public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
        String name = ctx.name.getText();
        if (table.existsDeclaration(name)) {
            errors.record(ParseUtil.duplicatedIdentifierError(name, table.getDeclarationToken(name)));
            return false;
        }
        this.expressionContext = ExpressionContext.CONSTANT;
        TypeInferenceVisitor tiv = new TypeInferenceVisitor(this.errors, s -> this.constantsAndParametersTypes.getOrDefault(s, YodaType.NONE_TYPE));
        YodaType constType = ctx.value.accept(tiv);
        if (!constType.equals(YodaType.NONE_TYPE)) {
            table.addParameter(ctx, constType);
            this.constantsAndParametersTypes.put(ctx.name.getText(), constType);
            return true;
        }
        return false;
    }

    //TODO
    @Override
    public Boolean visitTypeDeclaration(YodaModelParser.TypeDeclarationContext ctx) {
        return super.visitTypeDeclaration(ctx);
    }

    //TODO
    @Override
    public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
        try {
            String agentName = ctx.agentName.getText();
            boolean res = true;
            this.entityContext = agentName;
            if (table.existsAgent(agentName)) {
                this.errors.record(ParseUtil.duplicatedEntityError(agentName, table.getDeclarationToken(agentName)));
                res = false;
            } else {
                table.addAgent(ctx);
                res &= recordAgentKnowledge(ctx);
                res &= recordAgentInformation(ctx);
                res &= recordAgentObservation(ctx);
                res &= recordAgentAction(ctx);
            }
            return res;
        } finally {
            this.entityContext = null;
        }
    }

    private boolean recordAgentKnowledge(YodaModelParser.AgentDeclarationContext ctx) {
        boolean res = true;
        String agentName = ctx.agentName.getText();
        for (YodaModelParser.FieldDeclarationContext field : ctx.knowledgeDeclaration().fields) {
            res &= recordAgentKnowledge(agentName, field);
        }
        return res;
    }

    //TODO
    private boolean recordAgentKnowledge(String agentName, YodaModelParser.FieldDeclarationContext field) {
        String fieldName = field.fieldName.getText();
        String localVarName = ParseUtil.localVariableName(agentName, fieldName);
        if (table.existsDeclaration(localVarName)) {
            this.errors.record(ParseUtil.duplicatedEntityError(localVarName, table.getDeclarationToken(agentName)));
            return false;
        }
        if (table.existsAgentKnowledge(agentName, fieldName) || table.existsAgentInformation(agentName, fieldName) || table.existsAgentObservation(agentName, fieldName)){
            this.errors.record(ParseUtil.duplicatedFieldError(fieldName, field.fieldName));
            return false;
        }
        //registrare il tipo della variabile
        YodaType fieldType = YodaType.NONE_TYPE;
        if (YodaType.NONE_TYPE.equals(fieldType)) {
            return false;
        } else {
            table.recordAgentKnowledge(agentName, field.fieldName, fieldName, fieldType);
            return true;
        }

    }


    private boolean recordAgentInformation(YodaModelParser.AgentDeclarationContext ctx) {
        boolean res = true;
        String agentName = ctx.agentName.getText();
        for (YodaModelParser.FieldDeclarationContext field : ctx.informationDeclaration().fields) {
            res &= recordAgentInformation(agentName,field);
        }
        return res;
    }

    //TODO
    private boolean recordAgentInformation(String agentName, YodaModelParser.FieldDeclarationContext field) {
        String fieldName = field.fieldName.getText();
        String localVarName = ParseUtil.localVariableName(agentName, fieldName);
        if (table.existsDeclaration(localVarName)) {
            this.errors.record(ParseUtil.duplicatedIdentifierError(localVarName, table.getDeclarationToken(agentName)));
            return false;
        }
        if (table.existsAgentKnowledge(agentName, fieldName) || table.existsAgentInformation(agentName, fieldName) || table.existsAgentObservation(agentName, fieldName)){
            this.errors.record(ParseUtil.duplicatedFieldError(fieldName, field.fieldName));
            return false;
        }
        //registrare il tipo della variabile
        YodaType fieldType = YodaType.NONE_TYPE;
        if (YodaType.NONE_TYPE.equals(fieldType)) {
            return false;
        } else {
            table.recordAgentInformation( agentName, field.fieldName, fieldName, fieldType);
            return true;
        }
    }


    private boolean recordAgentObservation(YodaModelParser.AgentDeclarationContext ctx) {
        boolean res = true;
        String agentName = ctx.agentName.getText();
        for (YodaModelParser.FieldDeclarationContext field : ctx.observationDeclaration().fields) {
            res &= recordAgentObservation(agentName,field);
        }
        return res;
    }

    //TODO
    private boolean recordAgentObservation(String agentName, YodaModelParser.FieldDeclarationContext field) {
        String fieldName = field.fieldName.getText();
        String localVarName = ParseUtil.localVariableName(agentName, fieldName);
        if (table.existsDeclaration(localVarName)) {
            this.errors.record(ParseUtil.duplicatedIdentifierError(localVarName, table.getDeclarationToken(agentName)));
            return false;
        }
        if (table.existsAgentKnowledge(agentName, fieldName) || table.existsAgentInformation(agentName, fieldName) || table.existsAgentObservation(agentName, fieldName)){
            this.errors.record(ParseUtil.duplicatedFieldError(fieldName, field.fieldName));
            return false;
        }
        //registrare il tipo della variabile
        YodaType fieldType = YodaType.NONE_TYPE;
        if(YodaType.NONE_TYPE.equals(fieldType)){
            return false;
        } else {
            table.recordAgentObservation(agentName, field.fieldName, fieldName, fieldType);
            return true;
        }
    }

    private boolean recordAgentAction(YodaModelParser.AgentDeclarationContext ctx) {
        boolean res = true;
        String agentName = ctx.agentName.getText();
        for (YodaModelParser.ActionBodyContext actionBody : ctx.actionBody()){
            res &= recordAgentAction(agentName, actionBody);
        }
        return res;
    }

    //TODO
    private boolean recordAgentAction(String agentName, YodaModelParser.ActionBodyContext actionBody) {
        boolean res = true;
        String actionName = actionBody.actionName.getText();
        String localActionName = ParseUtil.localVariableName(agentName, actionName);
        if (table.existsDeclaration(localActionName)) {
            this.errors.record(ParseUtil.duplicatedIdentifierError(actionName, table.getDeclarationToken(agentName)));
            return false;
        }
        if (table.existsAgentAction(agentName, actionName)) {
            this.errors.record(ParseUtil.duplicatedActionName(actionName, actionBody.actionName));
            return false;
        }
        for (YodaModelParser.FieldUpdateContext f : actionBody.fieldUpdate()){
            res &= checkFieldUpdate(agentName, f);
        }
        table.recordAgentAction(agentName, actionBody.actionName, actionBody);
        return res;
    }

    //TODO
    private boolean checkFieldUpdate(String agentName, YodaModelParser.FieldUpdateContext fieldUpdate) {
        return false;
    }



    /*
    @Override
    public Boolean visitKnowledgeDeclaration(YodaModelParser.KnowledgeDeclarationContext ctx) {
        boolean res = true;
        for (YodaModelParser.NewFieldContext newField : ctx.newField()) {
            res &= newField.accept(this);
        }
        return res;
    }



    @Override
    public Boolean visitInformationDeclaration(YodaModelParser.InformationDeclarationContext ctx) {
        boolean res = true;
        for (YodaModelParser.NewFieldContext newField : ctx.newField()) {
            res &= newField.accept(this);
        }
        return res;
    }

    @Override
    public Boolean visitObservationDeclaration(YodaModelParser.ObservationDeclarationContext ctx) {
        boolean res = true;
        for (YodaModelParser.NewFieldContext newField : ctx.newField()) {
            res &= newField.accept(this);
        }
        return res;
    }

     */

    //TODO
    @Override
    public Boolean visitFieldDeclaration(YodaModelParser.FieldDeclarationContext ctx) {
        return super.visitFieldDeclaration(ctx);
    }


    //TODO
    @Override
    public Boolean visitActionBody(YodaModelParser.ActionBodyContext ctx) {
        return super.visitActionBody(ctx);
    }

    //TODO
    @Override
    public Boolean visitFieldUpdate(YodaModelParser.FieldUpdateContext ctx) {
        return super.visitFieldUpdate(ctx);
    }

    //TODO
    @Override
    public Boolean visitBehaviourDeclaration(YodaModelParser.BehaviourDeclarationContext ctx) {
        boolean res = true;
        for (YodaModelParser.RuleDeclarationContext ruleDeclaration : ctx.ruleDeclaration()){
            res &= ruleDeclaration.accept(this);
        }
        res &= ctx.defaultRule().accept(this);
        return res;
    }

    @Override
    public Boolean visitRuleDeclaration(YodaModelParser.RuleDeclarationContext ctx) {
        boolean res = true;
        res &= hasType(YodaType.BOOLEAN_TYPE, ctx.boolExpr, table.getAgentTypeSolver(entityContext));
        for (YodaModelParser.WeightedRuleContext w : ctx.weightedRule()) {
            res &= w.accept(this);
        }
        return res;
    }

    @Override
    public Boolean visitDefaultRule(YodaModelParser.DefaultRuleContext ctx) {
        boolean res = true;
        for (YodaModelParser.WeightedRuleContext rule : ctx.weightedRule()) {
            res &= rule.accept(this);
        }
        return res;
    }

    //TODO
    @Override
    public Boolean visitWeightedRule(YodaModelParser.WeightedRuleContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
        return super.visitSystemDeclaration(ctx);
    }

    //TODO
    @Override
    public Boolean visitAssignmentTemp(YodaModelParser.AssignmentTempContext ctx) {
        return super.visitAssignmentTemp(ctx);
    }

    //TODO
    @Override
    public Boolean visitSelectionBlock(YodaModelParser.SelectionBlockContext ctx) {
        return super.visitSelectionBlock(ctx);
    }

    //TODO
    @Override
    public Boolean visitAgentSensing(YodaModelParser.AgentSensingContext ctx) {
        return super.visitAgentSensing(ctx);
    }

    //TODO
    @Override
    public Boolean visitEvolutionDeclaration(YodaModelParser.EvolutionDeclarationContext ctx) {
        return super.visitEvolutionDeclaration(ctx);
    }

    //TODO
    @Override
    public Boolean visitConfigurationDeclaration(YodaModelParser.ConfigurationDeclarationContext ctx) {
        boolean res = true;
        return res;
    }

    //TODO
    @Override
    public Boolean visitCollectionDeclaration(YodaModelParser.CollectionDeclarationContext ctx) {
        return super.visitCollectionDeclaration(ctx);
    }



    //TODO
    @Override
    public Boolean visitFieldInit(YodaModelParser.FieldInitContext ctx) {
        return super.visitFieldInit(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        return super.visitExpressionNegation(ctx);
    }

    @Override
    public Boolean visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    //TODO
    @Override
    public Boolean visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        return super.visitExpressionMinimum(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        return super.visitExpressionMaximum(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        return super.visitExpressionRelation(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        return super.visitExpressionReference(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        return super.visitExpressionSquareRoot(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        return super.visitExpressionAnd(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        return super.visitExpressionForAll(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        return super.visitExpressionExists(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        return super.visitExpressionAddSubOperation(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        return super.visitExpressionInteger(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        return super.visitExpressionUnary(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        return super.visitExpressionItselfRef(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionWeightedRandom(YodaModelParser.ExpressionWeightedRandomContext ctx) {
        return super.visitExpressionWeightedRandom(ctx);
    }

    @Override
    public Boolean visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return true;
    }

    //TODO
    @Override
    public Boolean visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        return super.visitExpressionMultDivOperation(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        return super.visitExpressionOr(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionExponentOperation(YodaModelParser.ExpressionExponentOperationContext ctx) {
        return super.visitExpressionExponentOperation(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        return super.visitExpressionReal(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        return super.visitExpressionAdditionalOperation(ctx);
    }

    @Override
    public Boolean visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return true;
    }

    //TODO
    @Override
    public Boolean visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        return super.visitExpressionIfThenElse(ctx);
    }

    @Override
    public Boolean visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        return true;
    }

    //TODO
    @Override
    public Boolean visitExpressionAttributeRef(YodaModelParser.ExpressionAttributeRefContext ctx) {
        return super.visitExpressionAttributeRef(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        return super.visitExpressionCeiling(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return super.visitExpressionCos(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return super.visitExpressionAtan(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        return super.visitExpressionFloor(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return super.visitExpressionAsin(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return super.visitExpressionCosh(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return super.visitExpressionTan(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return super.visitExpressionAcos(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return super.visitExpressionSin(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        return super.visitExpressionRecord(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return super.visitExpressionSinh(ctx);
    }

    //TODO
    @Override
    public Boolean visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return super.visitExpressionTanh(ctx);
    }

    @Override
    public Boolean visitTypeInteger(YodaModelParser.TypeIntegerContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTypeReal(YodaModelParser.TypeRealContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTypeBoolean(YodaModelParser.TypeBooleanContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTypeCharacter(YodaModelParser.TypeCharacterContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTypeString(YodaModelParser.TypeStringContext ctx) {
        return true;
    }

    //TODO
    @Override
    public Boolean visitTypeArrayMultipleTypes(YodaModelParser.TypeArrayMultipleTypesContext ctx) {
        return super.visitTypeArrayMultipleTypes(ctx);
    }

    //TODO
    @Override
    public Boolean visitTypeNew(YodaModelParser.TypeNewContext ctx) {
        return super.visitTypeNew(ctx);
    }

    //TODO
    @Override
    public Boolean visitFunctionGenerate(YodaModelParser.FunctionGenerateContext ctx) {
        return super.visitFunctionGenerate(ctx);
    }

    //TODO
    @Override
    public Boolean visitFunctionDistinct(YodaModelParser.FunctionDistinctContext ctx) {
        return super.visitFunctionDistinct(ctx);
    }

    //TODO
    @Override
    public Boolean visitFunctionDistinctFrom(YodaModelParser.FunctionDistinctFromContext ctx) {
        return super.visitFunctionDistinctFrom(ctx);
    }


    /*
    public class SymbolCollector extends YodaModelBaseVisitor<Boolean> {

        @Override
        public Boolean visitModel(YodaModelParser.ModelContext ctx) {
            boolean flag = true;
            for (YodaModelParser.ElementContext e : ctx.element()) {
                flag &= e.accept(this);
            }
            return flag;
        }

        @Override
        public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.name.getText())) {
                String name = ctx.name.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.name, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addConstants(ctx);
                return true;
            }
        }

        @Override
        public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.name.getText())){
                String name = ctx.name.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.name, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addParameter(ctx);
                return true;
            }
        }

        @Override
        public Boolean visitTypeDeclaration(YodaModelParser.TypeDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.name.getText())) {
                String name = ctx.name.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.name, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addType(ctx);
                return true;
            }
        }

        @Override
        public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.agentName.getText())) {
                String name = ctx.agentName.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.agentName, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addAgent(ctx);
                return true;
            }
        }

        @Override
        public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.name.getText())) {
                String name = ctx.name.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.name, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addSystem(ctx);
                return true;
            }
        }

        @Override
        public Boolean visitConfigurationDeclaration(YodaModelParser.ConfigurationDeclarationContext ctx) {
            if (table.existsDeclaration(ctx.name.getText())) {
                String name = ctx.name.getText();
                errors.record(ParseUtil.duplicatedIdentifierError(name, ctx.name, table.getDeclarationToken(name)));
                return false;
            } else {
                table.addConfiguration(ctx);
                return true;
            }
        }


    }

     */
}
