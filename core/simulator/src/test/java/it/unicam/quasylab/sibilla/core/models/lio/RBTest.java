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

package it.unicam.quasylab.sibilla.core.models.lio;

import java.util.List;
import java.util.Optional;
import java.util.function.DoublePredicate;
import java.util.stream.IntStream;

import it.unicam.quasylab.sibilla.core.models.TimeStep;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimeAgentSMC;
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimePathChecker;
import it.unicam.quasylab.sibilla.core.util.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RBTest {

    @Test
    public void testAgentDefinition() {
        AgentsDefinition def = getAgentDefinition(0.5);
        assertNotNull(def.getAgent("B"));
        assertNotNull(def.getAgent("R"));
    }


    @Test
    public void testBehaviourOfAgentB() {
        AgentsDefinition def = getAgentDefinition(0.5);
        Agent a = def.getAgent("B");
        Agent b = def.getAgent("R");
        ActionsProbability prob = def.getActionProbability(getInitialState(def,10,10));
        List<Pair<Double, Agent>> next = a.next(prob);
        assertEquals(1,next.size());
        assertEquals(0.25,next.get(0).getKey().doubleValue());
        assertEquals(b,next.get(0).getValue());
    }

    @Test
    public void testBehaviourOfAgentR() {
        AgentsDefinition def = getAgentDefinition(0.5);
        Agent a = def.getAgent("R");
        Agent b = def.getAgent("B");
        ActionsProbability prob = def.getActionProbability(getInitialState(def,10,10));
        List<Pair<Double, Agent>> next = a.next(prob);
        assertEquals(1,next.size());
        assertEquals(0.25,next.get(0).getKey().doubleValue());
        assertEquals(b,next.get(0).getValue());
    }

    @Test
    public void testStep() {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        AgentsDefinition def = getAgentDefinition(1);
        Agent agentR = def.getAgent("R");
        Agent agentB = def.getAgent("B");
        LIOIndividualState s = getInitialState(def,100000,100000);
        assertEquals(0.5,s.fractionOf(agentB.getIndex()));
        assertEquals(0.5,s.fractionOf(agentR.getIndex()));
        LIOModel model = new LIOModel(def);
        Optional<TimeStep<LIOIndividualState>> oNext = model.next(rg,0.0,s);
        assertTrue(oNext.isPresent());
        TimeStep<LIOIndividualState> next = oNext.get();
        assertEquals(0.5,next.getValue().fractionOf(agentB.getIndex()), 0.1);
    }


    @Test
    public void testAtomic() {
        AgentsDefinition def = getAgentDefinition(0.5);
        LIOModel<LIOIndividualState> model = new LIOModel(def);
        LIOIndividualState initial = getInitialState(def, 200000,0);
        DiscreteTimePathChecker<LIOIndividualState, Boolean> phi = getPhiBal(100,0.1,def);
        DiscreteTimeAgentSMC<LIOIndividualState,Agent> smc = new DiscreteTimeAgentSMC<>(model,LIOIndividualState[]::new);
        smc.compute(initial,phi,100,100);
        assertTrue(true);
    }

    public DiscreteTimePathChecker<Agent,Boolean> balancedLocalFormula(AgentsDefinition def) {
        Agent agentB = def.getAgent("B");
        return DiscreteTimeAgentSMC.getAtomic(agentB::equals);
    }

    public DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhiBal(int size, double eps, AgentsDefinition def) {
        DoublePredicate dPred = d -> (d>=0.5-eps)&&(d<=0.5-eps);
        return DiscreteTimeAgentSMC.getFractionOf(size, balancedLocalFormula(def), dPred);
    }


    public AgentsDefinition getAgentDefinition(double meet_probability) {
        AgentsDefinition def = new AgentsDefinition();
        Agent agentR = def.addAgent("R");
        Agent agentB = def.addAgent("B");
        AgentAction redAction = def.addAction("red", s -> s.fractionOf(agentR)*meet_probability );
        AgentAction blueAction = def.addAction( "blue" , s -> s.fractionOf(agentB)*meet_probability );
        agentR.addAction(blueAction, agentB);
        agentB.addAction(redAction, agentR);
        return def;
    }

    private static LIOIndividualState getInitialState(AgentsDefinition def, int red, int blue) {
        int agentB = def.getAgentIndex("B");
        int agentR = def.getAgentIndex("R");
        return new LIOIndividualState(def,IntStream.range(0,red+blue).map(i -> (i<red?agentR:agentB)).toArray());
    }


}
