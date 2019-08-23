/**
 * 
 */
package quasylab.sibilla.examples.pm.seir;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.ThreadSimulationManager;
import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;
import quasylab.sibilla.core.simulator.ui.SimulationView;

/**
 * @author loreti
 *
 */
public class Main {

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
	private final static int TASKS = 15;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		PopulationRule rule_S_E = new ReactionRule(
				"S->E", 
				new Specie[] { new Specie(S), new Specie(I)} , 
				new Specie[] { new Specie(E), new Specie(I)},  
				s -> s.getOccupancy(S)*LAMBDA_E*(s.getOccupancy(I)/N)); 
		
		PopulationRule rule_E_I = new ReactionRule(
				"E->I",
				new Specie[] { new Specie(E) },
				new Specie[] { new Specie(I) },
				s -> s.getOccupancy(E)*LAMBDA_I
		);
		
		PopulationRule rule_I_R = new ReactionRule(
				"I->R",
				new Specie[] { new Specie(I) },
				new Specie[] { new Specie(R) },
				s -> s.getOccupancy(I)*LAMBDA_R
		);
		
		PopulationModel f = new PopulationModel();
		f.addState("initial", initialState());
		f.addRule(rule_S_E);
		f.addRule(rule_E_I);
		f.addRule(rule_I_R); 
		
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
		SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadsimulationManager(TASKS) );

		SamplingFunction<PopulationState> sf = new SamplingCollection<>(fiSamp,frSamp);

		sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE);

		fiSamp.printTimeSeries(new PrintStream("data/seir_"+REPLICA+"_"+N+"_FI_.data"),';');
		frSamp.printTimeSeries(new PrintStream("data/seir_"+REPLICA+"_"+N+"_FR_.data"),';');

	}
	

	public static PopulationState initialState() {
		return new PopulationState( new int[] { INIT_S, INIT_E, INIT_I, INIT_R } );
	}
}
