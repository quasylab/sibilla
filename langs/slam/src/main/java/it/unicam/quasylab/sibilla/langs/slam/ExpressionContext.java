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

/**
 * This enum is used to identify the possible contexts where an expression can occur.
 */
public enum ExpressionContext {
    NONE,
    PARAMETER,
    CONSTANT,
    PREDICATE,
    MEASURE,
    AGENT_VIEW,
    AGENT_MESSAGE_HANDLER,
    AGENT_COMMAND,
    AGENT_ATTRIBUTE,
    AGENT_SOJOURN_TIME,
    AGENT_TIME_UPDATE,
    SYSTEM;

    public boolean randomExpressionAllowed() {
        switch (this) {
            case NONE:
            case PARAMETER:
            case CONSTANT:
            case PREDICATE:
            case MEASURE:
            case SYSTEM:
                return false;
            case AGENT_MESSAGE_HANDLER:
            case AGENT_VIEW:
            case AGENT_COMMAND:
            case AGENT_ATTRIBUTE:
            case AGENT_SOJOURN_TIME:
            case AGENT_TIME_UPDATE:
                return true;
        }
        return false;
    }

    public boolean timedExpressionAllowed() {
        switch (this) {
            case NONE:
            case PARAMETER:
            case CONSTANT:
            case PREDICATE:
            case MEASURE:
            case SYSTEM:
                return false;
            case AGENT_MESSAGE_HANDLER:
            case AGENT_VIEW:
            case AGENT_COMMAND:
            case AGENT_ATTRIBUTE:
            case AGENT_SOJOURN_TIME:
            case AGENT_TIME_UPDATE:
                return true;
        }
        return false;
    }

    public boolean agentExpressionAllowed() {
        switch (this) {
            case PREDICATE:
            case MEASURE:
            case AGENT_VIEW:
                return true;
            default:
                return false;
        }
    }
}
