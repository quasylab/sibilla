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

package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SibillaRuntimeTest {

    public final String TEST_PARAM = "param lambda = 1.0;" +
            "species A;" +
            "species B;" +
            "rule step { A -[ lambda ]-> B}\n" +
            "system start = A;" +
            "predicate done = #A == 0;";


    public final String CODE = "param lambda = 1.0;\n" +
            "param N1 = 10;\n" +
            "param N2 = 10;\n" +
            "param NU = 10;\n" +
            "const N = N1+N2+NU;\n"+
            "species A0;\n" +
            "species A1;\n " +
            "species AU;\n" +
            "rule change01 { A0|A1 -[ #A0*%A1*lambda ]-> A0|AU }\n" +
            "rule change10 { A1|A0 -[ #A1*%A0*lambda ]-> A1|AU }\n" +
            "rule changeU0 { A0|AU -[ #A0*%AU*lambda ]-> A0|A0 }\n" +
            "rule changeU1 { A1|AU -[ #A1*%AU*lambda ]-> A1|A1 }\n" +
            "system init = A0<10>|A1<10>|AU<10>;";

    public final String CODE2 = "param N = 10;\n" +
            "\n" +
            "species GA;\n" +
            "species GB;\n" +
            "species CA;\n" +
            "species CB;\n" +
            "\n" +
            "param lambda = 1.0;\n" +
            "\n" +
            "label typeA = {GA, CA}\n" +
            "label typeB = {GB, CB}\n" +
            "\n" +
            "rule groupieChangeA {\n" +
            "    GB -[ #GB*lambda*%typeA ]-> GA\n" +
            "}\n" +
            "\n" +
            "rule groupieChangeB {\n" +
            "    GA -[ #GA*lambda*%typeB ]-> GB\n" +
            "}\n" +
            "\n" +
            "rule celebrityChangeA {\n" +
            "    CB -[ #GB*lambda*%typeB ]-> CA\n" +
            "}\n" +
            "\n" +
            "rule celebrityChangeB {\n" +
            "    CA -[ #GB*lambda*%typeA ]-> CB\n" +
            "}\n" +
            "\n" +
            "system balancedGroupies = GA<N>|GB<N>;\n" +
            "\n" +
            "system groupiesAndCelebrities = GA<N-1>|GB<N-1>|CA|CB;";

    public static String GROUPIES = "species A;\n" +
            "species B;\n" +
            "\n" +
            "const lambda = 1.0;\n" +
            "const NA = 10;\n" +
            "const NB = 10;\n" +
            "\n" +
            "rule a_to_b {\n" +
            "    A -[ #A*lambda*%B ]-> B\n" +
            "}\n" +
            "\n" +
            "rule b_to_a {\n" +
            "    B -[ #B*lambda*%A ]-> A\n" +
            "}\n" +
            "\n" +
            "system balanced = A<NA>|B<NB>;\n";

    private static String CELEBRITIES = "species A;\n" +
            "species B;\n" +
            "\n" +
            "const lambda = 1.0;\n" +
            "const NA = 10;\n" +
            "const NB = 10;\n" +
            "\n" +
            "rule a_to_b {\n" +
            "    A -[ #A*lambda*(1-%B) ]-> B\n" +
            "}\n" +
            "\n" +
            "rule b_to_a {\n" +
            "    B -[ #B*lambda*(1-%A) ]-> A\n" +
            "}\n" +
            "\n" +
            "system balanced = A<NA>|B<NB>;";

    private final static String CDOE_TSP = "species S0;\n" +
            "species S1;\n" +
            "species SU;\n" +
            "\n" +
            "const meetRate = 1.0;\n" +
            "\n" +
            "rule su_to_s1 {\n" +
            "    SU|S1 -[ #SU*meetRate*%S1 ]-> S1|S1\n" +
            "}\n" +
            "\n" +
            "rule su_to_s0 {\n" +
            "    SU|S0 -[ #SU*meetRate*%S0 ]-> S0|S0\n" +
            "}\n" +
            "\n" +
            "rule s1_to_su {\n" +
            "    S1|S0 -[ #S1*meetRate*%S0 ]-> SU|S0\n" +
            "}\n" +
            "\n" +
            "rule s0_to_su {\n" +
            "    S0|S1 -[ #S0*meetRate*%S1 ]-> SU|S1\n" +
            "}\n" +
            "\n" +
            "param scale = 1.0;\n" +
            "\n" +
            "system balanced = S0<1*scale>|S1<1*scale>|SU<8*scale>;\n" +
            "\n" +
            "\n" +
            "system custom(s0,s1,su) = S0<s0>|S1<s1>|SU<su>;\n" +
            "\n" +
            "predicate consensus = (%S1==1.0)||(%S0==1.0);";

    private final static String TEST_SHMNGR = "param lambda = 2; \t\t/* arrival rate [60,30,12,6] || or customer arrive per [1,2,5,10] minute */\n" +
            "param mu = 1; \t\t\t/* service rate [60,30,12,6] || or customer served per [1,2,5,10] minute */\n" +
            "param maxQueueL = 5;\t/* Max length of the queue [5,10,25] */\n" +
            "param samplingRate = 1;\t/* every minute should be equal to dt */\n" +
            "\n" +
            "species person;\n" +
            "species newCustomer;\n" +
            "species customer;\n" +
            "species servedCustomer;\n" +
            "species waitingCustomer;\n" +
            "species rejectedCustomer;\n" +
            "species salesClerk;\n" +
            "species busySalesClerk;\n" +
            "\n" +
            "const queueLength = 0;\t\t\t\t/* Initial length of the queue of waiting customers*/\n" +
            "const startPerson = 10000;\t\t\t/* Pool of persons available */\n" +
            "const startNewCustomer = 0;\t \t\t/* Initial number of customers */\n" +
            "const startCustomer = 0;\t \t\t/* Initial number of customers */\n" +
            "const startrejectedCustomer = 0;\t/* Initial number of customers */\n" +
            "const startWaitingCustomer = 0;\t\t/* Initial number of customers --> Queue Length*/\n" +
            "const startServedCustomer = 0;\t\t/* Initial number of customers */\n" +
            "const startClerks = 1;\t\t\t\t/* Initial number of sales clerks [1,2,5] */ \n" +
            "const startBusySalesClerk = 0;\t\t/* Initial number of busy sales clerks*/ \n" +
            "\n" +
            "/* -1- Rule when entering the Shop - each iteration a person enters*/\n" +
            "rule person_to_newCustomer {\n" +
            "\tperson-[1]->newCustomer\n" +
            "}\n" +
            "\n" +
            "/* -2- Rule when slots are available and no queue - customer only served if sales clerk available and no waiting customer*/\n" +
            "rule newCustomer_to_customer{\n" +
            "\tnewCustomer|salesClerk-[(#salesClerk>0?1:0)*(#waitingCustomer>0?0:1)]->customer|busySalesClerk\n" +
            "}\n" +
            "\n" +
            "/* -3- Rule when no slots are available and queue possible - customer added to the queue if no clerk available and queue still with free spots  */ \n" +
            "/* \t   Customer will be rejected in case the queue is full - FIFO applied */\n" +
            "rule newCustomer_to_waitingCustomer{\n" +
            "\tnewCustomer|newCustomer-[(#waitingCustomer<maxQueueL?1:0)*(#salesClerk==0?1:0)]->waitingCustomer|rejectedCustomer\n" +
            "}\n" +
            "\n" +
            "/* -4- Rule when customer can leave the queue and is getting served - can only leave the queue if clerk is available*/\n" +
            "rule waitingCustomer_to_customer{\n" +
            "\twaitingCustomer|salesClerk-[(#salesClerk>0)?1:0]->customer|busySalesClerk\n" +
            "}\n" +
            "\n" +
            "/* -5- Rule when the customer is completely served and sales clerk gets available */\n" +
            "rule customer_to_servedCustomer {\n" +
            "\tcustomer|customer|busySalesClerk|busySalesClerk-[lambda/(#salesClerk*mu)]->customer|servedCustomer|busySalesClerk|salesClerk\n" +
            "}\n" +
            "\n" +
            "system shop=person<startPerson>|newCustomer<startNewCustomer>|customer<startCustomer>|salesClerk<startClerks>|servedCustomer<startServedCustomer>|waitingCustomer<startWaitingCustomer>|rejectedCustomer<startrejectedCustomer>|busySalesClerk<startBusySalesClerk>;";

    public static final String WOLVES_AND_SHEEPS = "const startW = 20;  /* Initial number of wolves */\n" +
            "const startS = 100; /* Initial number of sheeps */\n" +
            "const N = 10;       /* Width of the Grid */\n" +
            "const M = 10;       /* Length of the Grid */\n" +
            "\n" +
            "param lambdaMovementS = 0.5; /* Rate of sheeps movement */\n" +
            "param lambdaMovementW = 0.2; /* Rate of wolves movement */\n" +
            "param lambdaMeet = 10.0;     /* Rate of wolves meeting with sheeps */\n" +
            "param eatingProb = 5.0 ;     /* Probability of wolf eating a sheep */\n" +
            "\n" +
            "species W of [0,N]*[0,M];\n" +
            "species S of [0,N]*[0,M];\n" +
            "\n" +
            "rule go_up_S for i in [0,N] and j in [0,M] when (j<N){\n" +
            "    S[i,j] -[ lambdaMovementS * (1 - (#W[i,j+1]/(#W[i,j+1] + #W[i,j-1] + #W[i+1,j] + #W[i-1,j]))) ]-> S[i,j+1]\n" +
            "}\n" +
            "\n" +
            "rule go_down_S for i in [0,N] and j in [0,M] when (j>0){\n" +
            "    S[i,j] -[ lambdaMovementS * (1 - (#W[i,j-1]/(#W[i,j+1] + #W[i,j-1] + #W[i+1,j] + #W[i-1,j]))) ]-> S[i,j-1]\n" +
            "}\n" +
            "\n" +
            "rule go_right_S for i in [0,N] and j in [0,M] when (i<N){\n" +
            "    S[i,j] -[ lambdaMovementS * (1 - (#W[i+1,j]/(#W[i,j+1] + #W[i,j-1] + #W[i+1,j] + #W[i-1,j]))) ]-> S[i+1,j]\n" +
            "}\n" +
            "\n" +
            "rule go_left_S for i in [0,N] and j in [0,M] when (i>0){\n" +
            "    S[i,j] -[ lambdaMovementS * (1 - (#W[i-1,j]/(#W[i,j+1] + #W[i,j-1] + #W[i+1,j] + #W[i-1,j]))) ]-> S[i-1,j]\n" +
            "}\n" +
            "\n" +
            "rule eating for i in [0,N] and j in [0,M]{\n" +
            "    W[i,j]|S[i,j] -[ (lambdaMeet*(#W[i,j]/startW))*(eatingProb) ]-> W[i,j]\n" +
            "}\n" +
            "\n" +
            "rule go_up_W for i in [0,N] and j in [0,M] when (j<N){\n" +
            "    W[i,j] -[ lambdaMovementW * (#S[i,j+1]/(#S[i,j+1] + #S[i,j-1] + #S[i+1,j] + #S[i-1,j])) ]-> W[i,j+1]\n" +
            "}\n" +
            "\n" +
            "rule go_down_W for i in [0,N] and j in [0,M] when (j>0){\n" +
            "    W[i,j] -[ lambdaMovementW * (#S[i,j-1]/(#S[i,j+1] + #S[i,j-1] + #S[i+1,j] + #S[i-1,j])) ]-> W[i,j-1]\n" +
            "}\n" +
            "\n" +
            "rule go_right_W for i in [0,N] and j in [0,M] when (i<N){\n" +
            "    W[i,j] -[ lambdaMovementW * (#S[i+1,j]/(#S[i,j+1] + #S[i,j-1] + #S[i+1,j] + #S[i-1,j])) ]-> W[i+1,j]\n" +
            "}\n" +
            "\n" +
            "rule go_left_W for i in [0,N] and j in [0,M] when (i>0){\n" +
            "    W[i,j] -[ lambdaMovementW * (#S[i-1,j]/(#S[i,j+1] + #S[i,j-1] + #S[i+1,j] + #S[i-1,j])) ]-> W[i-1,j]\n" +
            "}\n" +
            "\n" +
            "system startHunting = W[5,5]<startW>|S[5,5]<startS>;";

    @Test
    public void shouldSelectPopulationModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        assertTrue(Arrays.deepEquals(new String[] {LIOModelModule.MODULE_NAME, PopulationModelModule.MODULE_NAME, YodaModelModule.MODULE_NAME}, sr.getModules()));
        sr.loadModule(PopulationModelModule.MODULE_NAME);
    }

    @Test
    public void shouldLoadASpecificationFromString() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
    }

    @Test
    public void shouldInstantiateASystemFromName() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
        assertEquals(1,sr.getInitialConfigurations().length);
        assertEquals("init",sr.getInitialConfigurations()[0]);
        sr.setConfiguration("init");
    }

    @Test
    public void shouldSimulate() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
        sr.setConfiguration("init");
        sr.addAllMeasures();
        sr.setReplica(1);
        sr.setDeadline(100);
        sr.setDt(1);
        sr.simulate("test");
    }


    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(PopulationModelModule.MODULE_NAME);
        return sr;
    }

    @Test
    public void shouldLoadAndSimulate() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE2);
        sr.setConfiguration("balancedGroupies");
        sr.addMeasure("#GB");
        sr.setDeadline(100);
        sr.setReplica(1);
        sr.setDt(1);
        sr.simulate("test");
        sr.printData("test");
    }

    @Test
    public void shouldBeBalanced() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(GROUPIES);
        sr.setConfiguration("balanced");
        sr.setDeadline(100);
        sr.setDt(1);
        sr.simulate("test");
        sr.printData("test");
    }

    @Test
    public void shouldBeBalancedCelebrities() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CELEBRITIES);
        sr.setConfiguration("balanced");
        assertNotNull(sr.getMeasures());
        assertEquals(4, sr.getMeasures().length);
    }

    @Test
    public void shouldComputeReachProbability() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CDOE_TSP);
        sr.setConfiguration("balanced");
        sr.setDeadline(100.0);
        assertEquals(1.0, sr.computeProbReach(null, "consensus", 0.1, 0.1));
    }

    @Test
    public void shouldChangeWithSet() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_PARAM);
        sr.setParameter("lambda", 2.0);
        sr.setConfiguration("start");
        sr.setDeadline(100.0);
        sr.setReplica(500);
        FirstPassageTimeResults res = sr.firstPassageTime(null, "done");
        assertEquals(0.5, res.getMean(),0.2);
        sr.setParameter("lambda", 1.0);
        sr.setConfiguration("start");
        res = sr.firstPassageTime(null, "done");
        assertEquals(1.0, res.getMean(),0.2);
        sr.setParameter("lambda", 3.0);
        sr.setConfiguration("start");
        res = sr.firstPassageTime(null, "done");
        assertEquals(1.0/3.0, res.getMean(),0.2);
    }

    @Test
    public void testShopManager() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_SHMNGR);
        sr.setConfiguration("shop");
        sr.setDeadline(100.0);
        sr.setReplica(1);
        sr.setDt(1);
        sr.simulate("test");
    }

    @Test
    public void testWolvesAndSheep() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(WOLVES_AND_SHEEPS);
        sr.setConfiguration("startHunting");
        sr.setDeadline(100.0);
        sr.setReplica(1);
        sr.setDt(1);
        sr.simulate("test");
    }

}