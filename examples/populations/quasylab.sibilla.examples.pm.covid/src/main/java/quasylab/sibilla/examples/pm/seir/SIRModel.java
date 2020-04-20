/**
 * 
 */
package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.ThreadSimulationManager;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class SIRModel {

	public final static int S = 0;
	public final static int I = 1;
	public final static int R = 2;

	public final static int SCALE = 100;
	public final static int INIT_S = 99*SCALE;
	public final static int INIT_I = 1*SCALE;
	public final static int INIT_R = 0*SCALE;
	public final static double N = INIT_S + INIT_I + INIT_R;

	public final static double LAMBDA_MEET = 4;
	public final static double PROB_TRANSMISSION = 0.1;
	public final static double LAMBDA_R = 1 / 15.0;

	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;
	private final static int TASKS = 5;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		PopulationRule rule_S_I = new ReactionRule(
				"S->I",
				new Population[] { new Population(S), new Population(I)} ,
				new Population[] { new Population(I), new Population(I)},
				s -> s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(I)/N));
		
		PopulationRule rule_I_R = new ReactionRule(
				"I->R",
				new Population[] { new Population(I) },
				new Population[] { new Population(R) },
				s -> s.getOccupancy(I)*LAMBDA_R
		);
		
		
		PopulationModel f = new PopulationModel();
		f.addState("initial", initialState());
		f.addRule(rule_S_I);
		f.addRule(rule_I_R);

		StatisticSampling<PopulationState> fsSamp =
				StatisticSampling.measure("Fraction Infected",
						SAMPLINGS, DEADLINE,
						s -> s.getOccupancy(S)/N) ;
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
		SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );

		SamplingFunction<PopulationState> sf = new SamplingCollection<>(fsSamp,fiSamp,frSamp);

		sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE, true);

		fiSamp.printTimeSeries(new PrintStream("data/sir_I_.data"),';');
		frSamp.printTimeSeries(new PrintStream("data/sir_R_.data"),';');
		fsSamp.printTimeSeries(new PrintStream("data/sir_S_.data"),';');
		System.exit(0);
	}
	

	public static PopulationState initialState() {
		return new PopulationState( new int[] { INIT_S, INIT_I, INIT_R } );
	}
}
