/**
 * 
 */
package it.unicam.quasylab.sibilla.examples.pm.seir;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import it.unicam.quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class SIRModel  {

	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		SIRModelDefinition def = new SIRModelDefinition();
		SimulationEnvironment simulator = new SimulationEnvironment();
		SamplingCollection<PopulationState> collection = new SamplingCollection<>();
		collection.add(StatisticSampling.measure("S",SAMPLINGS,DEADLINE,s -> s.getFraction(SIRModelDefinition.S)));
		collection.add(StatisticSampling.measure("I",SAMPLINGS,DEADLINE,s -> s.getFraction(SIRModelDefinition.I)));
		collection.add(StatisticSampling.measure("R",SAMPLINGS,DEADLINE,s -> s.getFraction(SIRModelDefinition.R)));
		simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
		collection.printTimeSeries("data","sir_",".data");
	}



}
