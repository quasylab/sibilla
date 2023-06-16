package it.unicam.quasylab.sibilla.examples.yoda.roboticScenario;

import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.tools.glotl.GLoTLStatisticalModelChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalEventuallyFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFractionOfFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAtomicFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalEventuallyFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class RobotScenarioProperties {

    private static RandomGenerator rg;

    private File outputDir;

    private static List<YodaAgent> AGENTS = new LinkedList<>();
    static {
        AGENTS.add(RobotDefinition.R2D2);
        AGENTS.add(RobotDefinition.CHOPPER);
        AGENTS.add(RobotDefinition.BD1);
    }

    public static void main(String[] args) {
        RobotScenarioProperties robotScenarioProperties = new RobotScenarioProperties(new DefaultRandomGenerator());

        runAndPrint("testphi1_grid1", 100, 100, 200, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 1, 1000, i ->RobotScenarioProperties.getPhi1(i, 0.95));
        runAndPrint("testphi2_grid1", 100, 100, 200, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 1,1000, i ->RobotScenarioProperties.getPhi2(i, 0.95));
        runAndPrint("testphi1_grid2", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 2, 1000, i ->RobotScenarioProperties.getPhi1(i, 0.95));
        runAndPrint("testphi2_grid2", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 2,1000, i ->RobotScenarioProperties.getPhi2(i, 0.95));
        runAndPrint("testphi1_grid3", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 3, 1000, i ->RobotScenarioProperties.getPhi1(i, 0.95));
        runAndPrint("testphi2_grid3", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 3,1000, i ->RobotScenarioProperties.getPhi2(i, 0.25));
        runAndPrint("testphi1_grid3b", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.FOUR_DIRECTIONS), 3, 1000, i ->RobotScenarioProperties.getPhi1(i, 0.35));
        runAndPrint("testphi2_grid3b", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.FOUR_DIRECTIONS), 3,1000, i ->RobotScenarioProperties.getPhi2(i, 0.35));

    }



    public RobotScenarioProperties(DefaultRandomGenerator rg) {
        this.rg = rg;
        this.outputDir = new File("./dataProperties");
        if (!this.outputDir.exists()) {
            this.outputDir.mkdir();
        }

    }

    private static void runAndPrint(String label,
                                    int width,
                                    int height,
                                    int numberOfObstacles,
                                    List<YodaAgent> agents,
                                    int gridType,
                                    int replica,
                                    IntFunction<GlobalFormula<YodaAgent, YodaSystemState<Grid>>> formulaBuilder) {
        RobotScenarioDefinition def = new RobotScenarioDefinition(label, width, height, numberOfObstacles, agents, rg);
        def.initialiseScene(gridType);
        YodaModel<Grid> model = def.getYodaModel();
        GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();
        YodaSystemState<Grid> initial = def.getState();
        long start = System.currentTimeMillis();
        double[] values = modelChecker.computeProbability(model, initial, formulaBuilder , 200, replica);
        long end = System.currentTimeMillis();
        System.out.println(start);
        System.out.println(Arrays.toString(values));
        System.out.println(end);
    }

    public static LocalFormula<YodaAgent> goalReachedFormula() {
        return new LocalAtomicFormula<>(a ->
                a.getAgentObservations().getValue(RobotObservation.GOAL_VAR).equals(YodaValue.TRUE)
        );
    }

    public static LocalFormula<YodaAgent> getPhiGoal(int k) {
        return new LocalEventuallyFormula<>(0, k, goalReachedFormula());
    }

    public static GlobalFormula<YodaAgent, YodaSystemState<Grid>> getPhi1(int k, double eps) {
        return new GlobalFractionOfFormula<>(getPhiGoal(k), d -> d>=eps);
    }

    public static GlobalFormula<YodaAgent, YodaSystemState<Grid>> getPhi2(int k, double eps) {
        return new GlobalEventuallyFormula<>(0, k, new GlobalFractionOfFormula<>(goalReachedFormula(), d -> d>=eps));
    }
}
