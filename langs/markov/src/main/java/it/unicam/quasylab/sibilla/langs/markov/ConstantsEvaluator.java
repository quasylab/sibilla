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

import it.unicam.quasylab.sibilla.core.models.CachedValues;

import java.util.function.Function;

public class ConstantsEvaluator extends MarkovChainModelBaseVisitor<CachedValues> {

    private final CachedValues constants;
    private final Function<String, DataType> types;

    public ConstantsEvaluator(Function<String, DataType> types) {
        this.constants = new CachedValues();
        this.types = types;
    }

    @Override
    public CachedValues visitModel(MarkovChainModelParser.ModelContext ctx) {
        for (MarkovChainModelParser.ElementContext e: ctx.element()) {
            e.accept(this);
        }
        return constants;
    }

    @Override
    public CachedValues visitConst_declaration(MarkovChainModelParser.Const_declarationContext ctx) {
        DataType cType = types.apply(ctx.name.getText());
        if (DataType.INTEGER == cType) {
            constants.register(ctx.name.getText(), f -> (double) ExpressionEvaluator.evalInteger(types, f, ctx.expr()));
        }
        if (DataType.REAL == cType) {
            constants.register(ctx.name.getText(), f -> ExpressionEvaluator.evalDouble(types, f, ctx.expr()));
        }
        return constants;
    }

    @Override
    public CachedValues visitParam_declaration(MarkovChainModelParser.Param_declarationContext ctx) {
        return this.constants;
    }
}
