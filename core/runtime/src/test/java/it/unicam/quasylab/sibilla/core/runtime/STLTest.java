package it.unicam.quasylab.sibilla.core.runtime;


import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class STLTest {

     static String TEST_SIR = " " +
             "param meetRate = 1.0;      /* Meeting rate */ " +
             "param infectionRate = 0.005;  /* Probability of Infection */ "+
             "param recoverRate = 0.005;    /* Recovering rate */ " +

             "const startS = 95;           /* Initial number of S agents */ "+
             "const startI = 5;           /* Initial number of I agents */ "+
             "const startR = 0;            /* Initial number of R agents */ "+

             "species S; " +
             "species I; " +
             "species R; " +

             "rule infection { "+
             "    S|I -[ #S * %I * meetRate * infectionRate ]-> I|I "+
             "}" +

             "rule recovered {" +
             "    I -[ #I * recoverRate ]-> R "+
             "} "+

             "system init = S<startS>|I<startI>|R<startR>; "+
             "predicate allRecovered = (#S+#I==0); ";


     private PopulationModel getPopulationModel() throws ModelGenerationException {
         PopulationModelGenerator pmg = new PopulationModelGenerator(TEST_SIR);
         PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
         pmd.setParameter("infectionRate",new SibillaDouble(0.008));
         PopulationModel pm = pmd.createModel();
         return pm;
     }


     private PopulationState getPopulationState(){
         return new PopulationState(
                 3,
                 new Population(0,95),
                 new Population(1,5),
                 new Population(2,0)
         );
     }

     private  <S extends ImmutableState>  double getRobustness(QuantitativeMonitor<S> mon,SimulationEnvironment se,RandomGenerator rg, Model<S> model, S initialState){
         double timeHorizon = mon.getTimeHorizon();
         Trajectory<S> t = getTrajectory(se,rg,model,initialState,timeHorizon);
         Signal s = mon.monitor(t);

         System.out.println(t);
         System.out.println("ROBUSTNESS : " + s.valueAt(0));
         System.out.println("====================================================================");

         return s.valueAt(0);

     }

     public <S extends ImmutableState>  Trajectory<S> getTrajectory (SimulationEnvironment se, RandomGenerator randomGenerator, Model<S> model, S initialState, double deadline){
         Trajectory<S> t = se.sampleTrajectory(randomGenerator,model,initialState,deadline);
         return t;
     }



     @Test
     @Disabled
     public void stlTestOnSIR_1() throws ModelGenerationException {


         SimulationEnvironment se = new SimulationEnvironment();
         RandomGenerator rg = new DefaultRandomGenerator(1);

         QuantitativeMonitor<PopulationState> qMonitor = QuantitativeMonitor.eventually(
                 new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) -25),
                 0.0,
                 1000.0
         );

         int numberOfSample = 100;
         double robustness = 0.0;

         for (int i = 0; i < numberOfSample; i++) {
             robustness += getRobustness(qMonitor,se,rg,getPopulationModel(),getPopulationState());
         }
         robustness = robustness/numberOfSample;

         assertEquals(17.23,robustness);
     }


}
