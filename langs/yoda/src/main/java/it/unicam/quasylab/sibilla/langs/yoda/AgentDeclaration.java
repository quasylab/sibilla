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

import java.util.HashMap;
import java.util.Map;

public class AgentDeclaration {

    private final String name;
    private final YodaModelParser.AgentDeclarationContext agDecCtx;

    private final Map<String, YodaVariable> knowledgeFields = new HashMap<>();
    private final Map<String, YodaVariable> informationFields =  new HashMap<>();
    private final Map<String, YodaVariable> observationFields = new HashMap<>();
    private final Map<String, YodaModelParser.ActionBodyContext> actions = new HashMap<>();

    public AgentDeclaration(String name, YodaModelParser.AgentDeclarationContext agDecCtx) {
        this.name = name;
        this.agDecCtx = agDecCtx;
    }

    public String getName() {
        return name;
    }

    public YodaModelParser.AgentDeclarationContext getAgDecCtx() {
        return agDecCtx;
    }

    public void addKnowledge(String name, YodaVariable variable) {
        this.knowledgeFields.put(name, variable);
    }

    public void addInformation(String name, YodaVariable variable) {
        this.informationFields.put(name, variable);
    }

    public void addObservation(String name, YodaVariable variable) {
        this.observationFields.put(name, variable);
    }

    public void addAction(YodaModelParser.ActionBodyContext ctx) {
        this.actions.put(ctx.actionName.getText(), ctx);
    }

    public boolean existsKnowledge(String name) {
        return this.knowledgeFields.containsKey(name);
    }

    public boolean existsInformation(String name) {
        return this.informationFields.containsKey(name);
    }

    public boolean existsObservation(String name) {
        return this.observationFields.containsKey(name);
    }

    public boolean existsAction(String name) {
        return this.actions.containsKey(name);
    }

    public YodaVariable getVariableFromFields(String name) {
        if (existsKnowledge(name)) {
            return this.knowledgeFields.get(name);
        } else if (existsInformation(name)) {
            return this.informationFields.get(name);
        } else if (existsObservation(name)){
            return this.observationFields.get(name);
        } else {
            return null;
        }
    }

    public YodaType getTypeFromFields(String name) {
        YodaVariable variable = getVariableFromFields(name);
        if (variable == null) {
            return YodaType.NONE_TYPE;
        } else {
            return variable.getType();
        }
    }

}
