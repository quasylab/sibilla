package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class STLTest {

     static String TEST_SIR = """
             param meetRate = 1.0;      /* Meeting rate */
             param infectionRate = 0.005;  /* Probability of Infection */
             param recoverRate = 0.005;    /* Recovering rate */

             const startS = 95;           /* Initial number of S agents */
             const startI = 5;           /* Initial number of I agents */
             const startR = 0;            /* Initial number of R agents */

             species S;
             species I;
             species R;

             rule infection {
                 S|I -[ #S * %I * meetRate * infectionRate ]-> I|I
             }

             rule recovered {
                 I -[ #I * recoverRate ]-> R
             }

             system init = S<startS>|I<startI>|R<startR>;
             predicate allRecovered = (#S+#I==0);""";



     @Test
     public void stlTestOnSIR_1() throws ModelGenerationException {

         PopulationModelGenerator pmg = new PopulationModelGenerator(TEST_SIR);
         PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
         pmd.setParameter("infectionRate",new SibillaDouble(0.008));
         PopulationModel pm = pmd.createModel();
         RandomGenerator rg = new DefaultRandomGenerator(1);
         double deadline = 1000;
         PopulationState initialState = new PopulationState(
                 3,
                 new Population(0,95),
                 new Population(1,5),
                 new Population(2,0)
         );
         List<Trajectory<PopulationState>> trajectories = getTrajectories(100,rg,pm,initialState,deadline);
         //trajectories.forEach(System.out::println);
         QuantitativeMonitor<PopulationState> qMonitor = QuantitativeMonitor.eventually(
                 new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) -25),
                 0.0,
                 1000.0
         );
         double robustness = robustnessAtTimeZero(trajectories,qMonitor);
         assertEquals(17.23,robustness);
     }

    @Test
    public void stlTestOnSIR_2() throws ModelGenerationException {

        PopulationModelGenerator pmg = new PopulationModelGenerator(TEST_SIR);
        PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
        pmd.setParameter("infectionRate",new SibillaDouble(0.008));
        PopulationModel pm = pmd.createModel();
        RandomGenerator rg = new DefaultRandomGenerator(1);
        double deadline = 1000;
        PopulationState initialState = new PopulationState(
                3,
                new Population(0,95),
                new Population(1,5),
                new Population(2,0)
        );
        List<Trajectory<PopulationState>> trajectories = getTrajectories(1000,rg,pm,initialState,deadline);
        //trajectories.forEach(System.out::println);
        QuantitativeMonitor<PopulationState> qMonitor = QuantitativeMonitor.eventually(
                new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) -25),
                0.0,
                1000.0
        );
        double robustness = robustnessAtTimeZero(trajectories,qMonitor);
        assertEquals(15.958,robustness);
    }



    static <S extends ImmutableState> List<Trajectory<S>> getTrajectories(int numberOfTrj, RandomGenerator randomGenerator, Model<S> model, S initialState, double deadline){
        List<Trajectory<S>> list = new ArrayList<>();
        SimulationEnvironment se = new SimulationEnvironment();
        for (int i = 0; i < numberOfTrj; i++) {
            Trajectory<S> t = se.sampleTrajectory(randomGenerator,model,initialState,deadline);
            list.add(t);
        }
        return list;
    }

    static <S extends ImmutableState> double robustnessAtTimeZero( List<Trajectory<S>> trajectoryList, QuantitativeMonitor<S> quantitativeMonitor){
         return trajectoryList.stream().mapToDouble(t -> quantitativeMonitor.monitor(t).valueAt(0.0)).average().orElse(0.0);
    }



}
