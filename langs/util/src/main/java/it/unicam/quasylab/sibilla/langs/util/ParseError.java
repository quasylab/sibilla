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

package it.unicam.quasylab.sibilla.langs.util;

/**
 * Utility class used to identify a generic error.
 */
public final class ParseError {

    private final String message;
    private final int line;
    private final int offset;


    /**
     * Create a new error with the given message and position.
     *
     * @param message error message.
     * @param line line message.
     * @param offset offset in the line.
     */
    public ParseError(String message, int line, int offset) {
        this.message = message;
        this.line = line;
        this.offset = offset;
    }

    /**
     * Return the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the line of the error.
     *
     * @return line of the error.
     */
    public int getLine() {
        return line;
    }

    /**
     * Return the position of the error in the line.
     * @return the position of the error in the line.
     */
    public int getOffset() {
        return offset;
    }
}
