/**
 *
 */
package it.unicam.quasylab.sibilla.examples.pm.seir;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class CovidIGModel {



    public final static int SAMPLINGS = 120;
    public final static double DEADLINE = 120;
    private static final int REPLICA = 10;
    private final static int TASKS = 5;


    public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {

        CovidAGDefinition def = new CovidAGDefinition();
        SimulationEnvironment simulator = new SimulationEnvironment();
        Model<PopulationState> model = def.createModel();
        SamplingFunction<PopulationState> collection = model.selectSamplingFunction(SAMPLINGS,DEADLINE/SAMPLINGS);
        simulator.simulate(model,def.state(),collection,REPLICA,DEADLINE);
        collection.printTimeSeries("data","sir_",".data");
    }


}
