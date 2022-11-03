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

import java.util.HashMap;
import java.util.Map;

public class ParameterEvaluator extends YodaModelBaseVisitor<Map<String,Double>> {

    private final Map<String, Double> params;


    public ParameterEvaluator() {
        this.params = new HashMap<>();
    }

    @Override
    public Map<String,Double> visitModel(YodaModelParser.ModelContext ctx){
        for (YodaModelParser.ElementContext element : ctx.element()) {
            element.accept(this);
        }
        return this.params;
    }

    @Override
    public Map<String, Double> visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
        return this.params;
    }

    @Override
    public Map<String, Double> visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
        this.params.put(ctx.name.getText(), Double.parseDouble(ctx.value.getText()));
        return this.params;
    }
}
