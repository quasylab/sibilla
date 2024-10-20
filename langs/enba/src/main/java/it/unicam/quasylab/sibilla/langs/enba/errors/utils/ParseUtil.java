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

package it.unicam.quasylab.sibilla.langs.enba.errors.utils;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Type;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class ParseUtil {

    public static final String SYNTAX_ERROR = "Syntax error at line %d (offset %d): %s";
    public static final String DUPLICATED_SYMBOL_ERROR_MESSAGE = "Symbol %s at line %d char %d is already defined at line %d char %d!";
    public static final String DUPLICATED_ACTION = "Action at line %d character %d already used in the same process at line %d character %d";
    public static final String INCOMPATIBLE_CHANNEL= "Process %s is not compatible with channel %s";
    public static final String UNDEFINED_CHANNEL= "Channel %s is not defined at line %d character %d";
    public static final String DUPLICATED_LOCAL_VARIABLE_MESSAGE = "Duplicated Local variable %s at line %d char %d!";
    public static final String UNDEFINED_SYMBOL_ERROR_MESSAGE = "Unknown symbol %s at line %d char %d!";
    public static final String INCORRECT_AGENT_EXPRESSION= "Incorrect agent expression at line %d char %d!";
    public static final String EXPRESSION_TYPE_ERROR_MESSAGE = "Type error at line %d char %d: expected %s is %s!";
    public static final String EXPECTED_NUMBER_MESSAGE = "Expression at line %d char %d: expected int or real is %s!";
    public static final String ILLEGAL_USE_OF_AGENT_IDENTIFIER_MESSAGE = "Illegal use of agent identifier %s at line %d char %d!";
    public static final String EXPECTED_BOOLEAN_MESSAGE = "Expression at line %d char %d: expected a boolean is %s!";
    public static final String UNEXPECTED_TYPE = "Expression at line %d char %d: unexpected Type!";
    public static final String ILLEGAL_POPULATION_EXPRESSION_MESSAGE = "Population expression is not allowed at line %d char %d!";
    public static final String ILLEGAL_USE_OF_NAME_MESSAGE = "Name %s cannot be used at line %d char %d!";
    public static final String ILLEGAL_TIME_EXPRESSION_MESSAGE = "Time dependent expression is not allowed at line %d char %d!";
    public static final String UNDEFINED_AGENT_ERROR_MESSAGE = "Unknown agent %s at line %d char %d!";
    public static final String WRONG_NUMBER_OF_AGENT_PARAMETERS_MESSAGE = "Wrong number of agent parameters a line %d char %d (expected %d are %d)!";
    public static final String NOW = "now";
    private static final String SYMBOL_PATTERN = "__%s__";
    private static final String POPULATION_STATE_IDENTIFIER = "_state_";
    private static final String POPULATION_FRACTION_EXPRESSION = POPULATION_STATE_IDENTIFIER+".getFraction(%s)";
    private static final String SPECIES_FORMAT = "_SPECIES_%s_%s";
    private static final String POPULATION_SIZE_EXPRESSION = POPULATION_STATE_IDENTIFIER+".getOccupancy(%s)";
    private static final String STATE_NAME = "_state_";
    private static final String MEASURE_PATTER = "computeMeasure_%s";
    private static final String ILLEGAL_INTERVAL_MESSAGE = "Illegal interval %d>=%d at line %d char %d!";
    private static final String ILLEGAL_USE_OF_SPECIES_TEMPLATE_MESSAGE = "Species template are not allowed at line %d char %d!";
    private static final String WRONG_NUMBER_OF_LABEL_PARAMETERS_MESSAGE = "Wrong number of agent parameters a line %d char %d (expected %d are %d)!";


    public static String getDuplicatedSymbolErrorMessage(String name, ParserRuleContext existing, ParserRuleContext duplicated) {
        return String.format(DUPLICATED_SYMBOL_ERROR_MESSAGE,
                name,
                duplicated.start.getLine(),
                duplicated.start.getCharPositionInLine(),
                existing.start.getLine(),
                existing.start.getCharPositionInLine()
        );
    }
    public static String getIncorrectAgentExpression(String name, int line, int charPosition) {
        return String.format(INCORRECT_AGENT_EXPRESSION,line,charPosition);
    }

    public static String getDuplicatedAction(ParserRuleContext action, ParserRuleContext repeated) {
        return String.format(DUPLICATED_ACTION, action.start.getLine(), action.start.getCharPositionInLine(), repeated.start.getLine(), repeated.start.getCharPositionInLine());
    }

    public static String getUndefinedChannel(String channel, ParserRuleContext ctx) {
        return String.format(UNDEFINED_CHANNEL, channel, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    public static String getIncompatibleChannel(String species, String channel) {
        return String.format(INCOMPATIBLE_CHANNEL, species, channel);
    }

    public static String getUndefinedSymbolMessage(String name, int line, int charPosition) {
        return String.format(UNDEFINED_SYMBOL_ERROR_MESSAGE,name,line,charPosition);
    }

    public static String getDuplicatedLocalVariableMessage(String name, int line, int charPosition) {
        return String.format(DUPLICATED_LOCAL_VARIABLE_MESSAGE,name,line,charPosition);
    }

    public static String getTypeErrorMessage(Type expected, Type actual, ParserRuleContext ctx) {
        return String.format(EXPRESSION_TYPE_ERROR_MESSAGE,ctx.start.getLine(),ctx.start.getCharPositionInLine(),expected.toString(),actual.toString());
    }

    public static String getExpectedNumberMessage(Type t, ParserRuleContext ctx) {
        return String.format(EXPECTED_NUMBER_MESSAGE,ctx.start.getLine(),ctx.start.getCharPositionInLine(),t.toString());
    }

    public static String getUnexpectedType(ParserRuleContext ctx) {
        return String.format(UNEXPECTED_TYPE,ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }

    public static String getIllegalUseOfAgentIdentifierMessage(Token reference) {
        return String.format(ILLEGAL_USE_OF_AGENT_IDENTIFIER_MESSAGE,reference.getText(),reference.getLine(),reference.getCharPositionInLine());
    }

    public static String getExpectedBooleanMessage(Type t, ParserRuleContext ctx) {
        return String.format(EXPECTED_BOOLEAN_MESSAGE,ctx.start.getLine(),ctx.start.getCharPositionInLine(),t.toString());
    }

    public static String getIllegalPopulationExpression(ParserRuleContext ctx) {
        return String.format(ILLEGAL_POPULATION_EXPRESSION_MESSAGE,ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }

    public static String getIllegalUseOfNow(int line, int charPositionInLine) {
        return String.format(ILLEGAL_TIME_EXPRESSION_MESSAGE,line,charPositionInLine);
    }

    public static String getUndefinedAgentMessage(Token token) {
        return String.format(UNDEFINED_AGENT_ERROR_MESSAGE,token.getText(),token.getLine(),token.getCharPositionInLine());
    }

    public static String getSymbolName(String id) {
        return String.format(SYMBOL_PATTERN,id);
    }

    public static String populationFractionExpression(String name, String[] args) {
        return String.format(POPULATION_FRACTION_EXPRESSION,getSpeciesIndexExpression(name,args));
    }

    private static String getSpeciesIndexExpression(String name, String[] args) {
        return String.format(SPECIES_FORMAT,name,String.join("_",args));
    }

    public static String populationSizeExpression(String name, String[] args) {
        return String.format(POPULATION_SIZE_EXPRESSION,getSpeciesIndexExpression(name,args));
    }

    public static String getStateName() {
        return STATE_NAME;
    }

    public static String getMeasureName(String measure) {
        return String.format(MEASURE_PATTER,measure);
    }

    public static String syntaxErrorMessage(ParseError e) {
        return String.format(SYNTAX_ERROR,e.getLine(),e.getOffset(),e.getMessage());
    }

    public static String illegalUseOfName(String name, ExtendedNBAParser.ReferenceExpressionContext ctx) {
        return String.format(ILLEGAL_USE_OF_NAME_MESSAGE,name,ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }

    public static String illegalUseOfName(String name, ExtendedNBAParser.SenderReferenceExpressionContext ctx) {
        return String.format(ILLEGAL_USE_OF_NAME_MESSAGE,name,ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }
}
