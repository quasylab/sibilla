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

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class TracingFunction {

    private ToDoubleFunction<Function<String, SibillaValue>> tracingX;

    private ToDoubleFunction<Function<String, SibillaValue>> tracingY;

    private ToDoubleFunction<Function<String, SibillaValue>> tracingZ;

    private ToDoubleFunction<Function<String, SibillaValue>> tracingDirection;

    private Function<Function<String, SibillaValue>, String> tracingShape;

    private Function<Function<String, SibillaValue>, String> tracingColour;


    public TracingFunction() {
    }

    public ToDoubleFunction<Function<String, SibillaValue>> getTracingX() {
        return tracingX;
    }

    public ToDoubleFunction<Function<String, SibillaValue>> getTracingY() {
        return tracingY;
    }

    public ToDoubleFunction<Function<String, SibillaValue>> getTracingZ() {
        return tracingZ;
    }

    public ToDoubleFunction<Function<String, SibillaValue>> getTracingDirection() {
        return tracingDirection;
    }

    public Function<Function<String, SibillaValue>, String> getTracingShape() {
        return tracingShape;
    }

    public Function<Function<String, SibillaValue>, String> getTracingColour() {
        return tracingColour;
    }

    public void setTracingX(ToDoubleFunction<Function<String, SibillaValue>> tracingX) {
        this.tracingX = tracingX;
    }

    public void setTracingY(ToDoubleFunction<Function<String, SibillaValue>> tracingY) {
        this.tracingY = tracingY;
    }

    public void setTracingZ(ToDoubleFunction<Function<String, SibillaValue>> tracingZ) {
        this.tracingZ = tracingZ;
    }

    public void setTracingDirection(ToDoubleFunction<Function<String, SibillaValue>> tracingDirection) {
        this.tracingDirection = tracingDirection;
    }

    public void setTracingShape(Function<Function<String, SibillaValue>, String> tracingShape) {
        this.tracingShape = tracingShape;
    }

    public void setTracingColour(Function<Function<String, SibillaValue>, String> tracingColour) {
        this.tracingColour = tracingColour;
    }

    public TracingData apply(Function<String, SibillaValue> nameSolver, double t) {
        return new TracingData(
            t,
            extractDouble(TracingConstants.TracingFields.X.name().toLowerCase(), tracingX, nameSolver),
            extractDouble(TracingConstants.TracingFields.Y.name().toLowerCase(), tracingY, nameSolver),
            extractDouble(TracingConstants.TracingFields.Z.name().toLowerCase(), tracingZ, nameSolver),
            extractDouble(TracingConstants.TracingFields.DIRECTION.name().toLowerCase(), tracingDirection, nameSolver),
            extractString(tracingShape, nameSolver, TracingConstants.DEFAULT_SHAPE),
            extractString(tracingColour, nameSolver, TracingConstants.DEFAULT_COLOUR)

        );
    }

    private String extractString(Function<Function<String, SibillaValue>, String> tracingFunction, Function<String, SibillaValue> nameSolver, String defaultValue) {
        if (tracingFunction == null) {
            return defaultValue;
        }
        return tracingFunction.apply(nameSolver);
    }

    private double extractDouble(String field, ToDoubleFunction<Function<String, SibillaValue>> tracingFunction, Function<String, SibillaValue> nameSolver) {
        if (tracingFunction == null) {
            return nameSolver.apply(field).doubleOf();
        }
        return tracingFunction.applyAsDouble(nameSolver);
    }

    private double extractString(String field, ToDoubleFunction<Function<String, SibillaValue>> tracingFunction, Function<String, SibillaValue> nameSolver) {
        if (tracingFunction == null) {
            return nameSolver.apply(field).doubleOf();
        }
        return tracingFunction.applyAsDouble(nameSolver);
    }

}
