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

package it.unicam.quasylab.sibilla.shell;

import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;

import java.beans.PropertyChangeListener;
import java.io.Console;
import java.io.PrintStream;

public class ShellSimulationMonitor implements SimulationMonitor {
    private final PrintStream output;

    public ShellSimulationMonitor(PrintStream output) {
        this.output = output;
    }

    @Override
    public void startIteration(int i) {
        output.print("<");
        output.flush();
    }

    @Override
    public void endIteration(int i) {
        output.print(">");
        output.flush();
        if (i%50==0) {
            output.println(i);
        }
    }

    @Override
    public void endSimulation() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
    @Override
    public void registerPropertyChangeListener(PropertyChangeListener l) {

    }
}
