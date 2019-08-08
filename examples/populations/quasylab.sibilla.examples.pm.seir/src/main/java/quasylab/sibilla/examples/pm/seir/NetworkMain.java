/**
 * 
 */
package quasylab.sibilla.examples.pm.seir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;
import java.io.Serializable;

import quasylab.sibilla.core.simulator.NetworkSimulationManager;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

/**
 * @author loreti
 *
 */
public class NetworkMain {

	public final static int S = 0;
	public final static int E = 1;
	public final static int I = 2;
	public final static int R = 3;

	public final static int INIT_S = 99;
	public final static int INIT_E = 0;
	public final static int INIT_I = 1;
	public final static int INIT_R = 0;
	public final static double N = INIT_S + INIT_E + INIT_I + INIT_R;

	public final static double LAMBDA_E = 1;
	public final static double LAMBDA_I = 1 / 3.0;
	public final static double LAMBDA_R = 1 / 7.0;

	public final static int SAMPLINGS = 100;
	public final static double DEADLINE = 600;
	private static final int REPLICA = 1000;

	public static void main(String[] argv) throws InterruptedException, IOException {
		@SuppressWarnings("unchecked")
		PopulationRule rule_S_E = new ReactionRule(
				"S->E", 
				new Specie[] { new Specie(S), new Specie(I)} , 
				new Specie[] { new Specie(E), new Specie(I)},  
				(Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(S)*LAMBDA_E*(s.getOccupancy(I)/N)); 
		
		@SuppressWarnings("unchecked")
		PopulationRule rule_E_I = new ReactionRule(
				"E->I",
				new Specie[] { new Specie(E) },
				new Specie[] { new Specie(I) },
				(Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(E)*LAMBDA_I
		);
		
		@SuppressWarnings("unchecked")
		PopulationRule rule_I_R = new ReactionRule(
				"I->R",
				new Specie[] { new Specie(I) },
				new Specie[] { new Specie(R) },
				(Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(I)*LAMBDA_R
		);
		
		PopulationModel f = new PopulationModel( 
				initialState(),
				rule_S_E,
				rule_E_I,
				rule_I_R
		); 
		
		StatisticSampling<PopulationState> fiSamp = 
				StatisticSampling.measure("Fraction Infected", 
						SAMPLINGS, DEADLINE, 
						s -> s.getOccupancy(I)/N) ;
		StatisticSampling<PopulationState> frSamp = 
				StatisticSampling.measure("Fraction Recovered", 
						SAMPLINGS, DEADLINE, 
						s -> s.getOccupancy(R)/N) ;
		
//		StatisticSampling<PopulationModel> eSamp = StatisticSampling.measure("#E", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(E)) ;
//		StatisticSampling<PopulationModel> iSamp = StatisticSampling.measure("#I", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(I)) ;
//		StatisticSampling<PopulationModel> rSamp = StatisticSampling.measure("#R", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(R)) ;
		
		// SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f );
		//SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f, new ThreadSimulationManager<>(TASKS) );
		
		
		SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f, new NetworkSimulationManager<>(new InetAddress[]{InetAddress.getByName("192.168.1.64"), InetAddress.getByName("192.168.1.64"), InetAddress.getByName("192.168.1.64")},
																																			new int[]{8080, 8081, 8082},
																																	"quasylab.sibilla.examples.pm.seir.NetworkMain" ));

		/*SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f, new NetworkSimulationManager<>(new InetAddress[]{InetAddress.getByName("192.168.1.64")},
																																	new int[]{8080},
																															"quasylab.sibilla.examples.pm.seir.NetworkMain"));*/																													

		sim.setSampling(new SamplingCollection<>(fiSamp,frSamp));

		sim.simulate(REPLICA,DEADLINE);

		fiSamp.printTimeSeries(new PrintStream("data/seir_"+REPLICA+"_"+N+"_FI_.data"),';');
		frSamp.printTimeSeries(new PrintStream("data/seir_"+REPLICA+"_"+N+"_FR_.data"),';');

	}
	

	public static PopulationState initialState() {
		return new PopulationState( new int[] { INIT_S, INIT_E, INIT_I, INIT_R } );
	}
}
