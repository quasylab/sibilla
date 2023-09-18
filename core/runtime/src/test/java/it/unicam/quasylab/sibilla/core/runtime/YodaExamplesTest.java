package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAgentsDefinitions;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaModelDefinition;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerationException;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class YodaExamplesTest {

    public static final String ROBOT =
            "param height = 10;\n" +
                    "param width = 10;\n" +
                    "param no = 10;\n" +
                    "param na = 3;\n" +
                    "\n" +
                    "agent Robot =\n" +
                    "    state:\n" +
                    "        int dirx = 0;\n" +
                    "        int diry = 0;\n" +
                    "    features:\n" +
                    "        int x = 0;\n" +
                    "        int y = 0;\n" +
                    "    observations:\n" +
                    "        bool north = false;\n" +
                    "        bool south = false;\n" +
                    "        bool east = false;\n" +
                    "        bool west = false;\n" +
                    "        bool goal = false;\n" +
                    "    actions :\n" +
                    "        moveNorth [ dirx <- 0; diry <- 1;]\n" +
                    "        moveSouth [ dirx <- 0; diry <- 1;]\n" +
                    "        moveEast [ dirx <- 0; diry <- 1;]\n" +
                    "        moveWest [ dirx <- 0; diry <- 1;]\n" +
                    "        stop [ dirx <- 0; diry <- 0; ]\n" +
                    "    behaviour :\n" +
                    "        when goal -> [ stop: 1 ]\n" +
                    "        orwhen !north -> [ moveNorth: 1 ]\n" +
                    "        orwhen !east -> [ moveEast: 1 ]\n" +
                    "        orwhen !west -> [ moveWest: 1 ]\n" +
                    "        otherwise [ stop: 1 ]\n" +
                    "end\n" +
                    "\n" +
                    "element Obstacle =\n" +
                    "    int x = 0;\n" +
                    "    int y = 0;\n" +
                    "end\n" +
                    "\n" +
                    "environment:\n" +
                    "    sensing :\n" +
                    "        Robot [\n" +
                    "            north <- any Obstacle : (y==it.y+1)&&(x==it.y);\n" +
                    "            south <- any Obstacle : (y==it.y-1)&&(x==it.y);\n" +
                    "            east <- any Obstacle : (y==it.y-1)&&(x==it.y);\n" +
                    "            west <- any Obstacle : (y==it.y-1)&&(x==it.y);\n" +
                    "            goal <- it.y==height+1;\n" +
                    "        ]\n" +
                    "\n" +
                    "    dynamic :\n" +
                    "        Robot [\n" +
                    "           x <- x + dirx;\n" +
                    "           y <- y + diry;\n" +
                    "        ]\n" +
                    " end\n" +
                    "\n" +
                    " configuration Main :\n" +
                    "    for ox sampled distinct 5 time from U[0, width] do\n" +
                    "       Obstacle[ x = ox; y = 10; ]\n" +
                    "    endfor\n" +
                    "    Robot[ x = U[0, 20]; y = 0; ]\n" +
                    " end";


    @Test
    public void shouldSelectYODAModule() throws CommandExecutionException{
        SibillaRuntime sr = new SibillaRuntime();
        assertTrue(Arrays.deepEquals(new String[] {
                LIOModelModule.MODULE_NAME, PopulationModelModule.MODULE_NAME, YodaModelModule.MODULE_NAME
        }, sr.getModules()));
        sr.loadModule(YodaModelModule.MODULE_NAME);
    }

    private SibillaRuntime getRuntimeWithYodaModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(YodaModelModule.MODULE_NAME);
        return sr;
    }

    private URL getResource(String s) {
        return getClass().getClassLoader().getResource(s);
    }
    @Test
    public void shouldLoadSpecificationFromString() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(ROBOT);
    }

    @Test
    public void shouldLoadResource() throws CommandExecutionException {
        assertNotNull(getResource("yoda/robotAgent2.yoda"));
    }

    @Test
    public void shouldInstantiateAInitialConfigurationFromString() throws CommandExecutionException {
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
        sr.setConfiguration("Alone");
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

    private YodaModelDefinition loadModelDefinition(String s) throws YodaModelGenerationException, URISyntaxException, IOException {
        return new YodaModelGenerator(getResource(s)).getYodaModelDefinition();
    }

    @Test
    public void shouldBeAgentRobot() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithYodaModule();
        sr.load(getResource("yoda/robotAgent2.yoda"));
    }

}
