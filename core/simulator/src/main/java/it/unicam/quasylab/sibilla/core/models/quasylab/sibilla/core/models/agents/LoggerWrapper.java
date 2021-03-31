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

package it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents;

public class LoggerWrapper<W extends World> implements AgentModelBuilder<W> {

    private AgentModelBuilder<W> modelBuilder;
    private AgentLogBuilder loggerBuilder;

    public LoggerWrapper(AgentModelBuilder<W> modelBuilder, AgentLogBuilder loggerBuilder) {
        this.modelBuilder = modelBuilder;
        this.loggerBuilder = loggerBuilder;
    }

    @Override
    public W getWorld() {
        return modelBuilder.getWorld();
    }

    @Override
    public void initialiseWorld() {
        modelBuilder.initialiseWorld();
    }

    @Override
    public int getNumberOfAgents() {
        return modelBuilder.getNumberOfAgents();
    }

    @Override
    public SystemEnvironment<W> getEnvironment() {
        return modelBuilder.getEnvironment();
    }

    @Override
    public OmegaFunction getOmegaFunction(int i) {
        return modelBuilder.getOmegaFunction(i);
    }

    @Override
    public AgentBehaviour getAgentBehaviour(int i) {
        AgentLog log = getAgentLogger(i);
        if (log == null) {
            return modelBuilder.getAgentBehaviour(i);
        } else {
            return new AgentLogger(modelBuilder.getAgentBehaviour(i),log);
        }
    }

    public AgentLog getAgentLogger(int i) {
        return loggerBuilder.getLogger(i);
    }

    @Override
    public VariableMapping getAgentState(W world, int i) {
        return modelBuilder.getAgentState(world,i);
    }

    @Override
    public VariableMapping getAgentInfo(W world, int i) {
        return modelBuilder.getAgentInfo(world,i);
    }
}
