package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.TimeStep;
import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import it.unicam.quasylab.sibilla.core.tools.glotl.GLoTLStatisticalModelChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.*;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.*;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.yoda.YodaFunction;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerationException;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class YodaExamplesTest {

    private SibillaRuntime getRuntimeWithYodaModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(YodaModelModule.MODULE_NAME);
        return sr;
    }

    private URL getResource(String s) {
        return getClass().getClassLoader().getResource(s);
    }

    private YodaModelDefinition loadModelDefinition(String s) throws YodaModelGenerationException, URISyntaxException, IOException {
        return new YodaModelGenerator(getResource(s)).getYodaModelDefinition();
    }

    private YodaModelGenerator loadModelGenerator(String s) throws YodaModelGenerationException, URISyntaxException, IOException {
        return new YodaModelGenerator(getResource(s));
    }

    @Test
    @Disabled
    public void shouldSelectYODAModule() throws CommandExecutionException{
        SibillaRuntime sr = new SibillaRuntime();
        assertTrue(Arrays.deepEquals(new String[] {
                LIOModelModule.MODULE_NAME, PopulationModelModule.MODULE_NAME, YodaModelModule.MODULE_NAME
        }, sr.getModules()));
        sr.loadModule(YodaModelModule.MODULE_NAME);
    }

    /* BEGIN TESTS ON ROBOT SCENARIO */

    @Test
    @Disabled
    public void shouldLoadResourceRobotScenario() {
        assertNotNull(getResource("yoda/robotAgent.yoda"));
    }

    @Test
    @Disabled
    public void shouldInstantiateAInitialConfigurationFromStringRobotScenario() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }

    @Test
    @Disabled
    public void shouldSimulateRobotScenario() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(1);
        sr.simulate("TestRobotScenario");
        sr.printData("TestRobotScenario");
    }


    @Test
    @Disabled
    public void shouldSimulateRoboticScenarioWithTrace() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.trace(getResource("yoda/robotAgent.trc"), "./results/", true);
    }
    /*
    @Test
    public void shouldSimulateRobotScenarioWithTrace() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        sr.setParameter("no", 5);
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.trace(getResource("yoda/flock.trc"), "./results/", true);
*/
    @Test
    @Disabled
    public void shouldSimulateRobotAndChangeParam() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        sr.setParameter("no", 5);
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(1);
        sr.trace("./results/",true);
        //sr.simulate("TestRobotScenario");
        //sr.printData("TestRobotScenario");
    }

    @Test
    @Disabled
    public void testRobotMeasures() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();
        YodaAgent agent = agentsDefinitions.getAgent(0,elementNameRegistry.get("Robot"), new YodaVariableMapping());

        assertEquals(SibillaValue.of(0), agent.get(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(0), agent.get(variableRegistry.get("diry")));

        agent = agent.next(new DefaultRandomGenerator(), new YodaSystemState(List.of(agent), List.of()));
        assertEquals(SibillaValue.of(0), agent.get(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(1), agent.get(variableRegistry.get("diry")));
    }

    @Test
    @Disabled
    public void testRobotNext() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialAssignment = new YodaVariableMapping();
        initialAssignment = initialAssignment.setValue(variableRegistry.get("y"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("x"), SibillaValue.of(5));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("diry"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("dirx"), SibillaValue.of(0));

        //YodaAgent agent = agentsDefinitions.getAgent(0,elementNameRegistry.get("Robot"), new YodaVariableMapping());
        YodaAgent agent = agentsDefinitions.getAgent(0,elementNameRegistry.get("Robot"), initialAssignment);
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(agent), List.of());

        assertEquals(SibillaValue.of(0), state.get(0).get(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(0), state.get(0).get(variableRegistry.get("diry")));

        state = state.next(rg, 0.0).getValue();
        assertEquals(SibillaValue.of(0), state.get(0).get(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(1), state.get(0).get(variableRegistry.get("diry")));
        assertEquals(SibillaValue.of(5), state.get(0).get(variableRegistry.get("x")));
        assertEquals(SibillaValue.of(1), state.get(0).get(variableRegistry.get("y")));

        double time = 0.0;
        for (int i = 0; i<10; i++){
            TimeStep<YodaSystemState> next = state.next(rg, time);
            state = next.getValue();
            time += next.getTime();
        }
        assertEquals(SibillaValue.of(5), state.get(0).get(variableRegistry.get("x")));
        assertEquals(SibillaValue.of(11), state.get(0).get(variableRegistry.get("y")));
    }

    @Test
    @Disabled
    public void testRobotActions() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialAssignment = new YodaVariableMapping();
        initialAssignment = initialAssignment.setValue(variableRegistry.get("y"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("x"), SibillaValue.of(5));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("diry"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("dirx"), SibillaValue.of(0));

        YodaAgent agent = agentsDefinitions.getAgent(0,elementNameRegistry.get("Robot"), initialAssignment);

        WeightedStructure<YodaAction> actions = agent.getAgentBehaviour().evaluate(agent.getAgentAttributes(), agent.getAgentObservations());

        assertEquals(1, actions.getTotalWeight());
        assertEquals(1, actions.getAll().get(0).getTotalWeight());
        assertEquals("moveNorth", actions.getAll().get(0).getElement().getName());

        /*
        YodaVariableMapping newMap = actions.getAll().get(0).getElement().performAction(new DefaultRandomGenerator(), agent.getAgentAttributes());

        assertEquals(SibillaValue.of(0), newMap.getValue(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(1), newMap.getValue(variableRegistry.get("diry")));*/

    }

    @Test
    @Disabled
    public void testRobotCollision() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialAssignment = new YodaVariableMapping();
        initialAssignment = initialAssignment.setValue(variableRegistry.get("y"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("x"), SibillaValue.of(5));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("diry"), SibillaValue.of(0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("dirx"), SibillaValue.of(0));
        YodaAgent agent = agentsDefinitions.getAgent(0, elementNameRegistry.get("Robot"), initialAssignment);

        YodaVariableMapping initialOb1Assignment = new YodaVariableMapping();
        initialOb1Assignment = initialOb1Assignment.setValue(variableRegistry.get("posx"), SibillaValue.of(5));
        initialOb1Assignment = initialOb1Assignment.setValue(variableRegistry.get("posy"), SibillaValue.of(2));
        YodaSceneElement obs1 = agentsDefinitions.getElement(1, elementNameRegistry.get("Obstacle"), initialOb1Assignment);

        YodaVariableMapping initialOb2Assignment = new YodaVariableMapping();
        initialOb2Assignment = initialOb2Assignment.setValue(variableRegistry.get("posx"), SibillaValue.of(6));
        initialOb2Assignment = initialOb2Assignment.setValue(variableRegistry.get("posy"), SibillaValue.of(1));
        YodaSceneElement obs2 = agentsDefinitions.getElement(2, elementNameRegistry.get("Obstacle"), initialOb2Assignment);

        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(agent), List.of(obs1,obs2));

        double time = 0.0;

        TimeStep<YodaSystemState> next = state.next(rg, time);
        state = next.getValue();
        time += next.getTime();


        YodaVariableMapping observations = state.get(0).observe(rg, state);
        WeightedStructure<YodaAction> acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("moveWest", acts.getAll().get(0).getElement().getName());

        next = state.next(rg, time);
        state = next.getValue();
        time += next.getTime();

        observations = state.get(0).observe(rg, state);
        acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("moveNorth", acts.getAll().get(0).getElement().getName());

        for (int i = 0; i<10;i++){
            next = state.next(rg, time);
            state = next.getValue();
            time += next.getTime();
        }

        observations = state.get(0).observe(rg, state);
        acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("stop", acts.getAll().get(0).getElement().getName());
    }

    @Test
    @Disabled
    public void testFPT() throws URISyntaxException, IOException, CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        int k = 80;
        sr.setParameter("no", k);
        sr.setParameter("na", 20);
        sr.setParameter("width", k);
        sr.setParameter("height", k);



        sr.setConfiguration("Main");
        sr.setDeadline(150);
        sr.setDt(1);
        sr.setReplica(100);
        FirstPassageTime fptRes = sr.firstPassageTime(null, "success");
        System.out.println(fptRes.getMean());
        //assertEquals(true, fptRes.getMean()<100);




    }

    @Test
    @Disabled
    public void testReach() throws URISyntaxException, IOException, CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent.yoda"));
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(100);
        assertEquals(true, sr.computeProbReach(null, "success", 0.1,0.1)>0.5);
    }





    /* END TESTS ON ROBOT SCENARIO */


    /* BEGIN TESTS ON FINDERBOT */

    @Test
    @Disabled
    public void shouldLoadResourceFinderBot() {
        assertNotNull(getResource("yoda/finderBot.yoda"));
    }

    @Test
    @Disabled
    public void shouldInstantiateAInitialConfigurationFromFileFinderBot() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/finderBot.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }


    @Test
    @Disabled
    public void shouldSimulateFinderBot() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/finderBot.yoda"));
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.trace(getResource("yoda/finderBot.trc"),"./results/", true);
        //sr.simulate("FinderBot");
    }

    @Test
    @Disabled
    public void testFinderActionRoam() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/finderBot3.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialAssignment = new YodaVariableMapping();
        initialAssignment = initialAssignment.setValue(variableRegistry.get("dirx"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("diry"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("found"), SibillaValue.of(false));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("x"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("y"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("target_sensor"), SibillaValue.of(false));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("angle"), SibillaValue.of(0.0));
        YodaAgent agent = agentsDefinitions.getAgent(0, elementNameRegistry.get("Finder"), initialAssignment);

        YodaVariableMapping initialTargetAssignment = new YodaVariableMapping();
        initialTargetAssignment = initialTargetAssignment.setValue(variableRegistry.get("posx"), SibillaValue.of(5.0));
        initialTargetAssignment = initialTargetAssignment.setValue(variableRegistry.get("posy"), SibillaValue.of(5.0));
        YodaSceneElement target = agentsDefinitions.getElement(1, elementNameRegistry.get("Target"), initialTargetAssignment);

        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(agent), List.of(target));

        YodaVariableMapping observations = state.get(0).observe(rg, state);
        WeightedStructure<YodaAction> actions = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, actions.getTotalWeight());
        assertEquals(1, actions.getAll().get(0).getTotalWeight());
        assertEquals("moveto", actions.getAll().get(0).getElement().getName());
    }

    @Test
    @Disabled
    public void testFinderActionStop() throws  YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/finderBot3.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialAssignment = new YodaVariableMapping();
        initialAssignment = initialAssignment.setValue(variableRegistry.get("dirx"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("diry"), SibillaValue.of(0.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("found"), SibillaValue.of(false));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("x"), SibillaValue.of(6.0));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("y"), SibillaValue.of(6.5));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("target_sensor"), SibillaValue.of(false));
        initialAssignment = initialAssignment.setValue(variableRegistry.get("angle"), SibillaValue.of(0.0));
        YodaAgent agent = agentsDefinitions.getAgent(0, elementNameRegistry.get("Finder"), initialAssignment);

        YodaVariableMapping initialTargetAssignment = new YodaVariableMapping();
        initialTargetAssignment = initialTargetAssignment.setValue(variableRegistry.get("posx"), SibillaValue.of(5.0));
        initialTargetAssignment = initialTargetAssignment.setValue(variableRegistry.get("posy"), SibillaValue.of(5.0));
        YodaSceneElement target = agentsDefinitions.getElement(1, elementNameRegistry.get("Target"), initialTargetAssignment);

        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(agent), List.of(target));

        YodaVariableMapping observations = state.get(0).observe(rg, state);
        WeightedStructure<YodaAction> actions = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, actions.getTotalWeight());
        assertEquals(1, actions.getAll().get(0).getTotalWeight());
        assertEquals("stop", actions.getAll().get(0).getElement().getName());
    }

    /* END TESTS ON FINDERBOT */

    /* BEGIN TESTS ON FLOCK */

    @Test
    @Disabled
    public void shouldLoadResourceFlock() {
        assertNotNull(getResource("yoda/flock-rh.yoda"));
    }

    @Test
    @Disabled
    public void shouldInstantiateAInitialConfigurationFromFileFlock() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/flock-rh.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }

    @Test
    @Disabled
    public void shouldSimulateFlock() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/flock-rh.yoda"));
        sr.setParameter("nbirds", 500);
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.trace(getResource("yoda/flock.trc"), "./results/", false);
    }

    @Test
    @Disabled
    public void testFlockSeparate() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/flock-rh.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialBird1Assignment = new YodaVariableMapping();
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(80.0));
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(3.0));
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(3.5));
        YodaAgent bird1 = agentsDefinitions.getAgent(0, elementNameRegistry.get("Bird"), initialBird1Assignment);

        YodaVariableMapping initialBird2Assignment = new YodaVariableMapping();
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(85.0));
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(3.0));
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(2.5));
        YodaAgent bird2 = agentsDefinitions.getAgent(1, elementNameRegistry.get("Bird"), initialBird2Assignment);

        YodaVariableMapping initialBird3Assignment = new YodaVariableMapping();
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(75.0));
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(2.5));
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(3.0));
        YodaAgent bird3 = agentsDefinitions.getAgent(2, elementNameRegistry.get("Bird"), initialBird3Assignment);

        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(bird1, bird2, bird3), List.of());

        YodaVariableMapping obs1 = state.get(0).observe(rg, state);
        YodaVariableMapping obs2 = state.get(1).observe(rg, state);
        YodaVariableMapping obs3 = state.get(2).observe(rg, state);
        WeightedStructure<YodaAction> acts1 = state.get(0).getAgentBehaviour().evaluate(bird1.getAgentAttributes(), obs1);
        WeightedStructure<YodaAction> acts2 = state.get(1).getAgentBehaviour().evaluate(bird2.getAgentAttributes(), obs2);
        WeightedStructure<YodaAction> acts3 = state.get(2).getAgentBehaviour().evaluate(bird3.getAgentAttributes(), obs3);

        assertEquals("moveA", acts1.getAll().get(0).getElement().getName());
        assertEquals("moveD", acts2.getAll().get(0).getElement().getName());
        assertEquals("moveC", acts3.getAll().get(0).getElement().getName());
    }

    private static Predicate<YodaAgent> getClosePredicate(double delta,
                                                          YodaVariable x,
                                                          YodaVariable y,
                                                          YodaVariable meanX,
                                                          YodaVariable meanY) {
        return a -> {
            double dx = Math.pow(a.get(x).doubleOf()-a.get(meanX).doubleOf(),2);
            double dy = Math.pow(a.get(y).doubleOf()-a.get(meanY).doubleOf(),2);
            return (Math.sqrt(dx+dy) < 2*delta)&&((Math.sqrt(dx+dy) > delta));
        };
    }

    private static LocalFormula<YodaAgent> getLocalEventuallyClose(
                                                        int ks,
                                                        int k,
                                                        double delta,
                                                        YodaVariable x,
                                                        YodaVariable y,
                                                        YodaVariable meanX,
                                                        YodaVariable meanY) {
        return new LocalEventuallyFormula<>(0, k, new LocalAlwaysFormula<>(0, ks, new LocalAtomicFormula<>(getClosePredicate(delta, x, y, meanX, meanY))));
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getEventuallyCloseFormula(double delta,
                                                                                                    int ks,
                                                                                                    YodaVariableRegistry registry) {
        YodaVariable x = registry.get("x");
        YodaVariable y = registry.get("z");
        YodaVariable meanX = registry.get("meanX");
        YodaVariable meanY = registry.get("meanZ");
        Predicate<YodaAgent> pred = getClosePredicate(delta, x, y, meanX, meanY);
        GlobalFractionOfFormula<YodaAgent, YodaSystemState> close = new GlobalFractionOfFormula<>(
                new LocalAtomicFormula<>(pred),
                p -> p>0.90
        );
        GlobalAlwaysFormula<YodaAgent, YodaSystemState> stable = new GlobalAlwaysFormula<>(0, ks, close);
        return i -> new GlobalEventuallyFormula<>(0, i, stable);
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getEventuallyLocalCloseFormula(double delta,
                                                                                                    int ks,
                                                                                                    int k,
                                                                                                    YodaVariableRegistry registry) {
        YodaVariable x = registry.get("x");
        YodaVariable y = registry.get("z");
        YodaVariable meanX = registry.get("meanX");
        YodaVariable meanY = registry.get("meanZ");
        GlobalFractionOfFormula<YodaAgent, YodaSystemState> close = new GlobalFractionOfFormula<>(
                getLocalEventuallyClose(ks, k, delta, x, y, meanX, meanY),
                p -> p>0.90
        );
        return i -> new GlobalEventuallyFormula<>(0, i, close);
    }


    @Test
    @Disabled
    public void testFlockMonitor()throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/flock-rh.yoda");
        YodaModelDefinition yodaModelDefinition = generator.getYodaModelDefinition();
        YodaVariableRegistry yodaVariableRegistry = generator.getYodaVariableRegistry();
        GLoTLStatisticalModelChecker smc = new GLoTLStatisticalModelChecker();

        DefaultRandomGenerator drg = new DefaultRandomGenerator();
        Function<RandomGenerator, YodaSystemState> initialState;

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(5));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_5 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_5 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(10));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_10 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_10 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(20));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_20 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_20 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(30));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_30 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_30 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(40));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_40 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_40 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));

        yodaModelDefinition.setParameter("nbirds", SibillaValue.of(50));
        initialState = yodaModelDefinition.getDefaultConfiguration();
        System.out.println("phig_50 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyCloseFormula(1, 10, yodaVariableRegistry), 30, 100)));
        System.out.println("phil_50 = "+Arrays.toString(smc.computeProbability((rg, s) -> s.next(rg, 0.0).getValue(), initialState, getEventuallyLocalCloseFormula(1, 10, 10, yodaVariableRegistry), 30, 100)));
    }

    @Test
    @Disabled
    public void testFlockAlign()throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/flock-rh.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        Function<String, Optional<YodaFunction>> functions = generator.generateDeclaredFunctions(env);
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(functions, env.getEvaluator());
        YodaElementNameRegistry elementNameRegistry = generator.getYodaElementNameRegistry();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();

        YodaVariableMapping initialBird1Assignment = new YodaVariableMapping();
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(80.0));
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(5.0));
        initialBird1Assignment = initialBird1Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(5.5));
        YodaAgent bird1 = agentsDefinitions.getAgent(0, elementNameRegistry.get("Bird"), initialBird1Assignment);

        YodaVariableMapping initialBird2Assignment = new YodaVariableMapping();
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(85.0));
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(3.5));
        initialBird2Assignment = initialBird2Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(5.0));
        YodaAgent bird2 = agentsDefinitions.getAgent(1, elementNameRegistry.get("Bird"), initialBird2Assignment);

        YodaVariableMapping initialBird3Assignment = new YodaVariableMapping();
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("angle"), SibillaValue.of(75.0));
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("x"), SibillaValue.of(2.0));
        initialBird3Assignment = initialBird3Assignment.setValue(variableRegistry.get("y"), SibillaValue.of(1.5));
        YodaAgent bird3 = agentsDefinitions.getAgent(2, elementNameRegistry.get("Bird"), initialBird3Assignment);

        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        YodaSystemState state = new YodaSystemState(List.of(bird1, bird2, bird3), List.of());

        YodaVariableMapping obs1 = state.get(0).observe(rg, state);
        YodaVariableMapping obs2 = state.get(1).observe(rg, state);
        YodaVariableMapping obs3 = state.get(2).observe(rg, state);
        WeightedStructure<YodaAction> acts1 = state.get(0).getAgentBehaviour().evaluate(bird1.getAgentAttributes(), obs1);
        WeightedStructure<YodaAction> acts2 = state.get(1).getAgentBehaviour().evaluate(bird2.getAgentAttributes(), obs2);
        WeightedStructure<YodaAction> acts3 = state.get(2).getAgentBehaviour().evaluate(bird3.getAgentAttributes(), obs3);

        assertEquals("moveC", acts1.getAll().get(0).getElement().getName());
        assertEquals("moveD", acts2.getAll().get(0).getElement().getName());
        assertEquals("moveA", acts3.getAll().get(0).getElement().getName());
    }

    /* END TESTS ON FLOCK */


    /* BEGIN TESTS ON SEIR */
/*
    @Test
    @Disabled
    public void shouldSimulateSEIR() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/seir.yoda"));
        sr.setParameter("nSus", 90);
        sr.setParameter("nInf", 10);
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(100);
        //sr.trace(getResource("yoda/seir.trc"),"./results/", false);
        sr.simulate("SEIR");
        sr.save("./results/", "#", "SEIR");
    }

 */

    @Test
    @Disabled
    public void shouldSimulateNewSEIR() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/seir2.yoda"));
        sr.setParameter("nSus", 90);
        sr.setParameter("nInf", 10);
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(100);
//        sr.trace(getResource("yoda/seir.trc"),"./results/", false);
        sr.simulate("SEIR2");
        sr.save("./results/", "#", "SEIR2");
    }

    @Test
    public void testSEIRMonitor() throws IOException, URISyntaxException, YodaModelGenerationException {
        YodaModelGenerator generator = loadModelGenerator("yoda/seir2.yoda");
        YodaModelDefinition definition = generator.getYodaModelDefinition();
        YodaVariableRegistry variableRegistry = generator.getYodaVariableRegistry();
        GLoTLStatisticalModelChecker smc = new GLoTLStatisticalModelChecker();

        DefaultRandomGenerator drg = new DefaultRandomGenerator();
        Function<RandomGenerator, YodaSystemState> initialState;

        definition.setParameter("nSus", SibillaValue.of(90));
        definition.setParameter("nInf", SibillaValue.of(10));
        //definition.setParameter("er", SibillaValue.of(0.05));
        //definition.setParameter("ir", SibillaValue.of(0.03));
        //definition.setParameter("rr", SibillaValue.of(0.001));
        //definition.setParameter("tolerance", SibillaValue.of(0.1));

        initialState=definition.getDefaultConfiguration();

        //System.out.println("Phig_AH_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysAtomicHealthy(variableRegistry), 60, 100)));
        //System.out.println("Phig_GH_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysHealthy(4, variableRegistry), 60, 100)));
        //System.out.println("Phig_DI_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyDoubleInfection( 5, 10, 15, 0.25, variableRegistry), 30, 100)));
/*
        definition.setParameter("nSus", SibillaValue.of(450));
        definition.setParameter("nInf", SibillaValue.of(50));
        initialState=definition.getDefaultConfiguration();

        System.out.println("Phig_AH_500 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysAtomicHealthy(variableRegistry), 60, 100)));
        System.out.println("Phig_GH_500 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysHealthy(4, variableRegistry), 60, 100)));
        //System.out.println("Phig_DI_500 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyDoubleInfection(2, 4, 6, variableRegistry), 30, 100)));


        definition.setParameter("nSus", SibillaValue.of(900));
        definition.setParameter("nInf", SibillaValue.of(100));
        initialState=definition.getDefaultConfiguration();

        System.out.println("Phig_AH_1000 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysAtomicHealthy(variableRegistry), 60, 100)));
        System.out.println("Phig_GH_1000 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalAlwaysHealthy(4, variableRegistry), 60, 100)));
        //System.out.println("Phig_DI_1000 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyDoubleInfection(2, 4, 6, variableRegistry), 30, 100)));


 */
        System.out.println("Phi_gh_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getFormulaPhi_gh(variableRegistry), 60, 100)));
        System.out.println("Phi_glh_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getFormulaPhi_glh(variableRegistry, 5), 60, 100)));
        System.out.println("Phig_gdi_100 = "+Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getFormulaPhi_gdi( variableRegistry, 2,  4, 60), 60, 100)));
    }

    private static Predicate<YodaAgent> getSPredicate(YodaVariable exposed, YodaVariable infected){
        return a -> {
            boolean isExposed = a.get(exposed).booleanOf();
            boolean isInfected = a.get(infected).booleanOf();
            return !isExposed && !isInfected;
        };
    }

    private static Predicate<YodaAgent> getIsHealtyPredicate(YodaVariable state){
        return a -> {
            int s = a.get(state).intOf();
            return (s == 0)||(s == 3);
        };
    }

    private static Predicate<YodaAgent> getIsInfectedPredicate(YodaVariable state){
        return a -> {
            int s = a.get(state).intOf();
            return (s == 2);
        };
    }


    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getFormulaPhi_gh(YodaVariableRegistry registry){
        YodaVariable state = registry.get("status");
        return i -> {
            if (i==0) {
                return new GlobalFractionOfFormula<>(
                        new LocalAtomicFormula<>(getIsHealtyPredicate(state)),
                        p -> p>0.5
                );
            } else {
                return new GlobalNextFormula<>(i,
                        new GlobalFractionOfFormula<>(
                                new LocalAtomicFormula<>(getIsHealtyPredicate(state)),
                                p -> p>0.5
                        ));
            }
        };
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getFormulaPhi_glh(YodaVariableRegistry registry, int k){
        YodaVariable state = registry.get("status");
        LocalAlwaysFormula<YodaAgent> phi_lh = new LocalAlwaysFormula<>(0, k, new LocalAtomicFormula<>(getIsHealtyPredicate(state)));
        return i -> {
            if (i==0) {
                return new GlobalFractionOfFormula<>(
                        phi_lh,
                        p -> p>0.3
                );
            } else {
                return new GlobalNextFormula<>(i,
                        new GlobalFractionOfFormula<>(
                                phi_lh,
                                p -> p>0.3
                        ));
            }
        };
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getFormulaPhi_gdi(YodaVariableRegistry registry, int k_1, int k_2, int k_3){
        YodaVariable state = registry.get("status");
        LocalFormula<YodaAgent> phi_ldi = new LocalAlwaysFormula<>(0, k_1,
                        new LocalAtomicFormula<>(getIsInfectedPredicate(state))
        );
//        LocalFormula<YodaAgent> phi_ldi = new LocalConjunctionFormula<>(
//                new LocalAtomicFormula<>(getIsInfectedPredicate(state)),
//                new LocalEventuallyFormula<>(0, k_2,
//                        new LocalAtomicFormula<>(getIsHealtyPredicate(state))
//                )
//        );
//        );
        return i -> new GlobalNextFormula<>(i+1,
                new GlobalFractionOfFormula<>(
                        phi_ldi,
                        p -> p<0.25
                ));
    }


        private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getGlobalAlwaysAtomicHealthy(YodaVariableRegistry registry){
        YodaVariable exposed = registry.get("exposed");
        YodaVariable infected = registry.get("infected");

        GlobalFractionOfFormula<YodaAgent, YodaSystemState> healthy = new GlobalFractionOfFormula(
                new LocalAtomicFormula(getSPredicate(exposed,infected)),
                p -> p>0.5
        );

        return i -> new GlobalAlwaysFormula<>(0,i,healthy);

    }

    private static LocalAlwaysFormula<YodaAgent> getLocalAlwaysHealthy(int k, YodaVariable exposed, YodaVariable infected){
        return new LocalAlwaysFormula<>(0, k, new LocalAtomicFormula<>(getSPredicate(exposed, infected)));
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getGlobalAlwaysHealthy(int k1, YodaVariableRegistry registry){
        YodaVariable exposed = registry.get("exposed");
        YodaVariable infected = registry.get("infected");

        GlobalFractionOfFormula<YodaAgent, YodaSystemState> healthyStable = new GlobalFractionOfFormula<>(
                getLocalAlwaysHealthy(k1, exposed, infected),
                p->p>0.5
        );

        return i -> new GlobalAlwaysFormula<>(0,i,healthyStable);
    }

    private static Predicate<YodaAgent> getInfectedPredicate(YodaVariable infected){
        return a -> {
            boolean isInfected = a.get(infected).booleanOf();
            return isInfected;
        };
    }

    private static LocalEventuallyFormula<YodaAgent> getLocalEventuallyInfection(int k1, YodaVariable infected){
        return new LocalEventuallyFormula<>(0,k1, new LocalAtomicFormula<>(getInfectedPredicate(infected)));
    }

    private static LocalEventuallyFormula<YodaAgent> getLocalEventuallyHealthy(int k1, YodaVariable exposed, YodaVariable infected){
        return new LocalEventuallyFormula<>(0, k1, new LocalAtomicFormula<>(getSPredicate(exposed, infected)));
    }

    private static LocalEventuallyFormula<YodaAgent> getLocalEventuallySuccessiveInfection(int k1, int k2, YodaVariable exposed, YodaVariable infected){
        LocalEventuallyFormula<YodaAgent> secondInfection = new LocalEventuallyFormula<>(0,k2, new LocalAtomicFormula<>(getInfectedPredicate(infected)));
        LocalAtomicFormula<YodaAgent> healthy = new LocalAtomicFormula<>(getSPredicate(exposed, infected));
        return new LocalEventuallyFormula<>(0,k1, new LocalConjunctionFormula<>(healthy, secondInfection));
    }

    private static LocalEventuallyFormula<YodaAgent> getLocalDoubleInfection(int k, int k1, int k2, YodaVariable exposed, YodaVariable infected){
        LocalEventuallyFormula<YodaAgent> successiveInfection = getLocalEventuallySuccessiveInfection(k1, k2, exposed, infected);
        LocalAtomicFormula<YodaAgent> firstInfection = new LocalAtomicFormula<>(getInfectedPredicate(infected));
        return new LocalEventuallyFormula<>(0, k, new LocalConjunctionFormula<>(firstInfection, successiveInfection));
    }

    /*

    private static LocalEventuallyFormula<YodaAgent> getLocalDoubleInfection(int k0, int k1, int k2, int k3, YodaVariable exposed, YodaVariable infected){
        return new LocalEventuallyFormula<>(
                0, k1, new LocalConjunctionFormula<>(
                        new LocalAtomicFormula<>(getInfectedPredicate(infected)), new LocalEventuallyFormula<>(
                                0, k2, new LocalConjunctionFormula<>(
                                        new LocalAtomicFormula<>(getSPredicate(exposed,infected)), new LocalEventuallyFormula<>(
                                                0, k3, new LocalAtomicFormula<>(getInfectedPredicate(infected))
        )))));
    }


     */



    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getGlobalEventuallyDoubleInfection( int k1, int k2, int k3, double prob, YodaVariableRegistry registry){
        YodaVariable exposed = registry.get("exposed");
        YodaVariable infected = registry.get("infected");

        GlobalFractionOfFormula<YodaAgent, YodaSystemState> doubleInfection = new GlobalFractionOfFormula<>(
                getLocalDoubleInfection(k1,k2,k3,exposed,infected),
                p -> p<prob
        );

        return i -> new GlobalEventuallyFormula<>(0,i,doubleInfection);
    }


    /* END TESTS ON SEIR */

    /* BEGIN TESTS ON RB */
    @Test
    @Disabled
    public void shouldSimulateRedBlue() throws CommandExecutionException, IOException, URISyntaxException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/redblue.yoda"));
        sr.setConfiguration("Main");
        sr.setParameter("nRed", 70);
        sr.setParameter("nBlue", 30);
        sr.setDeadline(100);
        sr.trace(getResource("yoda/redblue.trc"),"./results/", false);
        //sr.simulate("FinderBot");
    }

    private static Predicate<YodaAgent> getRedPredicate(YodaVariable red){
        return a -> {
            boolean isRed = a.get(red).booleanOf();
            return isRed;
        };
    }

    private static Predicate<YodaAgent> getBluePredicate(YodaVariable blue){
        return a -> {
            boolean isBlue = a.get(blue).booleanOf();
            return isBlue;
        };
    }

    private static IntFunction<GlobalFormula<YodaAgent, YodaSystemState>> getGlobalEventuallyRed(int k2, YodaVariableRegistry registry){
        YodaVariable red = registry.get("redParty");
        Predicate<YodaAgent> pred = getRedPredicate(red);
        GlobalFractionOfFormula<YodaAgent,YodaSystemState> isRed = new GlobalFractionOfFormula<>(
                new LocalAtomicFormula<>(pred),
                p-> (p>0.4)&&(p<0.6)
        );
        GlobalAlwaysFormula<YodaAgent,YodaSystemState> stableRed = new GlobalAlwaysFormula<>(0,k2,isRed);
        return i -> new GlobalEventuallyFormula<>(0,i,stableRed);
    }

    private static LocalEventuallyFormula<YodaAgent> getLocalEventuallyStable(int k1, int k2, YodaVariable blue, YodaVariable red){
        LocalAlwaysFormula<YodaAgent> isBlue = new LocalAlwaysFormula<>(0,k2,new LocalAtomicFormula<>(getBluePredicate(blue)));
        LocalAlwaysFormula<YodaAgent> isRed = new LocalAlwaysFormula<>(0,k2,new LocalAtomicFormula<>(getRedPredicate(red)));
        return new LocalEventuallyFormula<>(0,k1, new LocalDisjunctionFormula<>(isBlue, isRed));
    }

    private static IntFunction<GlobalFormula<YodaAgent,YodaSystemState>> getGlobalEventuallyBalanced(int k1, int k2, int k3, YodaVariableRegistry registry){
        YodaVariable red = registry.get("redParty");
        YodaVariable blue = registry.get("blueParty");
        GlobalFractionOfFormula<YodaAgent,YodaSystemState> isStable = new GlobalFractionOfFormula<>(
                getLocalEventuallyStable(k1,k2,blue,red),
                p-> p>0.75
        );
        return i -> new GlobalEventuallyFormula<>(0,i,isStable);

    }

    @Test
    @Disabled
    public void testRBMonitor() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/redblue.yoda");
        YodaModelDefinition definition = generator.getYodaModelDefinition();
        YodaVariableRegistry registry = generator.getYodaVariableRegistry();
        GLoTLStatisticalModelChecker smc = new GLoTLStatisticalModelChecker();

        DefaultRandomGenerator rng = new DefaultRandomGenerator();
        Function<RandomGenerator, YodaSystemState> initialState;

        //70 red e 30 blue
        initialState = definition.getDefaultConfiguration();
        System.out.println("Phig_Red_100 = " +Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyRed(1, registry), 30, 100)));
        System.out.println("Phig_Bal_100 = "+ Arrays.toString(smc.computeProbability((r,state)-> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyBalanced(4, 2, 2, registry), 30, 100)));

        definition.setParameter("nRed", SibillaValue.of(350));
        definition.setParameter("nBlue", SibillaValue.of(150));
        initialState = definition.getDefaultConfiguration();
        System.out.println("Phig_Red_500 = " +Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyRed(1, registry), 30, 100)));
        System.out.println("Phig_Bal_500 = "+ Arrays.toString(smc.computeProbability((r,state)-> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyBalanced(4, 2, 2, registry), 30, 100)));

        definition.setParameter("nRed", SibillaValue.of(700));
        definition.setParameter("nBlue", SibillaValue.of(300));
        initialState = definition.getDefaultConfiguration();
        System.out.println("Phig_Red_1000 = " +Arrays.toString(smc.computeProbability((r,state) -> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyRed(1, registry), 30, 100)));
        System.out.println("Phig_Bal_1000 = "+ Arrays.toString(smc.computeProbability((r,state)-> state.next(r, 0.0).getValue(), initialState, getGlobalEventuallyBalanced(4, 2, 2, registry), 30, 100)));

    }
    /* END TESTS ON RB */
}
