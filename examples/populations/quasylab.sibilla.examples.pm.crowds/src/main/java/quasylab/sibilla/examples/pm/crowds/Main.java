/**
 * 
 */
package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author loreti
 *
 */
public class Main {
//TODO: Add this example again!
//	public static double LAMBDA_S = 1.0;
//	public static double P_F = 1.0;
//	public static int N = 10;
//	public static PopulationRegistry r = new PopulationRegistry();
//	public final static int SAMPLINGS = 100;
//	public final static double DEADLINE = 10;
//	private final static int TASKS = 5;
//	private static final int REPLICA = 1000;
//
//	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
//
//
//		for ( int i=0 ; i<N ; i++ ) {
//			r.register("A" , i );
//		}
//		for ( int i=0 ; i<N ; i++ ) {
//			r.register("AM" , i );
//		}
//
//		r.register("M1");
//		r.register("M2");
//
//		List<PopulationRule> rules = new LinkedList<PopulationRule>();
//
//		for( int i=0 ; i<N ; i++ ) {
//			rules.add( new ReactionRule(
//				"M1->A"+i,
//				new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("M1"))} ,
//				new Population[] { new Population(r.indexOf("AM",i)) } ,
//				s -> LAMBDA_S/N
//			));
//		}
//
//		for( int i=0 ; i<N ; i++ ) {
//			rules.add( new ReactionRule(
//				"M2->A"+i,
//				new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("M2"))} ,
//				new Population[] { new Population(r.indexOf("AM",i)) } ,
//				s -> LAMBDA_S/N
//			));
//		}
//		for( int i=0 ; i<N ; i++ ) {
//			for( int j=0; j<N ; j++ ) {
//				if (i!=j) {
//					rules.add(
//						new ReactionRule(
//							"A"+i+"->A"+j,
//							new Population[] { new Population(r.indexOf("AM",i)) , new Population(r.indexOf("A",j))} ,
//							new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("AM",j))} ,
//							s -> P_F*LAMBDA_S/N
//						)
//					);
//				}
//			}
//		}
//		for( int i=0 ; i<N ; i++ ) {
//			rules.add( new ReactionRule(
//				"A"+i+"->D",
//				new Population[] { new Population(r.indexOf("AM",i)) } ,
//				new Population[] { new Population(r.indexOf("A",i)) } ,
//				s -> (1-P_F)*LAMBDA_S
//			));
//		}
//
//
//
//		PopulationModel f = new PopulationModel();
//		//f.addState("init",initialState(1));//2 per M2
//		f.addRules(rules);
//
//		List<StatisticSampling<PopulationState>> samplings = new LinkedList<>();
//		for( int i=0 ; i<N ; i++ ) {
//			int idx = i;
//			samplings.add(
//				StatisticSampling.measure(
//					"AM"+i,
//					SAMPLINGS,DEADLINE,
//					s -> s.getOccupancy(r.indexOf("AM",idx))
//				)
//			);
//		}
//		samplings.add(
//			StatisticSampling.measure(
//				"MESSAGES",
//				SAMPLINGS,DEADLINE,
//				Main::runningMessages
//			)
//		);
//
//		SimulationEnvironment sim =
//				new SimulationEnvironment( );
//
//		SamplingFunction<PopulationState> sf = new SamplingCollection<PopulationState>(samplings);
//
//		sim.simulate(new DefaultRandomGenerator(), f, initialState(1),sf, REPLICA,DEADLINE, true);
//
//		for (StatisticSampling<PopulationState> s : samplings) {
//			s.printTimeSeries(new PrintStream("data/crowds_"+REPLICA+"_"+N+"_"+s.getName()+"_.data"),';');
//		}
//
//	}
//
//	public static double runningMessages( PopulationState s ) {
//		double sum = s.getOccupancy(r.indexOf("M1"))+s.getOccupancy(r.indexOf("M2"));
//		for( int i=0 ; i<N ; i++ ) {
//			sum += s.getOccupancy(r.indexOf("AM",i));
//		}
//		return sum;
//	}
//
//	public static PopulationState initialState(int m ) {
//		Population[] population = new Population[N+1];
//		for( int i=0 ; i<N ; i++ ) {
//			population[i] = new Population( r.indexOf("A",i ),1);
//		}
//		population[N] = new Population( r.indexOf("M"+m),1);
//		return new PopulationState(r.size(),population);
//	}
}
