/**
 *
 */
package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
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
        SamplingCollection<PopulationState> collection = new SamplingCollection<>();
        collection.add(StatisticSampling.measure(
                "S",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.S)));
        collection.add(StatisticSampling.measure(
                "A",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.A)));
        collection.add(StatisticSampling.measure(
                "G",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.G)+s.getFraction(CovidAGDefinition.IG)));
        collection.add(StatisticSampling.measure(
                "AG",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.G)+s.getFraction(CovidAGDefinition.IG)+s.getFraction(CovidDefinition.A)));
        collection.add(StatisticSampling.measure(
                "R",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.R)));
        collection.add(StatisticSampling.measure(
                "D",SAMPLINGS,DEADLINE,
                s -> s.getFraction(CovidAGDefinition.D)));
        simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
        collection.printTimeSeries("data","sir_",".data");
    }


}