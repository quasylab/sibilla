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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;

class LIOModelDataCollector extends LIOModelBaseVisitor<Boolean> {

    private final EvaluationEnvironment environment;

    private LIOModelDataCollector(EvaluationEnvironment environment) {
        this.environment = environment;
    }


    @Override
    public Boolean visitElementParam(LIOModelParser.ElementParamContext ctx) {
        return super.visitElementParam(ctx);
    }

    @Override
    public Boolean visitElementConstant(LIOModelParser.ElementConstantContext ctx) {
        return super.visitElementConstant(ctx);
    }

    @Override
    public Boolean visitElementAction(LIOModelParser.ElementActionContext ctx) {
        return super.visitElementAction(ctx);
    }

    @Override
    public Boolean visitElementState(LIOModelParser.ElementStateContext ctx) {
        return super.visitElementState(ctx);
    }

    @Override
    public Boolean visitElementMeasure(LIOModelParser.ElementMeasureContext ctx) {
        return super.visitElementMeasure(ctx);
    }

    @Override
    public Boolean visitElementAtomic(LIOModelParser.ElementAtomicContext ctx) {
        return super.visitElementAtomic(ctx);
    }

    @Override
    public Boolean visitElementPredicate(LIOModelParser.ElementPredicateContext ctx) {
        return super.visitElementPredicate(ctx);
    }

    @Override
    public Boolean visitElementSystem(LIOModelParser.ElementSystemContext ctx) {
        return super.visitElementSystem(ctx);
    }
}
