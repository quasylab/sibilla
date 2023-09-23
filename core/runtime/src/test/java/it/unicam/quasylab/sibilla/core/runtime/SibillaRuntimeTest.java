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

import java.io.IOException;
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

    private final static String CODE_TSP = "species S0;\n" +
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


    private final static String CODE_TSP_TEST = "species S0;\n" +
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


    private final static String TEST_TSP_OPT =
            "param meetRate = 1.0; \n" +
            "param pError = 0.5; \n" +
            "\n" +
            "param S0scale = 3; \n" +
            "param S1scale = 3; \n" +
            "param SUscale = 10; \n" +
            "param S0InitialConviction = 2; \n"+
            "param S1InitialConviction = 2; \n"+
            "const maxConviction = 5; \n" +
            "\n" +
            "species S0 of [0,maxConviction]; \n" +
            "species S1 of [0,maxConviction]; \n" +
            "species SU; \n" +
            "\n" +
            "rule S1_dissuade_S0 for c in [0,maxConviction] when c > 0 { \n" +
            "    S0[c]|S1[c] -[ #S0[c] * meetRate * %S1[c] * (1-pError) ]-> S0[c-1]|S1[c] \n" +
            "}\n" +
            "\n" +
            "rule S0_dissuade_S1 for c in [0,maxConviction] when c > 0 { \n" +
            "    S0[c]|S1[c] -[ #S1[c] * meetRate * %S0[c] ]-> S0[c]|S1[c-1] \n" +
            "}\n" +
            "\n" +
            "rule S0_become_uncertain_meeting_S1 for c in [0,maxConviction] when c == 0 { \n" +
            "    S0[c]|S1[c] -[ #S0[c] * meetRate * %S1[c] ]-> SU|S1[c] \n" +
            "}\n" +
            "\n" +
            "rule S1_become_uncertain_meeting_S0 for c in [0,maxConviction] when c == 0 { \n" +
            "    S0[c]|S1[c] -[ #S1[c] * meetRate * %S0[c] ]-> S0[c]|SU \n" +
            "}\n" +
            "\n" +
            "rule uncertain_become_S0_meeting_S0 for c in [0,maxConviction]{ \n" +
            "    SU|S0[c] -[ #SU*meetRate*%S0[c] ]-> S0[1]|S0[c] \n" +
            "}\n" +
            "\n" +
            "rule uncertain_become_S1_meeting_S1 for c in [0,maxConviction]{ \n" +
            "    SU|S1[c] -[ #SU*meetRate*%S1[c] ]-> S1[1]|S1[c] \n" +
            "}\n" +
            "\n" +
            "rule S0_become_uncertain for c in [0,maxConviction] when c == 0 { \n" +
            "    S0[c] -[ #S0[c] * meetRate * pError * (1-%S1[c]) ]-> SU \n" +
            "}\n" +
            "\n" +
            "rule S1_become_uncertain for c in [0,maxConviction] when c == 0 { \n" +
            "    S1[c] -[ #S1[c] * meetRate * pError * (1-%S0[c]) ]-> SU \n" +
            "}\n" +
            "\n" +
            "rule uncertain_become_S0 for c in [0,maxConviction]{ \n" +
            "    SU -[ #SU * meetRate * pError * (1-%S1[c])]-> S0[1]\n" +
            "}\n" +
            "\n" +
            "rule uncertain_become_S1 for c in [0,maxConviction]{ \n" +
            "    SU -[ #SU * meetRate * pError * (1-%S0[c]) ]-> S1[1]\n" +
            "} \n" +
            "\n" +
            "rule radicalization_of_S1 for c in [0,maxConviction] when c < maxConviction-1 { \n" +
            "    S1[c]|S1[c] -[ #S1[c] * meetRate * pError * (1-%S0[c]) ]-> S1[c]|S1[c+1] \n" +
            "} \n" +
            "rule radicalization_of_S0 for c in [0,maxConviction] when c < maxConviction-1 { \n" +
            "    S0[c]|S0[c] -[ #S0[c] * meetRate * pError * (1-%S1[c]) ]-> S0[c]|S0[c+1] \n" +
            "} \n" +
            "\n" +
            "\n" +
            "system start = S0[S0InitialConviction]<1*S0scale>|S1[S1InitialConviction]<1*S1scale>|SU<1*SUscale>; \n" +
            "\n" +
            "predicate consensus = ((( %S1[1] + %S1[2] + %S1[3] + %S1[4] + %S1[0] )) >= 0.999 ) || ((( %S0[1] + %S0[2] + %S0[3] + %S0[4] + %S0[0] )) >= 0.999 );" +
            "predicate win0 = ( %S0[1] + %S0[2] + %S0[3] + %S0[4] + %S0[0] ) >= 0.999; " +
            "predicate win1 = ( %S1[1] + %S1[2] + %S1[3] + %S1[4] + %S1[0] ) >= 0.999; " +
            "";




    public String TEST_SIR = "param meetRate = 1.0;      /* Meeting rate */\n" +
            "param infectionRate = 0.005;  /* Probability of Infection */\n" +
            "param recoverRate = 0.005;    /* Recovering rate */\n" +
            "\n" +
            "const startS = 95;           /* Initial number of S agents */\n" +
            "const startI = 5;           /* Initial number of I agents */\n" +
            "const startR = 0;            /* Initial number of R agents */\n" +
            "\n" +
            "species S;\n" +
            "species I;\n" +
            "species R;\n" +
            "\n" +
            "rule infection {\n" +
            "    S|I -[ #S * %I * meetRate * infectionRate ]-> I|I\n" +
            "}\n" +
            "\n" +
            "rule recovered {\n" +
            "    I -[ #I * recoverRate ]-> R\n" +
            "}\n" +
            "\n" +
            "system init = S<startS>|I<startI>|R<startR>;\n" +
            "predicate allRecovered = (#S+#I==0);";



    public String TEST_TSP_BATTERY = """
            param meetRate = 1;
                                  
            param scale = 10;
                                
            const b_size = 10;  /* battery max capacity         */
            const f_size = 2;   /* active flag size on and off  */
                        
            const startRED = 10;
            const startBLUE = 10;
                        
            param recharge_rate = 1; 
            param reactivation_rate = 1;
            param deactivation_rate = 1;
                                  
            species BLUE of [0,b_size]*[0,f_size];
            species RED of [0,b_size]*[0,f_size];
                        
            rule BLUE_persuades_RED for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b>=1)) {
                BLUE[b,f] | RED[b,f] -[ #BLUE[b,f] * meetRate * %RED[b,f] ]-> BLUE[b-1,f]|BLUE[b-1,f]
            }
                                            
            rule RED_persuades_BLUE for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b>=1)){
                BLUE[b,f]|RED[b,f] -[#RED[b,f] * meetRate * %BLUE[b,f] ]-> RED[b-1,f]|RED[b-1,f]
            }
                                          
            rule BLUE_deactivation for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b<b_size)){
                BLUE[b,f] -[ deactivation_rate ]-> BLUE[b,0]
            }
                                             
            rule RED_deactivation for b in [0,b_size] and f in [0,f_size] when ((f==1) && (b<b_size)){
                RED[b,f] -[ deactivation_rate ]-> RED[b,0]
            }
                                             
            rule BLUE_recharging for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b<b_size-1)){
                BLUE[b,f] -[ recharge_rate ]-> BLUE[b+1,f]
            }
                                           
            rule RED_recharging for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b<b_size-1)) {
                RED[b,f] -[ recharge_rate ]-> RED[b+1,f]
            }
                                           
            rule BLUE_reactivation for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b>0)){
                BLUE[b,f] -[ reactivation_rate ]-> BLUE[b,1]
            }
                                             
            rule RED_reactivation for b in [0,b_size] and f in [0,f_size] when ((f==0) && (b>0)){
                RED[b,f] -[ reactivation_rate ]-> RED[b,1]
            }
                        
                        
            system fair = RED[9,1]<startRED>|BLUE[9,1]<startBLUE>;                      
                        
            system balanced = RED[b_size-1,1]<1*scale>|BLUE[b_size-1,1]<1*scale>;
            system favor_of_RED = RED[b_size-1,1]<2*scale>|BLUE[b_size-1,1]<1*scale>;
            system favor_of_BLUE = RED[b_size-1,1]<1*scale>|BLUE[b_size-1,1]<2*scale>;
                        
            predicate RED_WIN = ( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ;
            predicate BLUE_WIN = ( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99 ;         
            predicate consensus = ((( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ) || (( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99)); /* predicate consensus = ( (RED_WIN) || (BLUE_WIN) ); */
            predicate consensus_and_charged = ((( %RED[0,0] + %RED[1,0] + %RED[2,0] + %RED[3,0] + %RED[4,0] + %RED[5,0] + %RED[6,0] + %RED[7,0] + %RED[8,0] + %RED[9,0] + %RED[0,1] + %RED[1,1] +  %RED[2,1] + %RED[3,1] + %RED[4,1] + %RED[5,1] + %RED[6,1] + %RED[7,1] + %RED[8,1] + %RED[9,1] ) > 0.99 ) || (( %BLUE[0,0] + %BLUE[1,0] + %BLUE[2,0] + %BLUE[3,0] + %BLUE[4,0] + %BLUE[5,0] + %BLUE[6,0] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %BLUE[0,1] + %BLUE[1,1] +  %BLUE[2,1] + %BLUE[3,1] + %BLUE[4,1] + %BLUE[5,1] + %BLUE[6,1] + %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1]) > 0.99)) && (( %BLUE[7,1] + %BLUE[8,1] + %BLUE[9,1] + %BLUE[7,0] + %BLUE[8,0] + %BLUE[9,0] + %RED[7,1] + %RED[8,1] + %RED[9,1] + %RED[7,0] + %RED[8,0] + %RED[9,0] )>0.7);
            """;

    public String TEST_THE_THING = """
            param infection_rate = 1.0;
            param paranoia = 1.0;
            param meet_rate = 1.0;
            param moral_decay_rate = 0.25;
            param suicide_rate = 0.05;
                        
            const max_sanity = 3;
                        
            species human of [0,max_sanity];
            species infected_human;
            species deceased;
                        
            rule loss_of_sanity for i in [0,max_sanity] when i>0 {
                human[i] -[ moral_decay_rate ]-> human[i-1]
            }
                        
            rule moral_support for i in [0,max_sanity] and j in [0,max_sanity]  when ((i<max_sanity-1)&&(j<max_sanity-1)) {
                human[i]|human[j] -[ (#human[0] + #human[1] + #human[2]) * meet_rate ]-> human[i+1]|human[j+1]
            }
                        
            rule human_get_infected for i in [0,max_sanity-1] {
                human[i]|infected_human -[ (#human[0] + #human[1] + #human[2]) * %infected_human * meet_rate ]-> infected_human|infected_human
            }
                        
            rule killing_human for i in [0,max_sanity-1] {
                human[i]|human[i] -[ (#human[0] + #human[1] + #human[2]) * meet_rate * paranoia ]-> human[i]|deceased
            }
                        
            rule killing_infected_human for i in [0,max_sanity-1] {
                human[i]|infected_human -[ (#human[0] + #human[1] + #human[2]) * %infected_human * meet_rate * paranoia  ]-> human[i]|deceased
            }
                        
            rule committing_suicide for i in [0,max_sanity] when i==0 {
                human[i] -[ suicide_rate ]-> deceased
            }
                        
            system initial = human[max_sanity-1]<10>|infected_human<3>;
                        
            predicate alien_eradicated = (%infected_human == 0.0);
            predicate most_humans_survived = ((%human[0] + %human[1] + %human[2])>=0.75);
            predicate half_humans_survived_and_alien_eradicated = ((%human[0] + %human[1] + %human[2])>=0.75) && (%infected_human == 0.0);
            """;



    public String TEST_FUNGI = """
            /* VARIABLES  */
                        
            param t = 19; /* current temperature */
            param h = 0.5;  /*  current humidity  */
                        
            /* CONSTANTS */
                        
            const e  = 2.7182818284590452353602874713527; /* euler number */
                        
            /* A */
                        
            const ideal_t_A = 15;
            const ideal_h_A = 0.7;
            const var_t_A = 1.6;
            const var_h_A = 0.8;
                        
            /* B */
                        
            const ideal_t_B = 22;
            const ideal_h_B = 0.4;
            const var_t_B = 1.0;
            const var_h_B = 0.7;
                        
            /* Common */
                        
            const interaction_rate = 0.005;
                        
            /* SPECIES */
                        
            /* species VOID;  To represent nothingness */
            species A;  /* Fungi type A */
            species B;  /* Fungi type B */
                        
            /* Reproduction rules */
                        
            rule reproduction_of_A {
                A -[  %A * (e^(-1*(( ideal_t_A - t )/(var_t_A))^2)  *  e^(-1*(( ideal_h_A - h )/(var_h_A))^2))  ]-> A|A
            }
                        
            rule reproduction_B {
                B -[  %B * (e^(-1*(( ideal_t_B - t )/(var_t_B))^2)  *  e^(-1*(( ideal_h_B - h )/(var_h_B))^2)) ]-> B|B
            }
                        
                        
            /* Killing rules */
                        
            rule A_kill_B {
                A|B -[ %A * interaction_rate * %B ]-> A
            }
                        
            rule B_kill_A {
                A|B -[ %B * interaction_rate * %A ]-> B
            }
                        
            /* SYSTEM */
                        
            system fair = A<10>|B<10>;
                        
            /* PREDICATE */
                        
            predicate balanced = (%A >= 0.45) && (%B >= 0.45) && ((#A+#B)>=15);
                        
            predicate majorityA = (%A >= 0.65) && (%B >= 0.25) && (#B>0);
            predicate majorityB = (%B >= 0.65) && (%A >= 0.25);
                        
            predicate onlyA = (%A == 1.0);
            predicate onlyB = (%B == 1.0);
            """;

    @Test
    public void testFungi() throws CommandExecutionException, IOException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.load(TEST_FUNGI);
        sr.setConfiguration("fair");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setSamplingStrategy("ffs");
        sr.setTrainingSetSize(40);
        sr.addSpaceInterval("t",-40,40);
        sr.addSpaceInterval("h",-5,5);
        sr.setProbReachAsObjectiveFunction(null,"majorityA",0.05,0.05);
        sr.generateTrainingSet();
        sr.saveTable("fungiBalanced","/Users/lorenzomatteucci/phd",null,null);
    }
    @Test
    public void testTheThing() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_THE_THING);
        sr.setConfiguration("initial");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setSamplingStrategy("ffs");
        sr.setTrainingSetSize(50);
        sr.addSpaceInterval("paranoia",0.05,2);
        sr.addSpaceInterval("meet_rate",0.05,2);
        sr.setProbReachAsObjectiveFunction(null,"half_humans_survived_and_alien_eradicated",0.05,0.05);
        sr.generateTrainingSet();
        sr.saveTable("alien","/Users/lorenzomatteucci/phd",null,null);
    }

    @Test
    public void testNewTSPBattery() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_TSP_BATTERY);
        sr.setConfiguration("fair");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setSamplingStrategy("ffs");
        sr.setTrainingSetSize(10);
        sr.addSpaceInterval("recharge_rate",0.05,2);
        sr.addSpaceInterval("reactivation_rate",0.05,2);
        sr.addSpaceInterval("deactivation_rate",0.05,2);
        sr.setProbReachAsObjectiveFunction(null,"consensus_and_charged",0.05,0.05);
        sr.generateTrainingSet();
        sr.saveTable("TSP_BATTERY","/Users/lorenzomatteucci/phd/tsp_battery",null,null);
    }
    @Test
    public void testNewSIR() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_SIR);
        sr.setConfiguration("init");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setSamplingStrategy("ffs");
        sr.setTrainingSetSize(50);
        sr.addSpaceInterval("infectionRate",0.005,1.5);
        sr.addSpaceInterval("recoverRate",0.005,1.5);
        sr.setProbReachAsObjectiveFunction(null,"allRecovered",0.05,0.05);
        sr.generateTrainingSet();
        //sr.saveTable("SIR_Sample","/Users/lorenzomatteucci/phd",null,null);
    }

//    @Test
//    public void testNewTSP() throws CommandExecutionException, IOException {
//        SibillaRuntime sr = getRuntimeWithModule();
//        sr.load(TEST_TSP_OPT);
//        sr.setConfiguration("start");
//        sr.addAllMeasures();
//        sr.setReplica(5);
//        sr.setDeadline(50);
//        sr.setDt(1);
//        sr.setSamplingStrategy("ffs");
//        sr.setTrainingSetSize(10);
//        sr.addSpaceInterval("meetRate",0.2,2);
//        sr.addSpaceInterval("pError",0.005,0.5);
//        sr.setProbReachAsObjectiveFunction(null,"win0",0.05,0.05);
//        sr.generateTrainingSet();
//        sr.saveTable("TSP_Sample","/Users/lorenzomatteucci/phd",null,null);
//    }

    @Test
    public void testNewTSP_Optimization() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_TSP_OPT);
        sr.setConfiguration("start");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(50);
        sr.setDt(1);
        sr.setSamplingStrategy("ffs");
        sr.setTrainingSetSize(20);
        sr.usingSurrogate(true);
        sr.addSpaceInterval("meetRate",0.2,2);
        sr.addSpaceInterval("pError",0.005,0.5);
        sr.setProbReachAsObjectiveFunction(null,"win0",0.05,0.05);
        sr.setOptimizationAsMinimization(false);
        sr.performOptimization();
        System.out.println(sr.getOptimizationInfo());
    }



//    @Test
//    public void testNewTSP_data_set() throws CommandExecutionException, IOException {
//        SibillaRuntime sr = getRuntimeWithModule();
//        sr.load(TEST_TSP_OPT);
//        sr.setConfiguration("start");
//        sr.addAllMeasures();
//        sr.setReplica(5);
//        sr.setDeadline(50);
//        sr.setDt(1);
//        sr.setSamplingStrategy("ffs");
//        sr.setTrainingSetSize(20);
//        sr.usingSurrogate(true);
//        sr.addSpaceInterval("meetRate",0.2,2);
//        sr.addSpaceInterval("maxConviction",2,11);
//        sr.setProbReachAsObjectiveFunction(null,"consensus",0.05,0.05);
//        sr.generateTrainingSet();
//        sr.saveTable("TSP_Sample_consensus","/Users/lorenzomatteucci/phd",null,null);
//
//    }


    @Test
    public void testNewTSP_Optimization_LTMADS() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(TEST_TSP_OPT);
        sr.setConfiguration("start");
        sr.addAllMeasures();
        sr.setReplica(5);
        sr.setDeadline(50);
        sr.setDt(1);
        sr.usingSurrogate(false);
        sr.setOptimizationStrategy("ltmads");
        sr.addSpaceInterval("meetRate",0.2,2);
        sr.addSpaceInterval("pError",0.005,0.5);
        sr.setProbReachAsObjectiveFunction(null,"win0",0.05,0.05);
        sr.setOptimizationAsMinimization(false);
        sr.performOptimization();
        System.out.println(sr.getOptimizationInfo());
    }

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
        sr.load(CODE_TSP);
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
    public void testOptimization() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE_TSP);
        sr.setConfiguration("balanced");
        sr.setDeadline(100.0);
        sr.usingSurrogate(true);
        sr.setProbReachAsObjectiveFunction(null,"consensus",0.1,0.1);
        sr.addSpaceInterval("scale",1.0,20.0);
        sr.setOptimizationAsMinimization(true);
        sr.performOptimization();
        System.out.println(sr.getOptimizationInfo());

//        interpreter.execute("optimizes using \"pso\" with surrogate \"rf\"");
//        interpreter.execute("search in \"x\" in [-10,10]");
//        interpreter.execute("min x^2+x+3");
    }


//    @Test
//    public void to() throws CommandExecutionException {
//        SibillaRuntime sr = getRuntimeWithModule();
//        sr.load(CDOE_TSP);
//        sr.setDeadline(100.0);
//        ToDoubleFunction<Map<String,Double>> fun = map -> {
//            map.keySet().forEach(key -> sr.setParameter(key,map.get(key)));
//            System.out.println("set parameter done");
//            sr.addAllMeasures();
//            System.out.println("add all measure done");
//            try {
//                sr.setConfiguration("balanced");
//            } catch (CommandExecutionException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                System.out.println("try computation");
//                double evaluation = sr.computeProbReach(null,"consensus",0.01,0.01);
//                System.out.println("evaluation : "+evaluation);
//                return evaluation;
//            } catch (CommandExecutionException e) {
//                System.out.println("fail");
//                throw new RuntimeException(e);
//            }
//        };
//
//        HyperRectangle hyperRectangle = new HyperRectangle(new ContinuousInterval("scale",1.0,10.0));
//        PSOTask psoTask = new PSOTask();
//        Map<String,Double> values = psoTask.minimize(fun,hyperRectangle);
//        System.out.println(values);
//    }


    @Test
    public void exceptionInSetProperty() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        try{
            sr.setSurrogateProperty("not_a_parameter","5");
        }catch (CommandExecutionException e){
            System.out.printf((e.getErrorMessages().toString()) + "%n","word");
        }

    }


//    @Test
//    public void testSIR() throws CommandExecutionException {
//
//         String TEST_SIR = """
//            param rate_meeting = 1.0;
//            param rateSI = 2.0;
//            param rateIR = rateSI / 8 ;
//            param rateRI = rateSI / 4 ;
//            param rateRS = rateSI / 8 ;
//
//            species S;
//            species I;
//            species R;
//
//            /*  infection of a susceptible */
//            rule rule_SI {
//              S|I -[ #S * %I * rate_meeting * rateSI ]-> I|I
//            }
//
//            /*  recovery of the infected */
//            rule rule_SI {
//              I -[ #I * rateIR ]-> R
//            }
//
//            /*  infection of a recovered */
//            rule rule_RI {
//              R|I -[#R * %I * rate_meeting * rateRI ]-> I|I
//            }
//
//
//            /*  the recovered returns susceptible */
//            rule rule_SI {
//              R -[ #R * rateRS ]-> S
//            }
//
//            system intitial = S < 990 > | I < I >;
//
//            predicate infection_spread_under_40 = ( %I <= 40);
//            """;
//
//
//        SibillaRuntime sr = getRuntimeWithModule();
//        sr.load(TEST_SIR);
//    }
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