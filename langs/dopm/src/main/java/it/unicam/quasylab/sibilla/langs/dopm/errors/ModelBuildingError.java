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

package it.unicam.quasylab.sibilla.langs.dopm.errors;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.utils.ParseUtil;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.Type;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class ModelBuildingError {

    private final String message;

    public ModelBuildingError(String message) {
        this.message = message;
    }

    public static ModelBuildingError unknownSymbol(String name, int line, int charPositionInLine) {
        return new ModelBuildingError(ParseUtil.getUndefinedSymbolMessage(name,line,charPositionInLine));
    }

    public static ModelBuildingError incorrectAgentExpression(String name, int line, int charPositionInLine) {
        return new ModelBuildingError(ParseUtil.getIncorrectAgentExpression(name,line,charPositionInLine));
    }

    public static ModelBuildingError unexpectedType(Type e, ParserRuleContext ctx) {
        return new ModelBuildingError(ParseUtil.getUnexpectedType(e,ctx));
    }

    public static ModelBuildingError typeError(Type expected, Type actual, ParserRuleContext ctx) {
        return new ModelBuildingError(ParseUtil.getTypeErrorMessage(expected,actual,ctx));
    }

    public static ModelBuildingError expectedNumber(Type t, ParserRuleContext ctx) {
        return new ModelBuildingError(ParseUtil.getExpectedNumberMessage(t,ctx));
    }

    public static ModelBuildingError illegalUseOfAgentIdentifier(Token reference) {
        return new ModelBuildingError(ParseUtil.getIllegalUseOfAgentIdentifierMessage(reference));
    }

    public static ModelBuildingError expectedBoolean(Type t, ParserRuleContext ctx) {
        return new ModelBuildingError(ParseUtil.getExpectedBooleanMessage(t,ctx));
    }

    public static ModelBuildingError illegalPopulationExpression(ParserRuleContext ctx) {
        return new ModelBuildingError(ParseUtil.getIllegalPopulationExpression(ctx));
    }

    public static ModelBuildingError illegalUseOfNow(int line, int charPositionInLine) {
        return new ModelBuildingError(ParseUtil.getIllegalUseOfNow(line,charPositionInLine));
    }

    public static ModelBuildingError unknownAgent(Token token) {
        return new ModelBuildingError(ParseUtil.getUndefinedAgentMessage(token));
    }

    public static ModelBuildingError syntaxError(ParseError e) {
        return new ModelBuildingError(ParseUtil.syntaxErrorMessage(e));
    }

    public static ModelBuildingError illegalUseOfName(String name, DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        return new ModelBuildingError(ParseUtil.illegalUseOfName(name, ctx));
    }

    public static ModelBuildingError illegalUseOfName(String name, DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        return new ModelBuildingError(ParseUtil.illegalUseOfName(name, ctx));
    }

    public static ModelBuildingError duplicatedName(String name, ParserRuleContext existing, ParserRuleContext duplicated) {
        return new ModelBuildingError(ParseUtil.getDuplicatedSymbolErrorMessage(name,existing,duplicated));
    }

    public static ModelBuildingError duplicatedLocalVariable(String name, int line, int charPositionInLine) {
        return new ModelBuildingError(ParseUtil.getDuplicatedLocalVariableMessage(name,line,charPositionInLine));
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }

    @Override
    public String toString() {
        return message;
    }
}
