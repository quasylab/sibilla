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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.Set;


public class ParseUtil {
    private static final String DUPLICATED_ID_ERROR = "Identifier %s has been already used at line %d:%d.";
    private static final String DUPLICATED_ENTITY = "Entity %s has been already used";
    private static final String DUPLICATED_FIELD = "Field %s has been already used in record %s";
    private static final String DUPLICATED_ACTION = "Action %s has been already used";
    private static final String EXPECTED_NUMBER_ERROR = "Expected numeric type while is %s";
    private static final String UNKNOWN_ACTION_ERROR = "Action %s can not be resolved";
    private static final String UNKNOWN_AGENT_ERROR = "LIOAgent or System %s can not be resolved";
    private static final String UNKNOWN_SYMBOL_ERROR = "Symbol %s can not be resolved";
    private static final String ILLEGAL_SYMBOL_ERROR = "Symbol %s can not be used in this context";
    private static final String UNKNOWN_VARIABLE_ERROR = "Variable %s can not be resolved";
    private static final String WRONG_TYPE_ERROR = "Wrong type! Expected %s actual is %s";
    private static final String ILLEGAL_TYPE_ERROR_ARITHMETIC = "Illegal type! Type %s can not be used in arithmetic expression";
    private static final String UNKNOWN_ENTITY_ERROR = "LIOAgent or Variable %s can not be resolved";
    private static final String ILLEGAL_USE_OF_GROUP_EXPRESSION = "Illegal use of a group expression!";
    private static final String RECORD_TYPE_EXECTED = "Record expected where %s is provided";
    private static final String UNKNOWN_FIELD_NAME = "Field %s is not available in type %s";
    private static final String UNKNOWN_ASSIGNED_FIELD_NAME = "Field name %s is unknown";
    private static final String INCONSISTENT_FIELD_ASSIGNMENT = "Illegal assignment of field %s of record type %s where record type %s is expected";
    private static final String MISSING_FIELD_ASSIGNMENT = "Illegal record instantiation for type %s. The following fields are missed: %s";
    private static final String ILLEGAL_RANDOM_EXPRESSION = "Illegal use of random expression";
    private static final String DUPLICATED_ELEMENT_DECLARATION = "Another element with the same name %s has been declared at line %d:%d";
    private static final String UNKNOWN_YODA_TYPE = "Type %s is unknown";
    private static final String UNKNOWN_ATTRIBUTE_NAME = "Unknown attribute %s";
    private static final String ILLEGAL_ATTRIBUTE_UPDATE = "Illegal update of attribute %s";
    private static final String DUPLICATED_SYSTEM_DECLARATION = "Duplicated system declaration";
    private static final String DUPLICATED_ATTRIBUTE_DECLARATION = "Attribute %s has been already declared at line %d:%d";


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

    public static ParseError duplicatedFieldError(Token fieldName, String recordName) {
        return new ParseError(
                String.format(DUPLICATED_FIELD, fieldName.getText(), recordName),
                fieldName.getLine(),
                fieldName.getCharPositionInLine()
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

    public static ParseError illegalSymbolError(String name, ParserRuleContext ctx) {
        return new ParseError(
                String.format(ILLEGAL_SYMBOL_ERROR, name),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
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


    public static ParseError illegalGroupExpression(YodaModelParser.ExprContext ctx) {
        return new ParseError(
                String.format(ILLEGAL_USE_OF_GROUP_EXPRESSION),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError recordTypeExpected(YodaType actual, YodaModelParser.ExpressionRecordAccessContext ctx) {
        return new ParseError(
                String.format(RECORD_TYPE_EXECTED, actual),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownFieldName(YodaType recordType, String fieldName, YodaModelParser.ExpressionRecordAccessContext ctx) {
        return new ParseError(
                String.format(UNKNOWN_FIELD_NAME, fieldName, recordType),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownAssignedFieldName(Token name) {
        return new ParseError(
                String.format(UNKNOWN_ASSIGNED_FIELD_NAME, name.getText()),
                name.getLine(),
                name.getCharPositionInLine()
        );
    }

    public static ParseError inconsistentFieldAssignment(YodaType.RecordType expected, YodaType.RecordType recordType, Token name) {
        return new ParseError(
                String.format(INCONSISTENT_FIELD_ASSIGNMENT, name.getText(), recordType, expected),
                name.getLine(),
                name.getCharPositionInLine()
        );
    }

    public static ParseError missingFieldAssignment(Set<String> fields, YodaType.RecordType recordType, YodaModelParser.ExpressionRecordContext ctx) {
        return new ParseError(
                String.format(MISSING_FIELD_ASSIGNMENT, recordType, String.join(", ", fields)),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError illegalRandomExpression(YodaModelParser.ExprContext ctx) {
        return new ParseError(
                String.format(ILLEGAL_RANDOM_EXPRESSION),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownSymbolError(Token name) {
        return new ParseError(
                String.format(UNKNOWN_SYMBOL_ERROR, name.getText()),
                name.getLine(),
                name.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedElementDeclaration(String name, ParserRuleContext existing, ParserRuleContext dup) {
        return new ParseError(
                String.format(DUPLICATED_ELEMENT_DECLARATION, name, existing.start.getLine(), existing.start.getCharPositionInLine()),
                dup.start.getLine(),
                dup.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownTypeName(YodaModelParser.TypeContext type) {
        return new ParseError(
                String.format(UNKNOWN_YODA_TYPE, type.getRuleIndex()),
                type.start.getLine(),
                type.start.getCharPositionInLine()
        );
    }

    public static ParseError unknownAttributeName(Token fieldName) {
        return new ParseError(
                String.format(UNKNOWN_ATTRIBUTE_NAME, fieldName.getText()),
                fieldName.getLine(),
                fieldName.getCharPositionInLine()
        );
    }

    public static ParseError illegalUpdateOfAttribute(Token fieldName) {
        return new ParseError(
                String.format(ILLEGAL_ATTRIBUTE_UPDATE, fieldName.getText()),
                fieldName.getLine(),
                fieldName.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
        return new ParseError(
                String.format(DUPLICATED_SYSTEM_DECLARATION),
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    public static ParseError duplicatedAttributeDeclarationError(String attributeName,
                                                                 YodaModelParser.NameDeclarationContext existingDeclaration,
                                                                 YodaModelParser.NameDeclarationContext newDeclaration) {
        return new ParseError(
                String.format(DUPLICATED_ATTRIBUTE_DECLARATION, attributeName, existingDeclaration.name.getLine(), existingDeclaration.name.getCharPositionInLine()),
                newDeclaration.name.getLine(),
                newDeclaration.name.getCharPositionInLine()
        );
    }

}
