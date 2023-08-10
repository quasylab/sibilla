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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementNameRegistry;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * This visitor class is used to validate the model
 */
public class YodaModelValidator {


    private final YodaElementAttributeTable elementAttributeTable;

    private final Map<String, ParserRuleContext> declarations;

    private final Map<String, YodaType.RecordType> declaredFields;

    private final Map<String, YodaType> constantsAndParameters;

    private final ErrorCollector errors;
    private final Map<String, YodaType> yodaTypes;
    private final Map<String, Set<String>> agentsActions;
    private final Map<String, YodaType> sceneAttributes;


    public YodaModelValidator(ErrorCollector errors) {
        this.errors = errors;
        elementAttributeTable = new YodaElementAttributeTable();
        constantsAndParameters = new HashMap<>();
        declaredFields = new HashMap<>();
        declarations = new HashMap<>();
        yodaTypes = YodaType.getMapOfYodaType();
        agentsActions = new HashMap<>();
        sceneAttributes = new HashMap<>();
    }

    public boolean validate(ParseTree parseTree) {
        return parseTree.accept(new CustomTypeCollector())
                & parseTree.accept(new AgentAttributesCollector())
                & parseTree.accept(new ModelElementValidator());
    }

    public YodaElementNameRegistry getElementNameRegistry() {
        return this.elementAttributeTable.getRegistry();
    }

    private class CustomTypeCollector extends YodaModelBaseVisitor<Boolean> {

        @Override
        public Boolean visitModel(YodaModelParser.ModelContext ctx) {
            return ctx.element().stream().allMatch(e -> e.accept(this));
        }

        @Override
        public Boolean visitTypeDeclaration(YodaModelParser.TypeDeclarationContext ctx) {
            String recordName = ctx.typeName.getText();
            boolean flag = true;
            if (checkUniquenessOfName(recordName, ctx)) {
                Map<String, YodaType> recordFields = new HashMap<>();
                for (YodaModelParser.RecordFieldDeclarationContext fd: ctx.fields) {
                    String fieldName = fd.name.getText();
                    if (declaredFields.containsKey(fieldName)) {
                        errors.record(ParseUtil.duplicatedFieldError(fd.name, declaredFields.get(fieldName).getName()));
                        flag = false;
                    } else {
                        if (recordFields.containsKey(fieldName)) {
                            errors.record(ParseUtil.duplicatedFieldError(fd.name, recordName));
                            flag = false;
                        }
                        recordFields.put(fieldName, getYodaType(fd.type()));
                    }
                }
                registerRecord(recordName, recordFields);
                return flag;
            } else {
                return false;
            }
        }

        @Override
        protected Boolean defaultResult() {
            return true;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate && nextResult;
        }


    }

    private void registerRecord(String recordName, Map<String, YodaType> recordFields) {
        YodaType.RecordType newRecord = new YodaType.RecordType(recordName, recordFields);
        this.yodaTypes.put(recordName, newRecord);
        for (String name: recordFields.keySet()) {
            this.declaredFields.put(name, newRecord);
        }
    }

    private boolean checkUniquenessOfName(String name, ParserRuleContext ctx) {
        if (this.declarations.containsKey(name)) {
            this.errors.record(ParseUtil.duplicatedElementDeclaration(name, this.declarations.get(name), ctx));
            return false;
        } else {
            this.declarations.put(name, ctx);
            return true;
        }
    }

    private YodaType getYodaType(YodaModelParser.TypeContext type) {
        if (yodaTypes.containsKey(type.getText())) {
            return yodaTypes.get(type.getText());
        } else {
            errors.record(ParseUtil.unknownTypeName(type));
            return YodaType.NONE_TYPE;
        }
    }

    private Optional<YodaType> getTypeOfConstantsAndParameters(String name) {
        if (constantsAndParameters.containsKey(name)) {
            return Optional.of(constantsAndParameters.get(name));
        } else {
            return Optional.empty();
        }
    }


    private class AgentAttributesCollector extends YodaModelBaseVisitor<Boolean> {


        @Override
        public Boolean visitModel(YodaModelParser.ModelContext ctx) {
            return ctx.element().stream().allMatch(e -> e.accept(this));
        }

        @Override
        public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
            if (checkUniquenessOfName(ctx.agentName.getText(), ctx)) {
                Map<String, YodaModelParser.NameDeclarationContext> declaredAttributes = new HashMap<>();
                boolean flag = true;
                Map<String, YodaType> agentAttributes = getAttributes(declaredAttributes, elementAttributeTable::isValidAgentAttribute, ctx.agentStateAttributes );
                Map<String, YodaType> agentEnvironmentalAttributes = getAttributes(declaredAttributes, elementAttributeTable::isValidEnvironmentalAttribute, ctx.agentFeaturesAttributes);
                Map<String, YodaType> observations = getAttributes(declaredAttributes, elementAttributeTable::isValidObservationsAttribute, ctx.agentObservationsAttributes);
                elementAttributeTable.recordAgentAttributes(ctx.agentName.getText(), agentEnvironmentalAttributes, agentAttributes, observations);
                return flag;
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitSceneElementDeclaration(YodaModelParser.SceneElementDeclarationContext ctx) {
            if (checkUniquenessOfName(ctx.agentName.getText(), ctx)) {
                Map<String, YodaModelParser.NameDeclarationContext> declaredAttributes = new HashMap<>();
                boolean flag = true;
                Map<String, YodaType> agentEnvironmentalAttributes = getAttributes(declaredAttributes, elementAttributeTable::isValidEnvironmentalAttribute, ctx.elementFeaturesAttributes);
                elementAttributeTable.recordAgentAttributes(ctx.agentName.getText(), agentEnvironmentalAttributes, Map.of(), Map.of());
                return flag;
            } else {
                return false;
            }
        }

        private Map<String, YodaType> getAttributes(Map<String, YodaModelParser.NameDeclarationContext> declaredAttributes, Predicate<String> attribueValidatorPredicate, List<YodaModelParser.NameDeclarationContext> fields) {
            return fields.stream().collect(Collectors.toMap(f -> f.name.getText(), fd -> checkFieldAndReturnItsType(declaredAttributes, attribueValidatorPredicate, fd)));
        }

        private YodaType checkFieldAndReturnItsType(Map<String, YodaModelParser.NameDeclarationContext> declaredAttributes, Predicate<String> attribueValidatorPredicate, YodaModelParser.NameDeclarationContext nameDeclarationContext) {
            if (!attribueValidatorPredicate.test(nameDeclarationContext.name.getText())) {
                errors.record(ParseUtil.illegalSymbolError(nameDeclarationContext.name.getText(), nameDeclarationContext));
                return YodaType.NONE_TYPE;
            }
            if (declaredAttributes.containsKey(nameDeclarationContext.name.getText())) {
                errors.record(ParseUtil.duplicatedAttributeDeclarationError(nameDeclarationContext.name.getText(), declaredAttributes.get(nameDeclarationContext.name.getText()), nameDeclarationContext));
            } else {
                declaredAttributes.put(nameDeclarationContext.name.getText(), nameDeclarationContext);
                YodaType thisType = getYodaType(nameDeclarationContext.type());
                if (YodaType.areCompatible(thisType, elementAttributeTable.getTypeOf(nameDeclarationContext.name.getText()))) {
                    thisType = YodaType.merge(thisType, elementAttributeTable.getTypeOf(nameDeclarationContext.name.getText()));
                }
                if (new TypeInferenceVisitor(errors, YodaModelValidator.this::getTypeOfConstantsAndParameters, elementAttributeTable, declaredFields).checkType(thisType, nameDeclarationContext.value)) {
                    return thisType;
                }
            }
            return YodaType.NONE_TYPE;
        }

        @Override
        protected Boolean defaultResult() {
            return true;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate && nextResult;
        }
    }

    private class ModelElementValidator extends YodaModelBaseVisitor<Boolean> {

        private boolean systemDeclared = false;

        @Override
        protected Boolean defaultResult() {
            return true;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate&&nextResult;
        }

        @Override
        public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
            if (checkUniquenessOfName(ctx.name.getText(), ctx)) {
                YodaType type = ctx.value.accept(new TypeInferenceVisitor(errors, YodaModelValidator.this::getTypeOfConstantsAndParameters, elementAttributeTable, declaredFields));
                if (type != YodaType.NONE_TYPE) {
                    constantsAndParameters.put(ctx.name.getText(), type);
                    return true;
                }
            }
            return false;
        }



        @Override
        public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
            if (checkUniquenessOfName(ctx.name.getText(), ctx)) {
                YodaType type = ctx.value.accept(new TypeInferenceVisitor(errors, YodaModelValidator.this::getTypeOfConstantsAndParameters, elementAttributeTable, declaredFields));
                if (type != YodaType.NONE_TYPE) {
                    constantsAndParameters.put(ctx.name.getText(), type);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
            return checkAgentActions(ctx.agentName.getText(), ctx.actionBody())&&checkAgentBehaviour(ctx.agentName.getText(), ctx.behaviourDeclaration());
        }

        private Boolean checkAgentActions(String agentName, List<YodaModelParser.ActionBodyContext> actionBodyContexts) {
            Set<String> agentActions = new HashSet<>();
            boolean flag = true;
            for (YodaModelParser.ActionBodyContext action: actionBodyContexts) {
                if (agentActions.contains(action.actionName.getText())) {
                    errors.record(ParseUtil.duplicatedActionName(agentName, action.actionName));
                    flag = false;
                } else {
                    agentActions.add(action.actionName.getText());
                    flag &= action.updates.stream().allMatch(au -> checkAttributeUpdate(agentName, s -> elementAttributeTable.isAgentAttribute(agentName, s), elementAttributeTable.getAccessibleAttributeOf(agentName), elementAttributeTable.getAccessibleAttributeOf(agentName),  au,false));
                }
            }
            agentsActions.put(agentName, agentActions);
            return flag;
        }

        private boolean checkAttributeUpdate(String agentName, Predicate<String> canBeUpdatedPredicate, Predicate<String> canBeReadPredicate, Predicate<String> canBeReadWithItPredicate, YodaModelParser.NameUpdateContext fieldUpdateContext, boolean groupExpressionsAllowed) {
            if (canBeUpdatedPredicate.test(fieldUpdateContext.fieldName.getText())) {
                YodaType fieldType = elementAttributeTable.getTypeOf(fieldUpdateContext.fieldName.getText());
                if (fieldType != null) {
                    return new TypeInferenceVisitor(errors, YodaModelValidator.this::getTypeOfConstantsAndParameters, elementAttributeTable, declaredFields, canBeReadPredicate, canBeReadWithItPredicate, true, groupExpressionsAllowed).checkType(fieldType, fieldUpdateContext.value);
                } else {
                    errors.record(ParseUtil.unknownAttributeName(fieldUpdateContext.fieldName));
                    return false;
                }
            } else {
                errors.record(ParseUtil.illegalUpdateOfAttribute(fieldUpdateContext.fieldName));
                return false;
            }
        }


        @Override
        public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
            if (systemDeclared) {
                errors.record(ParseUtil.duplicatedSystemDeclaration(ctx));
                return false;
            }
            systemDeclared = true;
            return ctx.agentSensing.stream().allMatch(this::checkAgentSensing)&
                    ctx.agentDynamics.stream().allMatch(this::checkAgentDynamic);
        }

        private boolean checkAgentSensing(YodaModelParser.AgentAttributesUpdateContext agentSensingContext) {
            String agentName = agentSensingContext.agentName.getText();
            return agentSensingContext.updates.stream().allMatch(fu -> checkAttributeUpdate(agentName, s -> elementAttributeTable.isAgentObservation(agentName, s), elementAttributeTable.getSensingAttributeOf(agentName), elementAttributeTable.getSensingAttributeOf(agentName), fu, true));
        }

        private boolean checkAgentDynamic(YodaModelParser.AgentAttributesUpdateContext agentSensingContext) {
            String agentName = agentSensingContext.agentName.getText();
            return agentSensingContext.updates.stream().allMatch(fu -> checkAttributeUpdate(agentName, elementAttributeTable.getEnvironmentalAttibutePredicateOf(agentName), elementAttributeTable.getSensingAttributeOf(agentName), elementAttributeTable.getSensingAttributeOf(agentName), fu, true));
        }

        @Override
        public Boolean visitConfigurationDeclaration(YodaModelParser.ConfigurationDeclarationContext ctx) {
            return super.visitConfigurationDeclaration(ctx);
        }

        @Override
        public Boolean visitGroupDeclaration(YodaModelParser.GroupDeclarationContext ctx) {
            boolean flag = true;
            if (checkUniquenessOfName(ctx.name.getText(), ctx)) {
                for (Token id: ctx.agents) {
                    if (!elementAttributeTable.isElement(id.getText())) {
                        errors.record(ParseUtil.unknownAgentError(id.getText(), id));
                        flag = false;
                    }
                }
            } else {
                flag = false;
            }
            return flag;
        }
    }



    private boolean checkAgentBehaviour(String agentName, YodaModelParser.BehaviourDeclarationContext behaviourDeclarationContext) {
        boolean flag = true;
        TypeInferenceVisitor typeInferenceVisitor = new TypeInferenceVisitor(errors, this::getTypeOfConstantsAndParameters, elementAttributeTable, declaredFields, elementAttributeTable.getAccessibleAttributeOf(agentName), elementAttributeTable.getAccessibleAttributeOf(agentName), false, false);
        for (YodaModelParser.RuleCaseContext rc: behaviourDeclarationContext.cases) {
            flag &= typeInferenceVisitor.checkType(YodaType.BOOLEAN_TYPE, rc.guard)&rc.actions.stream().allMatch(wa -> checkWeightedAction(typeInferenceVisitor, agentName, wa));
        }
        return flag & behaviourDeclarationContext.defaultcase.stream().allMatch(wa -> checkWeightedAction(typeInferenceVisitor, agentName, wa));
    }

    private boolean checkWeightedAction(TypeInferenceVisitor typeInferenceVisitor, String agentName, YodaModelParser.WeightedActionContext wa) {
        boolean flag = true;
        if (!agentsActions.getOrDefault(agentName, Set.of()).contains(wa.actionName.getText())) {
            flag = false;
            errors.record(ParseUtil.unknownActionError(wa.actionName.getText(), wa.actionName));
        }
        return flag & typeInferenceVisitor.checkType(YodaType.REAL_TYPE, wa.weight);
    }
}
