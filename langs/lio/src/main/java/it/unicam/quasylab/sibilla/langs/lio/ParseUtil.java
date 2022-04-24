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

package it.unicam.quasylab.sibilla.langs.lio;

import org.antlr.v4.runtime.Token;

import java.util.Locale;

/**
 * Utility class that provides methods supporting activities of
 */
public class ParseUtil {

    private final static String DUPLICATED_NAME_MESSAGE = "Name %s at line %d:%d has been already declared ad line %d:%d.";
    private final static String TYPE_ERROR_MESSAGE = "Type error at line %d:%d. Expected %s is %s.";
    private final static String ILLEGAL_USE_OF_NAME_MESSAGE = "Illegal use of name %s at line %d:%d.";
    private static final String NUMERICAL_VALUE_EXPECTED_MESSAGE = "A numerical value is expected at at line %d:%d while it is %s.";
    private static final String BOOLEAN_VALUE_EXPECTED_MESSAGE = "A boolean value is expected at at line %d:%d while it is %s.";
    private static final String ILLEGAL_USE_OF_UNBOUND_NAME_MESSAGE = "Name %s is unbound at line %d:%d.";
    private static final String UNKNOWN_ACTION_MESSAGE = "Action %s is unknown at line %d:%d.";
    private static final String UNKNOWN_STATE_MESSAGE = "Action %s is unknown at line %d:%d.";

    public static String duplicatedNameErrorMessage(Token declaredToken, Token existingToken) {
        return String.format(DUPLICATED_NAME_MESSAGE,
                declaredToken.getText(),
                declaredToken.getLine(),
                declaredToken.getCharPositionInLine(),
                existingToken.getLine(),
                existingToken.getCharPositionInLine());
    }

    public static String typeErrorMessage(LIOType expected, LIOType actual, Token token) {
        return String.format(TYPE_ERROR_MESSAGE,
                token.getLine(),
                token.getCharPositionInLine(),
                expected.messageText(),
                actual.messageText());
    }

    public static String illegalUseOfNameError(Token token) {
        return String.format(ILLEGAL_USE_OF_NAME_MESSAGE,
                token.getText(),
                token.getLine(),
                token.getCharPositionInLine());
    }

    public static String numericalValueExpected(LIOType actual, Token token) {
        return String.format(NUMERICAL_VALUE_EXPECTED_MESSAGE,
                token.getLine(),
                token.getCharPositionInLine(),
                actual.messageText());
    }

    public static String booleanValueExpected(LIOType actual, Token token) {
        return String.format(BOOLEAN_VALUE_EXPECTED_MESSAGE,
                token.getLine(),
                token.getCharPositionInLine(),
                actual.messageText());
    }

    public static String illegalUseOfUnboundNameError(Token token) {
        return String.format(ILLEGAL_USE_OF_UNBOUND_NAME_MESSAGE,
                token.getText(),
                token.getLine(),
                token.getCharPositionInLine());
    }

    public static String unknownActionError(Token token) {
        return String.format(UNKNOWN_ACTION_MESSAGE,
                token.getText(),
                token.getLine(),
                token.getCharPositionInLine());
    }

    public static String unknownStateError(Token token) {
        return String.format(UNKNOWN_STATE_MESSAGE,
                token.getText(),
                token.getLine(),
                token.getCharPositionInLine());
    }

}
