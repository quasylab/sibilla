package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulationEnvironmentTest {
    final String MODEL___SIR =
            """
            param k_i = 0.05;
            param k_r = 0.05;
            
            const startS = 95;           /* Initial number of S agents */
            const startI = 5;           /* Initial number of I agents */
            const startR = 0;            /* Initial number of R agents */
            
            species S;
            species I;
            species R;
            
            rule infection {
                S|I -[ #S * #I * k_i ]-> I|I
            }
            
            rule recovered {
                I -[ #I * k_r ]-> R
            }
                                       
            system init = S<startS>|I<startI>|R<startR>;
            """;

    @Disabled
    @Test
    void testTrajectoriesUnderTheDeadline() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(this.MODEL___SIR);

        PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
        PopulationModel pm = pmd.createModel();

        PopulationState populationState = new PopulationState(
                3,
                new Population(0, 95),
                new Population(1, 5),
                new Population(2, 0)
        );

        double deadline = 10;

        RandomGenerator rg  = new DefaultRandomGenerator(1);
        SimulationEnvironment se = new SimulationEnvironment();

        Trajectory<PopulationState> t = se.sampleTrajectory(rg,pm,populationState, deadline);
        assertTrue(deadline >= t.getData().get(t.getData().size()-1).getTime());
    }
}
