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

import java.util.Locale;
import java.util.stream.Stream;

public class TracingConstants {

    public static final String DEFAULT_COLOUR = Colour.BLACK.name().toLowerCase(Locale.ROOT);
    public static final String DEFAULT_SHAPE = Shape.CIRCLE.name().toLowerCase();

    public static boolean isShape(String name) {
        String upCase = name.toUpperCase();
        return Stream.of(Shape.values()).map(Enum::name).anyMatch(s -> s.equals(upCase));
    }

    public static boolean isColour(String name) {
        String upCase = name.toUpperCase();
        return Stream.of(Colour.values()).map(Enum::name).anyMatch(s -> s.equals(upCase));
    }

    public enum TracingFields {
        X,
        Y,
        Z,
        DIRECTION,
        COLOUR,
        SHAPE;
    }

    public enum Colour {
        BLUE,
        RED,
        GREEN,
        YELLOW,
        BLACK,
        CYAN,
        GRAY,
        MAGENTA,
        WHITE;
    }

    public enum Shape {
        CIRCLE,
        SQUARE,
        TRIANGLE,
        HEXAGON,
        CAR,
        ANT,
        BIRD;
    }
}
