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
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TraceSpecificationEvaluator extends TracingSpecificationBaseVisitor<Boolean> {

    private final TracingFunction function = new TracingFunction();

    public TracingFunction getFunction() {
        return function;
    }

    @Override
    public Boolean visitTracing(TracingSpecificationParser.TracingContext ctx) {
        boolean flag = true;
        for (TracingSpecificationParser.FieldAssignmentsContext assignment : ctx.assignments) {
            flag &= assignment.accept(this);
        }
        return flag;
    }

    @Override
    public Boolean visitSimpleFieldAssignment(TracingSpecificationParser.SimpleFieldAssignmentContext ctx) {
        String name = ctx.name.getText();
        try {
            TracingConstants.TracingFields field = TracingConstants.TracingFields.valueOf(name.toUpperCase());
            setField(field, ctx.value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Unknown field %s at line %d char %d.", name, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
        }
        return true;
    }

    private void setDoubleField(TracingConstants.TracingFields field, Function<Function<String,SibillaValue>, SibillaValue> f) {
        switch (field) {
            case X:
                this.function.setTracingX(ns -> f.apply(ns).doubleOf());
                return ;
            case Y:
                this.function.setTracingY(ns -> f.apply(ns).doubleOf());
                return ;
            case Z:
                this.function.setTracingZ(ns -> f.apply(ns).doubleOf());
                return ;
            case DIRECTION:
                this.function.setTracingDirection(ns -> f.apply(ns).doubleOf());
        }
    }

    private void setStringField(TracingConstants.TracingFields field, Function<Function<String,SibillaValue>, String> f) {
        switch (field) {
            case COLOUR:
                this.function.setTracingColour(f);
                return ;
            case SHAPE:
                this.function.setTracingShape(f);
        }
    }

    private void setField(TracingConstants.TracingFields field, ParserRuleContext value) {
        switch (field) {
            case X:
            case Y:
            case Z:
            case DIRECTION:
                setDoubleField(field, value.accept(new TraceExpressionEvaluator()));
                return ;
            case SHAPE:
            case COLOUR:
                setStringField(field, value.accept(new StringAttributeEvaluator()));
        }
    }


    @Override
    public Boolean visitBlockFieldAssignment(TracingSpecificationParser.BlockFieldAssignmentContext ctx) {
        String name = ctx.name.getText();
        try {
            TracingConstants.TracingFields field = TracingConstants.TracingFields.valueOf(name.toUpperCase());
            setField(field, ctx.whenBlock());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Unknown field %s at line %d char %d.", name, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
        }
        return true;
    }


    public static TracingFunction load(String source) {
        return load(CharStreams.fromString(source));
    }

    public static TracingFunction load(File file) throws IOException {
        return load(CharStreams.fromReader(new FileReader(file)));
    }

    public static TracingFunction load(CodePointCharStream source) {
        TraceSpecificationEvaluator evaluator = new TraceSpecificationEvaluator();
        TracingSpecificationParser.TracingContext tracingContext = getParseTree(source);
        tracingContext.accept(evaluator);
        return evaluator.getFunction();
    }


    private static TracingSpecificationParser.TracingContext getParseTree(CodePointCharStream source) {
        TracingSpecificationLexer lexer = new TracingSpecificationLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TracingSpecificationParser parser = new TracingSpecificationParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        if (errorListener.getErrorCollector().withErrors()) {
            throw new RuntimeException(errorListener.getErrorCollector().getSyntaxErrorList().stream().map(ParseError::toString).collect(Collectors.joining()));
        }
        return parser.tracing();
    }
}
