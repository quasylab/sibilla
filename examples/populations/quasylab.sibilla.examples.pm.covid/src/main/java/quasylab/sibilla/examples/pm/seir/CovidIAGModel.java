/**
 *
 */
package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class CovidIAGModel {

    public final static int SAMPLINGS = 120;
    public final static double DEADLINE = 120;
    private static final int REPLICA = 10;


    public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
        CovidAGDefinition def = new CovidAGDefinition();
        SimulationEnvironment simulator = new SimulationEnvironment();
        PopulationModel model = def.createModel();
        SamplingFunction<PopulationState> collection = model.getSamplingFunction(SAMPLINGS,DEADLINE/SAMPLINGS);
        simulator.simulate(model,def.state(),collection,REPLICA,DEADLINE);
        collection.printTimeSeries("data","sir_",".data");
    }


}