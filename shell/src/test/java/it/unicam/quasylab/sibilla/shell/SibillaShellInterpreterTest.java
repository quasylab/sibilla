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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SibillaShellInterpreterTest {

    @Test
    public void testLoadCommand() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();
        String fileName = ClassLoader.getSystemClassLoader().getResource("./celebr.pm").getFile();
        interpreter.execute("module ");
        interpreter.execute("run \"groupies.sib\"");
    }

    @Test
    public void testOptimizationProperty() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();
        interpreter.execute("optimizes using pso with surrogate rfr");
        try{
            interpreter.execute("set optimization property \"not_an_optimization_property\" \"5\" ");
        } catch(Exception e){
            assertTrue(e.getMessage().contains("particles_number"));
            assertTrue(e.getMessage().contains("Integer"));
        }
    }

    @Test
    public void testSurrogateProperty() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();
        interpreter.execute("optimizes using pso with surrogate rfr");
        try{
            interpreter.execute("set optimization property \"not_an_surrogate_property\" \"5\" ");
        } catch(Exception e){
            assertTrue(e.getMessage().contains("shrinkage"));
            assertTrue(e.getMessage().contains("Real number"));
        }
    }

    /**
     *      search_space_interval :
     *          'search' 'in' variable=STRING 'in' '[' lower_bound=expr',' upper_bound=expr ']'
     *      ;
     */
    @Test
    public void testInterval() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();
        interpreter.execute("search in \"interval\" in [1.5,3^3^2]");
        interpreter.execute("search in \"interval\" in [1.5,3^(3^2)]");
    }


    @Test
    public void testConstraint() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();
        //interpreter.execute("add constraint \"x\" >= \"y\" + 2 ");
        interpreter.execute("add constraint (x >= y + 2) ");
        interpreter.execute("add constraint x >= y + 2 ");
    }

    @Test
    public void testExprOpt() {
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter();

        interpreter.execute("optimizes using \"pso\" with surrogate \"rf\"");
        interpreter.execute("search in \"x\" in [-10,10]");
        interpreter.execute("min x^2+x+3");

    }

}