/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.tracing;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class StringAttributeEvaluator extends TracingSpecificationBaseVisitor<Function<Function<String, SibillaValue>,String>> {

    @Override
    public Function<Function<String, SibillaValue>, String> visitWhenBlock(TracingSpecificationParser.WhenBlockContext ctx) {
        TraceExpressionEvaluator evaluator = new TraceExpressionEvaluator();
        List<Function<Function<String, SibillaValue>, String>> values = ctx.values.stream().map(e -> e.accept(this)).toList();
        List<Function<Function<String, SibillaValue>, SibillaValue>> guards = ctx.guards.stream().map(g -> g.accept(evaluator)).toList();
        Function<Function<String, SibillaValue>, String> defaultValue =  ctx.default_.accept(this);
        return f -> {
            Iterator<Function<Function<String, SibillaValue>, String>> valuesIterator = values.iterator();
            for (Function<Function<String, SibillaValue>, SibillaValue> guard : guards) {
                Function<Function<String, SibillaValue>, String> value = valuesIterator.next();
                if (guard.apply(f).booleanOf()) {
                    return value.apply(f);
                }
            }
            return defaultValue.apply(f);
        };    }

    @Override
    public Function<Function<String, SibillaValue>, String> visitExpressionReference(TracingSpecificationParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        if (TracingConstants.isColour(name)||TracingConstants.isShape(name)) {
            return f -> name;
        }
        throw new RuntimeException(String.format("Illegal value at line %d char %d", ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
    }

    @Override
    protected Function<Function<String, SibillaValue>, String> defaultResult() {
        return null;
    }
}
