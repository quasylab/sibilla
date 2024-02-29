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


import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import it.unicam.quasylab.sibilla.shell.expression.ArithmeticExpressionVisitor;
import it.unicam.quasylab.sibilla.shell.expression.BooleanExpressionVisitor;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SibillaShellInterpreter extends SibillaScriptBaseVisitor<Boolean> {
    public static final String MODULE_MESSAGE = "Module %s has been successfully loaded\n";
    private static final String WELCOME_MESSAGE = "Sibilla interactive shell...\n";
    private static final String OK_MESSAGE = "Ok\n";
    private static final String CODE_LOADED = "Code %s has been successfully loaded\n";
    private static final String GOODBYE_MESSAGE = "Goodbye...";

    private final SibillaRuntime runtime;
    private final PrintStream output;
    private final PrintStream error;
    private final boolean isInteractive;
    private final boolean withOutput = true;
    private boolean terminated = false;
    private File currentDirectory = new File(".");

    public SibillaShellInterpreter(PrintStream out, PrintStream err, SibillaRuntime runtime, boolean isInteractive) {
        this.runtime = runtime;
        this.output = out;
        this.error = err;
        this.isInteractive = isInteractive;
    }

    public SibillaShellInterpreter() {
        this(System.out, System.err, new SibillaRuntime(), false);
    }

    public SibillaRuntime getRuntime() {
        return this.runtime;
    }

    public void showMessage(String message) {
        if (withOutput) {
            output.println(message);
        }
    }

    public void showErrorMessage(String message) {
        error.println(message);
    }

    public synchronized void execute(String code) {
        execute(CharStreams.fromString(code));
    }

    public synchronized void executeFile(String fileName) throws IOException {
        File selectedFile = getFile(fileName);
        this.currentDirectory = selectedFile.getParentFile();
        execute(CharStreams.fromFileName(selectedFile.getAbsolutePath()));
    }


    private void execute(CharStream code) {
        SibillaScriptLexer lexer = new SibillaScriptLexer(code);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SibillaScriptParser parser = new SibillaScriptParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        SibillaScriptParser.ScriptContext parseTree = parser.script();
        if (errorListener.withErrors()) {
            printScriptErrors(errorListener.getErrorCollector().getSyntaxErrorList());
        } else {
            this.visitScript(parseTree);
        }
    }

    private void printScriptErrors(List<ParseError> syntaxErrorList) {
        for (ParseError e : syntaxErrorList) {
            showErrorMessage(e.getMessage());
        }
    }

    @Override
    public Boolean visitScript(SibillaScriptParser.ScriptContext ctx) {
        boolean flag = true;
        for (SibillaScriptParser.CommandContext cmd : ctx.command()) {
            flag &= doExecute(cmd);
        }
        return flag;
    }

    private boolean doExecute(SibillaScriptParser.CommandContext cmd) {
        try {
            return visit(cmd);
        } catch (Exception e) {
            showErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean visitModule_command(SibillaScriptParser.Module_commandContext ctx) {
        try {
            runtime.loadModule(getStringContent(ctx.name.getText()));
            showMessage(String.format(MODULE_MESSAGE, ctx.name.getText()));
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    private void printErrorMessages(List<String> errorMessages) {
        errorMessages.forEach(this::showErrorMessage);
    }

    @Override
    public Boolean visitSeed_command(SibillaScriptParser.Seed_commandContext ctx) {
        runtime.setSeed(Long.parseLong(ctx.value.getText()));
        showMessage(OK_MESSAGE);
        return true;
    }


    private File getFile(String fileName) {
        Path p = Paths.get(fileName);
        if (p.isAbsolute()) {
            return p.toFile();
        } else {
            return new File(currentDirectory,fileName);
        }
    }

    @Override
    public Boolean visitLoad_command(SibillaScriptParser.Load_commandContext ctx) {
        try {
            String value = getStringContent(ctx.value.getText());
            File target = getFile(value);
            runtime.load(target);
            showMessage(String.format(CODE_LOADED, target.getCanonicalPath()));
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        } catch (IOException e) {
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    private String getStringContent(String value) {
        return value.substring(1, value.length() - 1);
    }

    @Override
    public Boolean visitEnvironment_command(SibillaScriptParser.Environment_commandContext ctx) {
        runtime.getEvaluationEnvironment().forEach((s, v) -> output.printf("%s=%f\n", s, v));
        return true;
    }

    @Override
    public Boolean visitSet_command(SibillaScriptParser.Set_commandContext ctx) {
        runtime.setParameter(getStringContent(ctx.name.getText()), Double.parseDouble(ctx.value.getText()));
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitClear_command(SibillaScriptParser.Clear_commandContext ctx) {
        runtime.clear();
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitReset_command(SibillaScriptParser.Reset_commandContext ctx) {
        if (ctx.name != null) {
            runtime.reset(ctx.name.getText());
        } else {
            runtime.reset();
        }
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitLs_command(SibillaScriptParser.Ls_commandContext ctx) {
        if (!currentDirectory.isDirectory()) {
            return false;
        }
        for (String s: currentDirectory.list()) {
            showMessage(s);
        }
        return true;
    }

    @Override
    public Boolean visitModules_command(SibillaScriptParser.Modules_commandContext ctx) {
        String[] modules = runtime.getModules();
        return printInfo("List of available modules:", modules);
    }

    private Boolean printInfo(String head, String[] modules) {
        output.println(head);
        Arrays.stream(modules).forEach(output::println);
        return true;
    }

    @Override
    public Boolean visitState_command(SibillaScriptParser.State_commandContext ctx) {
        try {
            runtime.setConfiguration(getStringContent(ctx.name.getText()), ctx.values.stream().mapToDouble(v -> Double.parseDouble(v.getText())).toArray());
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    @Override
    public Boolean visitInfo_command(SibillaScriptParser.Info_commandContext ctx) {
        output.println(runtime.info());
        return true;
    }

    @Override
    public Boolean visitReplica_command(SibillaScriptParser.Replica_commandContext ctx) {
        if (ctx.value == null) {
            showMessage(String.format("Current replica=%d", runtime.getReplica()));
            return true;
        }
        runtime.setReplica(Integer.parseInt(ctx.value.getText()));
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitDeadline_command(SibillaScriptParser.Deadline_commandContext ctx) {
        try {
            if (ctx.value == null) {
                showMessage(String.format("Current deadline=%f", runtime.getDeadline()));
                return true;
            }
            runtime.setDeadline(Double.parseDouble(ctx.value.getText()));
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    @Override
    public Boolean visitDt_command(SibillaScriptParser.Dt_commandContext ctx) {
        try {
            if (ctx.value == null) {
                showMessage(String.format("Current dt=%f", runtime.getDt()));
                return true;
            }
            runtime.setDt(Double.parseDouble(ctx.value.getText()));
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    @Override
    public Boolean visitMeasures_command(SibillaScriptParser.Measures_commandContext ctx) {
        String[] measures = Arrays.stream(runtime.getMeasures()).map(s -> (runtime.isEnabledMeasure(s) ? s + " *" : s)).toArray(String[]::new);
        printInfo("List of available measures:", measures);
        return true;
    }

    @Override
    public Boolean visitPredicates_command(SibillaScriptParser.Predicates_commandContext ctx) {
        String[] predicates = Arrays.stream(runtime.getPredicates()).toArray(String[]::new);
        printInfo("List of available predicates:", predicates);
        return true;
    }

    @Override
    public Boolean visitFirst_passage_time(SibillaScriptParser.First_passage_timeContext ctx) {
        String predicateName = getStringContent(ctx.name.getText());
        ShellSimulationMonitor monitor = null;
        if (isInteractive) {
            monitor = new ShellSimulationMonitor(output);
        }
        try {
            showFirstPassageTimeResults(predicateName, runtime.firstPassageTime(monitor, predicateName) );
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
            return false;
        }
    }

    private void showFirstPassageTimeResults(String name, FirstPassageTimeResults firstPassageTime) {
        if (firstPassageTime.getTests()==0) {
            printInfo("First passage time "+name+":", new String[] { "Tests = "+0});
            return ;
        }
        long hits = firstPassageTime.getHits();
        if (hits == 0) {
            printInfo("First passage time "+name+":", new String[] { "Tests = "+firstPassageTime.getTests(),
                "Hits = "+hits});
            return ;
        }
        printInfo("First passage time "+name+":", new String[] { "Tests = "+firstPassageTime.getTests(),
                "Hits = "+hits,
                "Mean = "+firstPassageTime.getMean(),
                "SD = "+firstPassageTime.getStandardDeviation(),
                "MIN = "+firstPassageTime.getMin(),
                "Q1 = "+firstPassageTime.getQ1(),
                "Q2 = "+firstPassageTime.getQ2(),
                "Q3 = "+firstPassageTime.getQ3(),
                "MAX = "+firstPassageTime.getMax()
        });

    }

    @Override
    public Boolean visitAdd_measure_command(SibillaScriptParser.Add_measure_commandContext ctx) {
        runtime.addMeasure(getStringContent(ctx.name.getText()));
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitRemove_measure_command(SibillaScriptParser.Remove_measure_commandContext ctx) {
        runtime.removeMeasure(getStringContent(ctx.name.getText()));
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitCwd_command(SibillaScriptParser.Cwd_commandContext ctx) {
        try {
            showMessage(currentDirectory.getCanonicalPath());
            return true;
        } catch (IOException e) {
            showErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean visitCd_command(SibillaScriptParser.Cd_commandContext ctx) {
        File newDir = getFile(getStringContent(ctx.name));
        if (!newDir.isDirectory()) {
            printErrorMessages(List.of(newDir.getAbsolutePath() + " is not a directory!"));
            return false;
        }
        currentDirectory = newDir;

        return true;
    }

    @Override
    public Boolean visitRun_command(SibillaScriptParser.Run_commandContext ctx) {
        String file = getStringContent(ctx.name.getText());
        try {
            executeFile(file);
            return true;
        } catch (IOException e) {
            printErrorMessages(List.of(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitLoad_properties_command(SibillaScriptParser.Load_properties_commandContext ctx) {
        error.println("Command is not yet implemented!");
        return false;
    }

    @Override
    public Boolean visitFormulas_command(SibillaScriptParser.Formulas_commandContext ctx) {
        error.println("Command is not yet implemented!");
        return false;
    }

    @Override
    public Boolean visitCheck_command(SibillaScriptParser.Check_commandContext ctx) {
        error.println("Command is not yet implemented!");
        return false;
    }

    @Override
    public Boolean visitSave_command(SibillaScriptParser.Save_commandContext ctx) {
        try {
            runtime.save((ctx.name == null ? null : ctx.name.getText()), getStringContent(ctx.dir), getStringContent(ctx.prefix), getStringContent(ctx.postfix));
            return true;
        } catch (IOException e) {
            printErrorMessages(List.of(e.getMessage()));
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    private String getStringContent(Token token) {
        if (token == null) {
            return null;
        }
        return getStringContent(token.getText());
    }

    @Override
    public Boolean visitSimulate_command(SibillaScriptParser.Simulate_commandContext ctx) {
        ShellSimulationMonitor monitor = null;
        if (isInteractive) {
            monitor = new ShellSimulationMonitor(output);
        }
        try {
            runtime.simulate(monitor, (ctx.label == null ? null : ctx.label.getText()));
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
            return false;
        }
    }

    @Override
    public Boolean visitQuit_command(SibillaScriptParser.Quit_commandContext ctx) {
        terminated = true;
        showMessage(GOODBYE_MESSAGE);
        return true;
    }

    public boolean isRunning() {
        return !terminated;
    }

    @Override
    public Boolean visitStates_command(SibillaScriptParser.States_commandContext ctx) {
        output.println("Configurations:");
        Arrays.stream(runtime.getInitialConfigurations()).forEach(output::println);
        return true;
    }

    @Override
    public Boolean visitAdd_all_measures_command(SibillaScriptParser.Add_all_measures_commandContext ctx) {
        runtime.addAllMeasures();
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitRemove_all_measures_command(SibillaScriptParser.Remove_all_measures_commandContext ctx) {
        runtime.removeAllMeasures();
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitShow_statistics(SibillaScriptParser.Show_statisticsContext ctx) {
        showMessage(this.runtime.getStatistics());
        return true;
    }

    @Override
    public Boolean visitSummary_statistics(SibillaScriptParser.Summary_statisticsContext ctx) {
        this.runtime.useSummaryStatistics();
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitDescriptive_statistics(SibillaScriptParser.Descriptive_statisticsContext ctx) {
        this.runtime.useDescriptiveStatistics();
        showMessage(OK_MESSAGE);
        return true;
    }

    @Override
    public Boolean visitReachability_command(SibillaScriptParser.Reachability_commandContext ctx) {
        ShellSimulationMonitor monitor = null;
        if (isInteractive) {
            monitor = new ShellSimulationMonitor(output);
        }
        String targetPredicate = getStringContent(ctx.goal.getText());
        double alpha = Double.parseDouble(ctx.alpha.getText());
        double delta = Double.parseDouble(ctx.delta.getText());
        try {
            double prob = 0.0;
            if (ctx.condition != null) {
                prob = runtime.computeProbReach(monitor, getStringContent(ctx.condition.getText()), targetPredicate, alpha, delta);
            } else {
                prob = runtime.computeProbReach(monitor, targetPredicate, alpha, delta);
            }
            showMessage("\nProbability: "+prob);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
            return false;
        }
    }

    @Override
    public Boolean visitSet_optimization_strategy(SibillaScriptParser.Set_optimization_strategyContext ctx){
        try {
            runtime.setOptimizationStrategy(getStringContent(ctx.algorithm_name));
            if(ctx.surrogate_name == null)
                runtime.usingSurrogate(false);
            else{
                runtime.usingSurrogate(true);
                runtime.setSurrogateStrategy(getStringContent(ctx.surrogate_name));
            }
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;

    }


    @Override
    public Boolean visitSet_optimization_properties(SibillaScriptParser.Set_optimization_propertiesContext ctx){
        try {
            runtime.setOptimizationProperty(getStringContent(ctx.key),getStringContent(ctx.value));
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        } catch (IllegalArgumentException e){
            printErrorMessages(Collections.singletonList(String.format(e.getMessage(), getStringContent(ctx.key))));
        }
        return false;

    }

    @Override
    public Boolean visitSet_surrogate_properties(SibillaScriptParser.Set_surrogate_propertiesContext ctx){
        try {
            runtime.setSurrogateProperty(getStringContent(ctx.key),getStringContent(ctx.value));
            showMessage(OK_MESSAGE);
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    @Override
    public Boolean visitSearch_space_interval(SibillaScriptParser.Search_space_intervalContext ctx) {
        try {
            runtime.addSpaceInterval(
                    getStringContent(ctx.variable),
                    new ArithmeticExpressionVisitor().visit(ctx.lower_bound).applyAsDouble(null),
                    new ArithmeticExpressionVisitor().visit(ctx.upper_bound).applyAsDouble(null)
            );
            showMessage(OK_MESSAGE);
            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean visitReset_optimization_command(SibillaScriptParser.Reset_optimization_commandContext ctx) {
        return super.visitReset_optimization_command(ctx);
    }

    @Override
    public Boolean visitConstraints_definition(SibillaScriptParser.Constraints_definitionContext ctx) {
        try {
            runtime.addConstraint(new BooleanExpressionVisitor(new ArithmeticExpressionVisitor()).visit(ctx.constraint));
            showMessage(OK_MESSAGE);
            return true;
        } catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

//    @Override
//    public Boolean visitTraining_set_setting(SibillaScriptParser.Training_set_settingContext ctx) {
//        try {
//            runtime.setDataSetSize(Integer.parseInt(ctx.training_set_size.getText()));
//            Optional<String> samplingNameOptional = Optional.ofNullable(getStringContent(ctx.sampling_strategy_name));
//            samplingNameOptional.ifPresent(runtime::setSamplingStrategy);
//            showMessage(OK_MESSAGE);
//            return true;
//        } catch (Exception e){
//            showErrorMessage(e.getMessage());
//        }
//        return false;
//    }


    @Override
    public Boolean visitDataset_setting(SibillaScriptParser.Dataset_settingContext ctx) {
        try {
            runtime.setDataSetSize(Integer.parseInt(ctx.dataset_size.getText()));
            Optional<String> samplingNameOptional = Optional.ofNullable(getStringContent(ctx.sampling_strategy_name));
            //TODO
            //if (ctx.test_percentage != null) runtime.setTrainingSetProportion(ctx.sampling_strategy_name);
            Optional<Double> testPercentageOptional = Optional.of(Double.parseDouble(ctx.test_percentage.getText()));
            testPercentageOptional.ifPresent(aDouble -> runtime.setTrainingSetProportion(1 - aDouble));
            samplingNameOptional.ifPresent(runtime::setSamplingStrategy);
            showMessage(OK_MESSAGE);
            return true;
        } catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean visitObjective_expr(SibillaScriptParser.Objective_exprContext ctx) {
        try {
            runtime.setObjectiveFunction(new ArithmeticExpressionVisitor().visit(ctx.expression));
            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }


    @Override
    public Boolean visitObjective_first_passage_time(SibillaScriptParser.Objective_first_passage_timeContext ctx) {
        try {
            String name = getStringContent(ctx.name);
            runtime.setFirstPassageTimeAsObjectiveFunction(null,name);
            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
     }

    @Override
    public Boolean visitObjective_reachability(SibillaScriptParser.Objective_reachabilityContext ctx) {

        try {
            String targetPredicate = getStringContent(ctx.goal.getText());
            double alpha = ctx.alpha != null ? Double.parseDouble(ctx.alpha.getText()) : 0.01;
            double delta = ctx.delta != null ? Double.parseDouble(ctx.delta.getText()) : 0.01;

            if (ctx.condition != null) {
                runtime.setProbReachAsObjectiveFunction(null,targetPredicate,alpha,delta);
            } else {
                runtime.setProbReachAsObjectiveFunctionWithCondition(null,getStringContent(ctx.condition.getText()),targetPredicate,alpha,delta);
            }
            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean visitOptimization_command(SibillaScriptParser.Optimization_commandContext ctx) {
        try {
            runtime.setOptimizationAsMinimization(ctx.kind_of_optimization.getText().equals("min") || ctx.kind_of_optimization.getText().equals("minimize"));
            this.visitObjective_function(ctx.objective_function());
            showMessage("Processing...\n");
            runtime.performOptimization();
            showMessage(runtime.getOptimizationInfo());

            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean visitSample_command(SibillaScriptParser.Sample_commandContext ctx) {
        try {
            if(ctx.sampling_strategy.getText() != null)
                runtime.setSamplingStrategy(ctx.sampling_strategy.getText());
            if(ctx.number_of_samples.getText() != null)
                runtime.setDataSetSize(Integer.parseInt(ctx.number_of_samples.getText()));
            runtime.generateTrainingSet();
            String messageStringBuilder = "Training set generated : \n"+runtime.getTrainingSetInfo();
            showMessage(messageStringBuilder);
            return true;
        }catch (Exception e){
            showErrorMessage(e.getMessage());
        }
        return false;
    }

    @Override
    public Boolean visitSave_samples_command(SibillaScriptParser.Save_samples_commandContext ctx) {
        try {
            runtime.saveTable((ctx.name == null ? null : ctx.name.getText()), getStringContent(ctx.dir), getStringContent(ctx.prefix), getStringContent(ctx.postfix));
            return true;
        } catch (IOException e) {
            printErrorMessages(List.of(e.getMessage()));
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        }
        return false;
    }

    @Override
    public Boolean visitTrace_command(SibillaScriptParser.Trace_commandContext ctx) {
        try {
            if (ctx.input == null) {
                runtime.trace(getStringContent(ctx.output), true);
            } else {
                runtime.trace(getStringContent(ctx.input), getStringContent(ctx.output), true);
            }
            return true;
        } catch (CommandExecutionException e) {
            printErrorMessages(e.getErrorMessages());
        } catch (Exception e) {
            printErrorMessages(List.of(e.getMessage()));
        }
        return false;
    }
}
