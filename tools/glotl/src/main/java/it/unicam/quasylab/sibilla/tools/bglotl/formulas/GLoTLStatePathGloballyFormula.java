/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.bglotl.formulas;

/**
 * Represents a formula that is satisfied by a global path where  all the states reachable with a number
 * of steps between <code>from</code> and <code>to</code> satisfy <code>argument</code>.
 *
 * @param argument a local path formula
 * @param from     an integer
 * @param to       an integer
 */
public record GLoTLStatePathGloballyFormula(GLoTLStateFormula argument, int from, int to) implements GLoTLStatePathFormula {
}
