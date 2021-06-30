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

import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

public class SibillaShell {

    public SibillaShell() {
    }

    public SibillaShell(PrintStream out, Console console, PrintStream err) {
    }

    private void initModules() {
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        SibillaShell shell = new SibillaShell();
        shell.interactive();
    }

    private void interactive() {
        Scanner input = new Scanner(System.in);
        SibillaShellInterpreter interpreter = new SibillaShellInterpreter(System.out,System.err,new SibillaRuntime(),true);
        while(interpreter.isRunning() ) {
            System.out.print("> ");
            System.out.flush();
            String command = input.nextLine();
            if (!command.isEmpty()) {
                System.out.println("#> "+command);
                interpreter.execute(command);
            }
        }
    }




}
