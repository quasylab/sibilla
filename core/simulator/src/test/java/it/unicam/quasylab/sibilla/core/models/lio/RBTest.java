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

import java.util.Optional;
import java.util.function.DoublePredicate;
import java.util.stream.IntStream;

import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.models.TimeStep;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimeAgentSMC;
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimePathChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFractionOfFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAlwaysFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAtomicFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalNextFormula;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RBTest {

    @Test
    public void testAgentDefinition() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        assertNotNull(def.getAgent("B"));
        assertNotNull(def.getAgent("R"));
    }


    @Test
    public void testBehaviourOfAgentB() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        LIOAgent a = def.getAgent("B");
        LIOAgent b = def.getAgent("R");
        LIOActionsProbability prob = def.getActionProbability(getInitialState(def,10,10));
        ProbabilityVector<LIOAgent> next = a.probabilityVector(prob);
        assertEquals(2,next.size());
        assertEquals(0.25,next.getProbability(b));
    }

    @Test
    public void testBehaviourOfAgentR() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        LIOAgent a = def.getAgent("R");
        LIOAgent b = def.getAgent("B");
        LIOActionsProbability prob = def.getActionProbability(getInitialState(def,10,10));
        ProbabilityVector<LIOAgent> next = a.probabilityVector(prob);
        assertEquals(2,next.size());
        assertEquals(0.25,next.getProbability(b));
    }

    @Test
    public void testStep() {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        LIOAgentDefinitions def = getAgentDefinition(1);
        LIOAgent agentR = def.getAgent("R");
        LIOAgent agentB = def.getAgent("B");
        LIOIndividualState s = getInitialState(def,100000,100000);
        assertEquals(0.5,s.fractionOf(agentB));
        assertEquals(0.5,s.fractionOf(agentR));
        LIOModel model = new LIOModel(def);
        Optional<TimeStep<LIOState>> oNext = model.next(rg,0.0,s);
        assertTrue(oNext.isPresent());
        TimeStep<LIOState> next = oNext.get();
        assertEquals(0.5,next.getValue().fractionOf(agentB), 0.1);
    }


    @Test
    public void testAtomic() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        LIOModel model = new LIOModel(def);
        LIOIndividualState initial = getInitialState(def, 100,0);
        DiscreteTimePathChecker<LIOIndividualState, Boolean> phi = getPhiBal(100,0.1,def);
        DiscreteTimeAgentSMC<LIOIndividualState, LIOAgent> smc = new DiscreteTimeAgentSMC<>(model.nextIndividuals(), LIOIndividualState[]::new);
        smc.compute(initial,phi,100,100);
        assertTrue(true);
    }

    public DiscreteTimePathChecker<LIOAgent,Boolean> balancedLocalFormula(LIOAgentDefinitions def) {
        LIOAgent agentB = def.getAgent("B");
        return DiscreteTimeAgentSMC.getAtomic(agentB::equals);
    }

    public DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhiBal(int size, double eps, LIOAgentDefinitions def) {
        DoublePredicate dPred = d -> (d>=0.5-eps)&&(d<=0.5-eps);
        return DiscreteTimeAgentSMC.getFractionOf(size, balancedLocalFormula(def), dPred);
    }


    public LIOAgentDefinitions getAgentDefinition(double meet_probability) {
        LIOAgentDefinitions def = new LIOAgentDefinitions();
        LIOAgent agentR = def.addAgent("R");
        LIOAgent agentB = def.addAgent("B");
        LIOAgentAction redAction = def.addAction( "red", s -> s.fractionOf(agentB)*meet_probability );
        LIOAgentAction blueAction = def.addAction( "blue" , s -> s.fractionOf(agentR)*meet_probability );
        agentR.addAction(blueAction, agentB);
        agentB.addAction(redAction, agentR);
        return def;
    }

    private static LIOIndividualState getInitialState(LIOAgentDefinitions def, int red, int blue) {
        int agentB = def.getAgentIndex("B");
        int agentR = def.getAgentIndex("R");
        return new LIOIndividualState(def,IntStream.range(0,red+blue).map(i -> (i<red?agentR:agentB)).toArray());
    }

    @Test
    public void shouldNotBeLocalStable() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        LIOAgent red = def.getAgent("R");
        LIOAgent blue = def.getAgent("B");
        LocalFormula<LIOAgent> formula = LocalFormula.conjunction(phiStable(2, red, blue), phiStable(2, blue, red));

        formula = formula.next(red);
        assertFalse(formula.isAccepting());
        assertFalse(formula.isRejecting());
        formula = formula.next(blue);
        assertFalse(formula.isAccepting());
        assertFalse(formula.isRejecting());
        formula = formula.next(red);
        assertFalse(formula.isAccepting());
        assertTrue(formula.isRejecting());
    }

    @Test
    public void shouldBeLocalStable() {
        LIOAgentDefinitions def = getAgentDefinition(0.5);
        LIOAgent red = def.getAgent("R");
        LIOAgent blue = def.getAgent("B");
        LocalFormula<LIOAgent> formula = LocalFormula.conjunction(phiStable(2, red, blue), phiStable(2, blue, red));

        formula = formula.next(red);
        assertFalse(formula.isAccepting());
        assertFalse(formula.isRejecting());
        formula = formula.next(blue);
        assertFalse(formula.isAccepting());
        assertFalse(formula.isRejecting());
        formula = formula.next(blue);
        assertFalse(formula.isAccepting());
        assertFalse(formula.isRejecting());
        formula = formula.next(blue);
        assertTrue(formula.isAccepting());
        assertFalse(formula.isRejecting());
    }


    public void shouldBeBalanced() {

    }

    public static LocalFormula<LIOAgent> phiStable(int k, LIOAgent a1, LIOAgent a2) {
        return LocalFormula.imply(new LocalAtomicFormula<>(a1::equals),
                new LocalNextFormula<>(
                        LocalFormula.imply(new LocalAtomicFormula<>(a2::equals),
                                new LocalAlwaysFormula<>(0, k, new LocalAtomicFormula<>(a2::equals))
                        )
                )
        );
    }


    public static GlobalFormula<LIOAgent, LIOIndividualState> getPhiBal(double eps, LIOAgentDefinitions def) {
        DoublePredicate dPred = d -> (d>=0.5-eps)&&(d<=0.5+eps);
        return new GlobalFractionOfFormula<>(balancedLocalFormula2(def), dPred);
    }

    public static LocalFormula<LIOAgent> balancedLocalFormula2(LIOAgentDefinitions def) {
        LIOAgent agentB = def.getAgent("B");
        return new LocalAtomicFormula<>(agentB::equals);
    }


}
