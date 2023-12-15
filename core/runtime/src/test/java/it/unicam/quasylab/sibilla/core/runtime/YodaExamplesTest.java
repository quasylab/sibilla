package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerationException;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
    public void shouldSelectYODAModule() throws CommandExecutionException{
        SibillaRuntime sr = new SibillaRuntime();
        assertTrue(Arrays.deepEquals(new String[] {
                LIOModelModule.MODULE_NAME, PopulationModelModule.MODULE_NAME, YodaModelModule.MODULE_NAME
        }, sr.getModules()));
        sr.loadModule(YodaModelModule.MODULE_NAME);
    }

    /* BEGIN TESTS ON ROBOT SCENARIO */

    @Test
    public void shouldLoadResourceRobotScenario() {
        assertNotNull(getResource("yoda/robotAgent2.yoda"));
    }

    @Test
    public void shouldInstantiateAInitialConfigurationFromStringRobotScenario() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent2.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }

    @Test
    public void shouldSimulateRobotScenario() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent2.yoda"));
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(1);
        sr.simulate("TestRobotScenario");
        sr.printData("TestRobotScenario");
    }

    @Test
    public void shouldSimulateRobotAndChangeParam() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent2.yoda"));
        sr.setParameter("no", 4);
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(1);
        sr.simulate("TestRobotScenario");
        sr.printData("TestRobotScenario");
    }

    @Test
    public void testRobotMeasures() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent2.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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
    public void testRobotNext() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent2.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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

        state = state.next(rg);
        assertEquals(SibillaValue.of(0), state.get(0).get(variableRegistry.get("dirx")));
        assertEquals(SibillaValue.of(1), state.get(0).get(variableRegistry.get("diry")));
        assertEquals(SibillaValue.of(5), state.get(0).get(variableRegistry.get("x")));
        assertEquals(SibillaValue.of(1), state.get(0).get(variableRegistry.get("y")));

        for (int i = 0; i<10; i++){
            state = state.next(rg);
        }
        assertEquals(SibillaValue.of(5), state.get(0).get(variableRegistry.get("x")));
        assertEquals(SibillaValue.of(11), state.get(0).get(variableRegistry.get("y")));
    }

    @Test
    public void testRobotActions() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent2.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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
    public void testRobotCollision() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/robotAgent2.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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

        state = state.next(rg);
        YodaVariableMapping observations = state.get(0).observe(rg, state);
        WeightedStructure<YodaAction> acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("moveWest", acts.getAll().get(0).getElement().getName());

        state = state.next(rg);
        observations = state.get(0).observe(rg, state);
        acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("moveNorth", acts.getAll().get(0).getElement().getName());

        for (int i = 0; i<10;i++){
            state = state.next(rg);
        }
        observations = state.get(0).observe(rg, state);
        acts = state.get(0).getAgentBehaviour().evaluate(agent.getAgentAttributes(), observations);
        assertEquals(1, acts.getTotalWeight());
        assertEquals(1, acts.getAll().get(0).getTotalWeight());
        assertEquals("stop", acts.getAll().get(0).getElement().getName());
    }

    /* END TESTS ON ROBOT SCENARIO */


    /* BEGIN TESTS ON FINDERBOT */

    @Test
    public void shouldLoadResourceFinderBot() {
        assertNotNull(getResource("yoda/finderBot.yoda"));
    }

    @Test
    public void shouldInstantiateAInitialConfigurationFromFileFinderBot() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/finderBot.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }

    @Test
    public void shouldSimulateFinderBot() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/finderBot.yoda"));
        sr.setConfiguration("Main");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.setReplica(1);
        sr.simulate("TestFinderBot");
        sr.printData("TestFinderBot");
    }

    @Test
    @Disabled
    public void testFinderActionRoam() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/finderBot.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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
        YodaModelGenerator generator = loadModelGenerator("yoda/finderBot.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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
    public void shouldLoadResourceFlock() {
        assertNotNull(getResource("yoda/flock.yoda"));
    }

    @Test
    public void shouldInstantiateAInitialConfigurationFromFileFlock() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/flock.yoda"));
        assertEquals(1, sr.getInitialConfigurations().length);
        assertEquals("Main", sr.getInitialConfigurations()[0]);
        sr.setConfiguration("Main");
    }

    @Test
    public void shouldSimulateFlock() throws CommandExecutionException, IOException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/flock.yoda"));
        sr.setParameter("nbirds", 45);
        sr.setConfiguration("Main");
        sr.setDeadline(100);
        sr.trace("TestFlock", true);
        //sr.printData("TestFlock");
    }

    @Test
    @Disabled
    public void testFlockSeparate() throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/flock.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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

    @Test
    @Disabled
    public void testFlockAlign()throws YodaModelGenerationException, URISyntaxException, IOException {
        YodaModelGenerator generator = loadModelGenerator("yoda/flock.yoda");
        generator.getParseTree();
        EvaluationEnvironment env = generator.getEvaluationEnvironment();
        YodaAgentsDefinitions agentsDefinitions = generator.getYodaAgentsDefinitions(env.getEvaluator());
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

}
