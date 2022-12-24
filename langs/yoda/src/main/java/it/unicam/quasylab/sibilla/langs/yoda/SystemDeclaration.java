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

public class SystemDeclaration {

    private final String name;
    private final YodaModelParser.SystemDeclarationContext sysDecCtx;

    private final Map<String, YodaVariable> sceneFields = new HashMap<>();
    private final Map<String, YodaModelParser.AssignmentTempContext> temps = new HashMap<>();

    public SystemDeclaration(String name, YodaModelParser.SystemDeclarationContext sysDecCtx) {
        this.name = name;
        this.sysDecCtx = sysDecCtx;
    }

    public String getName() {
        return name;
    }

    public YodaModelParser.SystemDeclarationContext getSysDecCtx() {
        return sysDecCtx;
    }

    public void addSceneField(String name, YodaVariable variable) {
        this.sceneFields.put(name, variable);
    }

    public boolean existsSceneField(String name) {
        return this.sceneFields.containsKey(name);
    }

    public YodaType getTypeSceneField(String name) {
        return this.sceneFields.get(name).getType();
    }

    public void addTemp(YodaModelParser.AssignmentTempContext ctx) {
        this.temps.put(ctx.tempName.getText(), ctx);
    }

    public boolean existsTemp(String name) {
        return this.temps.containsKey(name);
    }
}
