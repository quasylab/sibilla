/**
 * 
 */
package quasylab.sibilla.examples.pm.crowds;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.ThreadSimulationManager;
import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
import quasylab.sibilla.core.simulator.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

/**
 * @author loreti
 *
 */
public class Main {

	public static double LAMBDA_S = 1.0;
	public static double P_F = 1.0;
	public static int N = 10; 
	public static PopulationRegistry r = new PopulationRegistry();
	public final static int SAMPLINGS = 100;
	public final static double DEADLINE = 10;
	private final static int TASKS = 5;
	private static final int REPLICA = 1000;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		
		
		for ( int i=0 ; i<N ; i++ ) {
			r.register("A" , i );
		}
		for ( int i=0 ; i<N ; i++ ) {
			r.register("AM" , i );
		}
		
		r.register("M1");
		r.register("M2");
		
		List<PopulationRule> rules = new LinkedList<PopulationRule>();
		
		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"M1->A"+i,
				new Specie[] { new Specie(r.indexOf("A",i)) , new Specie(r.indexOf("M1"))} ,
				new Specie[] { new Specie(r.indexOf("AM",i)) } ,
				s -> LAMBDA_S/N
			));
		}

		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"M2->A"+i,
				new Specie[] { new Specie(r.indexOf("A",i)) , new Specie(r.indexOf("M2"))} ,
				new Specie[] { new Specie(r.indexOf("AM",i)) } ,
				s -> LAMBDA_S/N
			));
		}
		for( int i=0 ; i<N ; i++ ) {
			for( int j=0; j<N ; j++ ) {
				if (i!=j) {
					rules.add( 
						new ReactionRule(
							"A"+i+"->A"+j,
							new Specie[] { new Specie(r.indexOf("AM",i)) , new Specie(r.indexOf("A",j))} ,
							new Specie[] { new Specie(r.indexOf("A",i)) , new Specie(r.indexOf("AM",j))} ,
							s -> P_F*LAMBDA_S/N
						)
					);
				}
			}
		}
		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"A"+i+"->D",
				new Specie[] { new Specie(r.indexOf("AM",i)) } ,
				new Specie[] { new Specie(r.indexOf("A",i)) } ,
				s -> (1-P_F)*LAMBDA_S
			));
		}
		
		

		PopulationModel f = new PopulationModel( 
				initialState(1),//2 per M2
				rules
		); 
		
		List<StatisticSampling<PopulationState>> samplings = new LinkedList<>();
		for( int i=0 ; i<N ; i++ ) {
			int idx = i;
			samplings.add(
				StatisticSampling.measure(
					"AM"+i,
					SAMPLINGS,DEADLINE,
					s -> s.getOccupancy(r.indexOf("AM",idx))
				) 			
			);
		}
		samplings.add(
			StatisticSampling.measure(
				"MESSAGES",
				SAMPLINGS,DEADLINE,
				Main::runningMessages
			) 			
		);
		
		SimulationEnvironment<PopulationModel,PopulationState> sim = 
				new SimulationEnvironment<>( f, new ThreadSimulationManager<>(TASKS));
		
		sim.setSampling(new SamplingCollection<PopulationState>(samplings));

		sim.simulate(REPLICA,DEADLINE);

		for (StatisticSampling<PopulationState> sf : samplings) {
			sf.printTimeSeries(new PrintStream("data/crowds_"+REPLICA+"_"+N+"_"+sf.getName()+"_.data"),';');
		}

	}
	
	public static double runningMessages( PopulationState s ) {
		double sum = s.getOccupancy(r.indexOf("M1"))+s.getOccupancy(r.indexOf("M2"));
		for( int i=0 ; i<N ; i++ ) {
			sum += s.getOccupancy(r.indexOf("AM",i));
		}
		return sum;
	}

	public static PopulationState initialState(int m ) {
		Specie[] population = new Specie[N+1];
		for( int i=0 ; i<N ; i++ ) {
			population[i] = new Specie( r.indexOf("A",i ),1);
		}
		population[N] = new Specie( r.indexOf("M"+m),1);
		return new PopulationState(r.size(),population);
	}
}
