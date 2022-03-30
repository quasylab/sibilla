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

import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class ParseUtil {

    private static final String DUPLICATED_NAME_ERROR = "Name %s has been already declared at line %d:%d.";
    private static final String TYPE_ERROR_MESSAGE = "Type error! Expected %s is %s.";
    private static final String UNKNOWN_SYMBOL_MESSAGE = "Name %s is unknown!";
    private static final String EXPECTED_NUMBER_MESSAGE = "Expected a numeric value while it is %s!";
    private static final String UNKNOWN_STATE_VARIABLE_MESSAGE = "Variable %s is not a state variable!";
    private static final String DUPLICATED_STATE_VARIABLE_ASSIGNMENT = "Variable %s has been already assigned!";
    private static final String DEFAULT_INITIAL_STATE_ALREADY_DEFINED = "Default initial state already declared at line %d:%d.";
    private static final String DUPLICATED_INITIAL_STATE = "State %s has been already declared at line %d:%d";

    public static ParseError duplicatedSymbolError(String name, Token duplicated, Token previous) {
        return new ParseError(
                String.format(DUPLICATED_NAME_ERROR,name,previous.getLine(),previous.getCharPositionInLine()),
                duplicated.getLine(),
                duplicated.getCharPositionInLine());
    }


    public static ParseError wrongTypeError(DataType expected, DataType actual, MarkovChainModelParser.ExprContext arg) {
        return new ParseError(
                String.format(TYPE_ERROR_MESSAGE,expected,actual),
                arg.start.getLine(),
                arg.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownSymbol(String name, ParserRuleContext ctx) {
        return new ParseError(
                String.format(UNKNOWN_SYMBOL_MESSAGE,name),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError expectedNumberError(DataType t, MarkovChainModelParser.ExprContext expr) {
        return new ParseError(
                String.format(EXPECTED_NUMBER_MESSAGE, t),
                expr.start.getLine(),
                expr.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownUpdateVariable(String name, Token target) {
        return new ParseError(
                String.format(UNKNOWN_STATE_VARIABLE_MESSAGE, name),
                target.getLine(),
                target.getCharPositionInLine()
        );
    }

    public static ParseError unknownStateVariable(String name, Token token) {
        return new ParseError(
                String.format(UNKNOWN_STATE_VARIABLE_MESSAGE, name),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedStateVariableAssignment(String name, MarkovChainModelParser.Variable_assignmentContext va) {
        return new ParseError(
                String.format(DUPLICATED_STATE_VARIABLE_ASSIGNMENT, name),
                va.start.getLine(),
                va.start.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedDefaultInitialState(Token duplicated, Token current) {
        return new ParseError(
                String.format(DEFAULT_INITIAL_STATE_ALREADY_DEFINED, current.getLine(), current.getCharPositionInLine()),
                duplicated.getLine(),
                duplicated.getCharPositionInLine());
    }

    public static ParseError duplicatedInitialState(String name, MarkovChainModelParser.Init_declarationsContext duplicated, MarkovChainModelParser.Init_declarationsContext existing) {
        return new ParseError(
                String.format(DUPLICATED_INITIAL_STATE,name, existing.start.getLine(), existing.start.getCharPositionInLine()),
                duplicated.start.getLine(),
                duplicated.start.getCharPositionInLine()
        );
    }

    public static String getVariableNameFromNextId(String target) {
        return target.substring(0, target.length()-1);
    }
}
