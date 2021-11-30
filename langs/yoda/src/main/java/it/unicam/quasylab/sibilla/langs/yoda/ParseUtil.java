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

import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;


public class ParseUtil {
    private static final String DUPLICATED_ID_ERROR = "Identifier %s has been already used at line %d:%d.";
    private static final String WRONG_TYPE_ERROR = "Wrong type! Expected %s actual is %s";
    private static final String UNKNOWN_SYMBOL_ERROR = "Symbol %s can not be resolved";
    private static final String EXPECTED_NUMBER_ERROR = "Expected numeric type while is %s";

    public static ParseError duplicatedIdentifierError(String name, Token duplicated, Token original){
        return new ParseError(
                String.format(DUPLICATED_ID_ERROR, name, original.getLine(), original.getCharPositionInLine()),
                duplicated.getLine(),
                duplicated.getCharPositionInLine());
    }

    public static ParseError wrongTypeError(DataType expected, DataType actual, YodaModelParser.ExprContext argument){
        return new ParseError(
                String.format(WRONG_TYPE_ERROR, expected, actual),
                argument.start.getLine(),
                argument.start.getCharPositionInLine());
    }

    //TODO
    public static ParseError unknownSymbolError(){
        return null;
    }

    public static ParseError expectedNumberError(DataType type, YodaModelParser.ExprContext exprContext) {
        return new ParseError(
                String.format(EXPECTED_NUMBER_ERROR, type),
                exprContext.start.getLine(),
                exprContext.start.getCharPositionInLine()
        );
    }

}
