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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;

public class ParseUtil {


    private static final String DUPLICATED_STATE_NAME_MESSAGE = "State name %s at line %d:%d has been already used at line %d:%d.";
    private static final String DUPLICATED_AGENT_NAME_MESSAGE = "Duplicated agent name %s at line %d:%d.";
    private static final String DUPLICATED_NAME_MESSAGE = "Name %s has at line %d:%d has been already declared at line %d:%d.";
    private static final String TYPE_ERROR_MESSAGE = "Type error at line %d:%d: expected %s while it is %s.";
    private static final String UNKNOWN_SYMBOL_MESSAGE = "Symbol %s at line %d:%d is unknown.";
    private static final String INCOMPARABLE_TYPE_MESSAGE = "Type error at line %d:%d: values of type %s cannot be compared!";
    private static final String ILLEGAL_CAST_MESSAGE = "Type error at line %d:%d: values of type %s cannot be cast to type %s!";
    private static final String ILLEGAL_TYPE_IN_ARITHMETIC_EXPRESSION = "Type error at line %d:%d: values of type %s cannot be in arithmetic expressions!";
    private static final String INCOMPATIBLE_TYPE_DECLARATION = "Incompatible type for %s at line %d:%d: expected %s is %s!";
    private static final String UNKNOWN_AGENT_MESSAGE = "Unknown agent %s at line %d:%d";
    private static final String ILLEGAL_NUMBER_OF_PARAMETERS = "Illegal number of parameters for %s at line %d:%d: expected %d are %d!";
    private static final String DUPLICATE_INITIAL_STATE = "Duplicated initial state %s at line %d:%d! Another state %s is declared initial at line %d:%d";
    private static final String UNKNOWN_TAG_MESSAGE = "Message tag %s at line %d:%d is unknown.";
    private static final String ILLEGAL_NUMBER_OF_MESSAGE_ELEMENTS = "Wrong number of message elements for tag %s at line %d:%d: expected %d are %d!";
    private static final String ILLEGAL_AGENT_EXPRESSION = "Illegal agent expression at line %d:%d";
    private static final String ILLEGAL_TIME_EXPRESSION = "Illegal time expression at line %d:%d";
    private static final String ILLEGAL_ATTRIBUTE_DECLARATION = "The use of name %s is not allowed at line %d:%d";
    private static final String ILLEGAL_PARAMETER_DECLARATION = "Parameter %s at line %d:%d has the same name of attribute at line %d:%d!";
    private static final String DUPLICATED_ATTRIBUTE_DECLARATION = "Duplicated attribute %s at line %d:%d has been already defined at line %d:%d";
    private static final String DUPLICATED_PARAMETER_DECLARATION =  "Duplicated parameter %s at line %d:%d has been already defined at line %d:%d";
    private static final String NAME_ALREADY_USED_IN_PATTERN = "Name %s at line %d:%d has been already used in the same message pattern!";
    private static final String UNKNOWN_SYMBOL_STATE = "State %s at line %d:%d is unknown in agent %s!";
    private static final String ILLEGAL_ASSIGNMENT = "Values cannot be assigned to %s at line %d:%d!";
    private static final String ILLEGAL_RANDOM_EXPRESSION = "Illegal use of random expression at line %d:%d!";
    private static final String IT_IS_NOT_ALLOWED_HERE_MESSAGE = "Use of 'it' is not allowed at line %d:%d.";

    public static String duplicatedAgentName(Token agentName) {
        return String.format(DUPLICATED_AGENT_NAME_MESSAGE, agentName.getText(), agentName.getLine(), agentName.getCharPositionInLine());
    }

    public static String duplicatedName(Token newDeclarationToken, Token previousDeclarationToken) {
        return String.format(DUPLICATED_NAME_MESSAGE,newDeclarationToken.getText(),
                newDeclarationToken.getLine(),
                newDeclarationToken.getCharPositionInLine(),
                previousDeclarationToken.getLine(),
                previousDeclarationToken.getCharPositionInLine());
    }


    public static String typeErrorMessage(SlamType expected, SlamType actual, int line, int charPositionInLine) {
        return String.format(TYPE_ERROR_MESSAGE, line, charPositionInLine, expected, actual);
    }


    public static ParseError typeError(SlamType expected, SlamType actual, Token start) {
        return new ParseError(
                typeErrorMessage(expected, actual, start.getLine(), start.getCharPositionInLine()),
                start.getLine(),
                start.getCharPositionInLine()
        );
    }

    public static ParseError unknownSymbolError(Token token) {
        return new ParseError(
                unknownSymbolMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static String unknownSymbolMessage(Token token) {
        return String.format(UNKNOWN_SYMBOL_MESSAGE, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError incomparableTypeError(SlamType type, Token token) {
        return new ParseError(
                incomparableTypeMessage(type, token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String incomparableTypeMessage(SlamType type, Token token) {
        return String.format(INCOMPARABLE_TYPE_MESSAGE, token.getLine(), token.getCharPositionInLine(), type);
    }

    public static ParseError illegalCastError(SlamType source, SlamType cast, Token token) {
        return new ParseError(
                illegalCastMessage(source, cast, token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String illegalCastMessage(SlamType source, SlamType cast, Token token) {
        return String.format(ILLEGAL_CAST_MESSAGE, token.getLine(), token.getCharPositionInLine(), source, cast);
    }

    public static ParseError illegalTypeInArithmeticExpressionError(SlamType argumentType, Token token) {
        return new ParseError(
                illegalTypeInArithmeticExpressionMessage(argumentType, token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }


    public static String illegalTypeInArithmeticExpressionMessage(SlamType argumentType, Token token) {
        return String.format(ILLEGAL_TYPE_IN_ARITHMETIC_EXPRESSION, argumentType, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError incompatibleTypeDeclaration(Token token, SlamType expected, SlamType actual) {
        return new ParseError(
                incompatibleTypeDeclarationMessage(token, expected, actual),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static String incompatibleTypeDeclarationMessage(Token token, SlamType expected, SlamType actual) {
        return String.format(INCOMPATIBLE_TYPE_DECLARATION, token.getText(), token.getLine(), token.getCharPositionInLine(), expected, actual);
    }

    public static ParseError unknownAgentError(Token token) {
        return new ParseError(
                unknownAgentMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String unknownAgentMessage(Token token) {
        return String.format(UNKNOWN_AGENT_MESSAGE, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError illegalNumberOfParameters(Token token, int expected, int actual) {
        return new ParseError(
                illegalNumberOfParametersMessage(token.getText(), expected, actual, token.getLine(), token.getCharPositionInLine()),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String illegalNumberOfParametersMessage(String name, int expected, int actual, int line, int charPositionInLine) {
        return String.format(ILLEGAL_NUMBER_OF_PARAMETERS, name, line, charPositionInLine, expected, actual);
    }

    public static ParseError duplicatedInitialStateError(Token token, Token initialState) {
        return new ParseError(
                duplicatedInitialStateMessage(token, initialState),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String duplicatedInitialStateMessage(Token token, Token initialState) {
        return String.format(DUPLICATE_INITIAL_STATE, token.getText(), token.getLine(), token.getCharPositionInLine(),
                initialState.getText(), initialState.getLine(), initialState.getCharPositionInLine());
    }

    public static ParseError unknownTagError(Token token) {
        return new ParseError(
                unknownTagMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String unknownTagMessage(Token token) {
        return String.format(UNKNOWN_TAG_MESSAGE, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError illegalNumberOfMessageElements(Token token, int expected, int actual) {
        return new ParseError(
                illegalNumberOfMessageElementsMessage(token, expected, actual),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static String illegalNumberOfMessageElementsMessage(Token token, int expected, int actual) {
        return String.format(ILLEGAL_NUMBER_OF_MESSAGE_ELEMENTS, token.getText(), token.getLine(), token.getCharPositionInLine(), expected, actual);
    }

    public static String localVariableName(String agentName, String variableName) {
        return agentName+"@"+variableName;
    }

    public static ParseError illegalAgentExpressionError(Token token) {
        return new ParseError(
                illegalAgentExpressionMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static String illegalAgentExpressionMessage(Token token) {
        return String.format(ILLEGAL_AGENT_EXPRESSION, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError illegalAttributeDeclarationError(Token token) {
        return new ParseError(
                illegalAttributeDeclarationMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String illegalAttributeDeclarationMessage(Token token) {
        return String.format(ILLEGAL_ATTRIBUTE_DECLARATION, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError illegalParameterDeclarationError(SlamModelParser.AgentParameterContext parameterContext, SlamModelParser.AttributeDeclarationContext attributeDeclarationContext) {
        return new ParseError(
                illegalParameterDeclarationMessage(parameterContext.name, attributeDeclarationContext.name),
                parameterContext.name.getLine(),
                parameterContext.name.getCharPositionInLine()
        );
    }

    private static String illegalParameterDeclarationMessage(Token parameterToken, Token attributeToken) {
        return String.format(ILLEGAL_PARAMETER_DECLARATION, parameterToken.getText(), parameterToken.getLine(), parameterToken.getCharPositionInLine(),
                attributeToken.getLine(), attributeToken.getCharPositionInLine());
    }

    public static ParseError duplicatedAttributeError(Token attribute1, Token attribute2) {
        return new ParseError(
                duplicatedAttributeMessage(attribute1, attribute2),
                attribute1.getLine(),
                attribute1.getCharPositionInLine()
        );
    }


    private static String duplicatedAttributeMessage(Token attribute1, Token attribute2) {
        return String.format(DUPLICATED_ATTRIBUTE_DECLARATION, attribute1.getText(), attribute1.getLine(), attribute1.getCharPositionInLine(),
                attribute2.getLine(), attribute2.getCharPositionInLine());
    }

    public static ParseError duplicatedParameterError(Token param1, Token param2) {
        return new ParseError(
                duplicatedParameterMessage(param1, param2),
                param1.getLine(),
                param2.getCharPositionInLine()
        );
    }

    private static String duplicatedParameterMessage(Token attribute1, Token attribute2) {
        return String.format(DUPLICATED_PARAMETER_DECLARATION, attribute1.getText(), attribute1.getLine(), attribute1.getCharPositionInLine(),
                attribute2.getLine(), attribute2.getCharPositionInLine());
    }

    public static ParseError illegalExpressionError(Token token) {
        return new ParseError(
                illegalExpressionMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String illegalExpressionMessage(Token token) {
        return String.format(ILLEGAL_TIME_EXPRESSION, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError nameAlreadyUsedInMessagePattern(Token token) {
        return new ParseError(
                nameAlreadyUsedInMessageMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String nameAlreadyUsedInMessageMessage(Token token) {
        return String.format(NAME_ALREADY_USED_IN_PATTERN, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError unknownAgentStateError(String agentName, Token token) {
        return new ParseError(
                unknownAgentStateMessage(agentName, token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String unknownAgentStateMessage(String agentName, Token token) {
        return String.format(UNKNOWN_SYMBOL_STATE, token.getText(), token.getLine(), token.getCharPositionInLine(), agentName);
    }

    public static ParseError illegalAssignmentError(Token token) {
        return new ParseError(
                illegalAssignmentMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    private static String illegalAssignmentMessage(Token token) {
        return String.format(ILLEGAL_ASSIGNMENT, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    public static String illegalUseOfRandomExpressionMessage(Token token) {
        return String.format(ILLEGAL_RANDOM_EXPRESSION, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError illegalUseOfRandomExpression(Token token) {
        return new ParseError(
                illegalUseOfRandomExpressionMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    public static ParseError illegalUseOfTimedExpression(Token token) {
        return new ParseError(
                illegalUseOfTimedExpressionMessage(token),
                token.getLine(),
                token.getCharPositionInLine()
        );
    }

    static String illegalUseOfTimedExpressionMessage(Token token) {
        return String.format(ILLEGAL_TIME_EXPRESSION, token.getLine(), token.getCharPositionInLine());
    }

    public static ParseError duplicatedStateName(Token state, Token existingState) {
        return new ParseError(
                duplicatedStateNameMessage(state, existingState),
                state.getLine(),
                state.getCharPositionInLine());
    }

    private static String duplicatedStateNameMessage(Token state, Token existingState) {
        return String.format(DUPLICATED_STATE_NAME_MESSAGE, state.getText(), state.getLine(), state.getCharPositionInLine(), existingState.getLine(), existingState.getCharPositionInLine());
    }

    public static ParseError itIsNotAllowedHere(Token token) {
        return new ParseError(
            itIsNotAllowedHereMessage(token),
            token.getLine(),
            token.getCharPositionInLine()
        );
    }

    public static String itIsNotAllowedHereMessage(Token token) {
        return String.format(IT_IS_NOT_ALLOWED_HERE_MESSAGE, token.getLine(), token.getCharPositionInLine());
    }

}
