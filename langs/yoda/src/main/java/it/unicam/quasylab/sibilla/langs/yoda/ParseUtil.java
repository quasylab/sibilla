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
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;


public class ParseUtil {
    private static final String DUPLICATED_ID_ERROR = "Identifier %s has been already used at line %d:%d.";
    private static final String DUPLICATED_ENTITY = "Entity %s has been already used";
    private static final String DUPLICATED_FIELD = "Field %s has been already used";
    private static final String DUPLICATED_ACTION = "Action %s has been already used";
    private static final String EXPECTED_NUMBER_ERROR = "Expected numeric type while is %s";
    private static final String UNKNOWN_ACTION_ERROR = "Action %s can not be resolved";
    private static final String UNKNOWN_AGENT_ERROR = "Agent or System %s can not be resolved";
    private static final String UNKNOWN_SYMBOL_ERROR = "Symbol %s can not be resolved";
    private static final String UNKNOWN_VARIABLE_ERROR = "Variable %s can not be resolved";
    private static final String WRONG_TYPE_ERROR = "Wrong type! Expected %s actual is %s";
    private static final String ILLEGAL_TYPE_ERROR_ARITHMETIC = "Illegal type! Type %s can not be used in arithmetic expression";
    private static final String UNKNOWN_ENTITY_ERROR = "Agent or Variable %s can not be resolved";

    public static ParseError duplicatedIdentifierError(String name, Token original){
        return new ParseError(
                String.format(DUPLICATED_ID_ERROR, name, original.getLine(), original.getCharPositionInLine()),
                original.getLine(),
                original.getCharPositionInLine());
    }

    public static ParseError duplicatedEntityError(String entityName, Token original) {
        return new ParseError(
                String.format(DUPLICATED_ENTITY,entityName),
                original.getLine(),
                original.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedFieldError(String fieldName, Token original) {
        return new ParseError(
                String.format(DUPLICATED_FIELD, fieldName),
                original.getLine(),
                original.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedActionName(String actionName, Token original) {
        return new ParseError(
                String.format(DUPLICATED_ACTION, actionName),
                original.getLine(),
                original.getCharPositionInLine()
        );
    }

    public static ParseError expectedNumberError(YodaType type, YodaModelParser.ExprContext exprContext) {
        return new ParseError(
                String.format(EXPECTED_NUMBER_ERROR, type),
                exprContext.start.getLine(),
                exprContext.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownActionError(String name, Token actionName){
        return new ParseError(
                String.format(UNKNOWN_ACTION_ERROR, name),
                actionName.getLine(),
                actionName.getCharPositionInLine()
        );
    }

    public static ParseError unknownAgentError(String name, Token agentName){
        return new ParseError(
                String.format(UNKNOWN_AGENT_ERROR, name),
                agentName.getLine(),
                agentName.getCharPositionInLine()
        );
    }

    public static ParseError unknownSymbolError(String name, ParserRuleContext ctx){
        return new ParseError(
                String.format(UNKNOWN_SYMBOL_ERROR, name),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownVariableError(String name, Token variableName){
        return new ParseError(
                String.format(UNKNOWN_VARIABLE_ERROR, name),
                variableName.getLine(),
                variableName.getCharPositionInLine()
        );
    }

    public static ParseError wrongTypeError(YodaType expected, YodaType actual, YodaModelParser.ExprContext argument){
        return new ParseError(
                String.format(WRONG_TYPE_ERROR, expected, actual),
                argument.start.getLine(),
                argument.start.getCharPositionInLine()
        );
    }

    public static ParseError illegalTypeError(YodaType actual, YodaModelParser.ExprContext argument) {
        return new ParseError(
                String.format(ILLEGAL_TYPE_ERROR_ARITHMETIC, actual),
                argument.start.getLine(),
                argument.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownEntityError(String name, Token variableName){
        return new ParseError(
                String.format(UNKNOWN_ENTITY_ERROR, name),
                variableName.getLine(),
                variableName.getCharPositionInLine()
        );
    }

    public static String localVariableName(String agentName, String variableName) {
        return agentName+"@"+variableName;
    }


}
