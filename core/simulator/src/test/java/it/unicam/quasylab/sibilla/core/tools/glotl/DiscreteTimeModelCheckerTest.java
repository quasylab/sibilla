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

package it.unicam.quasylab.sibilla.core.tools.glotl;

import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.models.lio.Agent;
import it.unicam.quasylab.sibilla.core.models.lio.AgentAction;
import it.unicam.quasylab.sibilla.core.models.lio.AgentsDefinition;
import it.unicam.quasylab.sibilla.core.models.lio.LIOIndividualState;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalEventuallyFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFractionOfFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAtomicFormula;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DiscreteTimeModelCheckerTest {

    @Test
    public void testProbabilityTime() {
        GLoTLDiscreteTimeModelChecker dtmc = new GLoTLDiscreteTimeModelChecker();
        AgentsDefinition def = getAgentDefinition();
        int stateZero = def.getAgentIndex("0");
        int stateOne = def.getAgentIndex("1");
        LIOIndividualState start = getInitialState(def,5, 5);
        ChachedFunction<LIOIndividualState,ProbabilityVector<LIOIndividualState>> cachedNext = new ChachedFunction<>(LIOIndividualState::next);
        ProbabilityVector<LIOIndividualState> vector = ProbabilityVector.dirac(start);
        ProbabilityVector<LIOIndividualState> cVector = ProbabilityVector.dirac(start);
        long startTime = System.currentTimeMillis();
        vector = vector.apply(LIOIndividualState::next);
        System.out.println("Next: "+vector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        cVector = cVector.apply(cachedNext);
        System.out.println("Cached Next ("+cachedNext.size()+"): "+cVector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        vector = vector.apply(LIOIndividualState::next);
        System.out.println("Next: "+vector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        cVector = cVector.apply(cachedNext);
        System.out.println("Cached Next ("+cachedNext.size()+"): "+cVector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        vector = vector.apply(LIOIndividualState::next);
        System.out.println("Next: "+vector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        cVector = cVector.apply(cachedNext);
        System.out.println("Cached Next ("+cachedNext.size()+"): "+cVector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        vector = vector.apply(LIOIndividualState::next);
        System.out.println("Next: "+vector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        cVector = cVector.apply(cachedNext);
        System.out.println("Cached Next ("+cachedNext.size()+"): "+cVector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        vector = vector.apply(LIOIndividualState::next);
        System.out.println("Next: "+vector.size()+": "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        cVector = cVector.apply(cachedNext);
        System.out.println("Cached Next ("+cachedNext.size()+"): "+cVector.size()+": "+(System.currentTimeMillis()-startTime));
    }

    @Test
    public void testInsertionTime() {
        GLoTLDiscreteTimeModelChecker dtmc = new GLoTLDiscreteTimeModelChecker();
        AgentsDefinition def = getAgentDefinition();
        int stateZero = def.getAgentIndex("0");
        int stateOne = def.getAgentIndex("1");
        long startTime = System.currentTimeMillis();
        ProbabilityVector<Integer> current = apply(0);
        System.out.println("Size: "+current.size()+" Time: "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        current = current.apply(this::apply);
        System.out.println("Size: "+current.size()+" Time: "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        current = current.apply(this::apply);
        System.out.println("Size: "+current.size()+" Time: "+(System.currentTimeMillis()-startTime));
    }

    @Test
    public void testMultiply() {
        double[] values = IntStream.range(0, 4096).mapToDouble(i -> 1.0/4096).toArray();
        double[][] matrix = IntStream.range(0, 4096).mapToObj(i -> values).toArray(double[][]::new);

        double[] current = values;

        long startTime = System.currentTimeMillis();
        current = multiply(current, matrix);
        System.out.println(" Time: "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        current = multiply(current, matrix);
        System.out.println(" Time: "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        current = multiply(current, matrix);
        System.out.println(" Time: "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();
        current = multiply(current, matrix);
        System.out.println(" Time: "+(System.currentTimeMillis()-startTime));


    }

    public double[] multiply(double[] v, double[][] matrix) {
        System.out.println(v.length);
        System.out.println(matrix.length);
        System.out.println(matrix[0].length);

        double[] result = new double[v.length];
        for(int i=0; i<v.length; i++) {
            for(int j=0; j<v.length; j++) {
                result[i]+=v[j]*matrix[i][j];
            }
        }
        return result;
    }

    public ProbabilityVector<Integer> apply(int i) {
        ProbabilityVector<Integer> test = new ProbabilityVector<>();
        int counter = 0;
        long startTime = System.currentTimeMillis();
        for(int j=0; j<4096; j++) {
            test.add(j, 1.0/4096);
        }
        return test;
    }


    @Disabled
    @Test
    public void testReachAllZeros() {
        GLoTLDiscreteTimeModelChecker dtmc = new GLoTLDiscreteTimeModelChecker();
        AgentsDefinition def = getAgentDefinition();
        int stateZero = def.getAgentIndex("0");
        int stateOne = def.getAgentIndex("1");
        long startTime = System.currentTimeMillis();
        LIOIndividualState start = getInitialState(def,1, 1);
        ChachedFunction<LIOIndividualState, ProbabilityVector<LIOIndividualState>> cachedNext = new ChachedFunction<>(LIOIndividualState::next);
        double prob = dtmc.computeProbability(cachedNext, start, getEventuallyAllOneFormula(0,10, stateZero, stateOne), 0.0001);
        System.out.println(System.currentTimeMillis()-startTime);
        startTime = System.currentTimeMillis();
        prob = dtmc.computeProbability(LIOIndividualState::next, start, getEventuallyAllOneFormula(0,30, stateZero, stateOne), 0.0001);
        System.out.println(System.currentTimeMillis()-startTime);
        assertEquals(0.25, prob);
    }


    public GlobalFormula<Agent, LIOIndividualState> getAllOneFormula(int stateZero, int stateOne) {
        return new GlobalFractionOfFormula<>(new LocalAtomicFormula<>(a -> (a.getIndex()==stateOne)), d -> d>=1.0);
    }

    public GlobalFormula<Agent, LIOIndividualState> getEventuallyAllOneFormula(int from, int to, int stateZero, int stateOne) {
        return new GlobalEventuallyFormula<>(from, to, getAllOneFormula(stateZero, stateOne));
    }

    public AgentsDefinition getAgentDefinition() {
        AgentsDefinition def = new AgentsDefinition();
        Agent stateZero = def.addAgent("0");
        Agent stateOne = def.addAgent("1");
        AgentAction beOne = def.addAction( "be1", s -> 0.5 );
        AgentAction beZero = def.addAction( "be0" , s -> 0.5 );
        stateZero.addAction(beOne, stateOne);
        stateOne.addAction(beZero, stateZero);
        return def;
    }

    private static LIOIndividualState getInitialState(AgentsDefinition def, int numberOfZeros, int numberOfOnes) {
        int stateZero = def.getAgentIndex("0");
        int stateOne = def.getAgentIndex("1");
        return new LIOIndividualState(def, IntStream.range(0,numberOfOnes+numberOfZeros).map(i -> (i<numberOfOnes?stateZero:stateOne)).toArray());
    }


}