/**
 * 
 */
package quasylab.sibilla.examples.pm.le;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;


/**
 * @author loreti
 *
 */
public class Main {

	
	public final static int SAMPLINGS = 100;
	public final static double DEADLINE = 600;
	public static final int REPLICA = 1000;
	public final static int TASKS = 5;
	
	
	public static void main(String[] argv) throws FileNotFoundException, InterruptedException {
		LeaderElectionDefinition def = new LeaderElectionDefinition();
		SimulationEnvironment simulator = new SimulationEnvironment();
		SamplingCollection<PopulationState> collection = new SamplingCollection<>();
		collection.add(StatisticSampling.measure("L",SAMPLINGS,DEADLINE,LeaderElectionDefinition::numberOfLeaders));
		collection.add(StatisticSampling.measure("F",SAMPLINGS,DEADLINE,LeaderElectionDefinition::numberOfFollowers));
		collection.add(StatisticSampling.measure("S",SAMPLINGS,DEADLINE,LeaderElectionDefinition::numberOfSupplicants));
		simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
		collection.printTimeSeries("data","le_",".data");
	}
	

}
