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

package quasylab.sibilla.langs.pm;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Describes the info associated to a given symbol.
 */
public class SymbolInfo {

    private final SymbolType type;

    private final ParserRuleContext context;

    /**
     * Creates a symbol info with a given type defined in a given context.
     *
     * @param type symbol type.
     * @param context symbol context.
     */
    public SymbolInfo(SymbolType type, ParserRuleContext context) {
        this.type = type;
        this.context = context;
    }

    /**
     * Returns symbol type.
     *
     * @return symbol type.
     */
    public SymbolType getType() {
        return type;
    }

    /**
     * Returns the parser context where the symbol is defined.
     *
     * @return the parser context where the symbol is defined.
     */
    public ParserRuleContext getContext() {
        return context;
    }
}
