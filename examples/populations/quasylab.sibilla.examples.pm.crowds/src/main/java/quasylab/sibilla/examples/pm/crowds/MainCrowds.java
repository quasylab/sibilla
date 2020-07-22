package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.ThreadSimulationManager;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class MainCrowds {



    public static void main(String[] args) throws FileNotFoundException, InterruptedException {



        //////////////////////////////////////////////////////////
        //
        //
        // TIER MODEL
        //
        //
        //////////////////////////////////////////////////////////

        /*


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
						s -> runningMessages(s, idx)
					)
				);
			for (int j=0; j<H; j++) {
			int jdx = j;
			samplings.add(
				StatisticSampling.measure(
					"AM"+i+j,
					SAMPLINGS,DEADLINE,
					s -> s.getOccupancy(reg.indexOf("AM",idx, jdx))
				)
			);
		}
		}
		samplings.add(
			StatisticSampling.measure(
				"MESSAGES",
				SAMPLINGS,DEADLINE,
				MainTier::runningMessages
			)
		);

		SimulationEnvironment<PopulationModel,PopulationState> sim =
				new SimulationEnvironment<>( f, new ThreadSimulationManager<>(TASKS));

		sim.setSampling(new SamplingCollection<PopulationState>(samplings));

		sim.simulate(REPLICA,DEADLINE);

		for (StatisticSampling<PopulationState> sf : samplings) {
			sf.printTimeSeries(new PrintStream("data/tier/tier_"+REPLICA+"_"+N+"x"+H+"_"+sf.getName()+"_.data"),';');
		}














         */

        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------


        //////////////////////////////////////////////////////////
        //
        //
        // MESH MODEL
        //
        //
        //////////////////////////////////////////////////////////

        /*


        PopulationModel f = new PopulationModel(
				initialState(1),//2 per M2
				rules
		);

		List<StatisticSampling<PopulationState>> samplings = new LinkedList<>();

		for( int i=0 ; i<N ; i++ ) {
			for(int j=0; j<H; j++) {
				int jdx =j;

			int idx = i;
			samplings.add(
				StatisticSampling.measure(
					"AM"+i+j,
					SAMPLINGS,DEADLINE,
					s -> s.getOccupancy(r.indexOf("AM",idx, jdx))
				)
			);
		}}
		samplings.add(
			StatisticSampling.measure(
				"MESSAGES",
				SAMPLINGS,DEADLINE,
				MainMesh::runningMessages
			)
		);

		SimulationEnvironment<PopulationModel,PopulationState> sim =
				new SimulationEnvironment<>( f, new ThreadSimulationManager<>(TASKS));

		sim.setSampling(new SamplingCollection<PopulationState>(samplings));

		sim.simulate(REPLICA,DEADLINE);

		for (StatisticSampling<PopulationState> sf : samplings) {
			sf.printTimeSeries(new PrintStream("data/mesh/mesh_"+REPLICA+"_"+N+"x"+H+"_"+sf.getName()+"_.data"),';');
		}



















         */

        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------------------------------------


        //////////////////////////////////////////////////////////
        //
        //
        // CHORD MODEL
        //
        //
        //////////////////////////////////////////////////////////

        /*

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
					s -> s.getOccupancy(reg.indexOf("AM",idx))
				)
			);
		}
		samplings.add(
			StatisticSampling.measure(
				"MESSAGES",
				SAMPLINGS,DEADLINE,
				MainChord::runningMessages
			)
		);

		SimulationEnvironment<PopulationModel,PopulationState> sim =
				new SimulationEnvironment<>( f, new ThreadSimulationManager<>(TASKS));

		sim.setSampling(new SamplingCollection<PopulationState>(samplings));

		sim.simulate(REPLICA,DEADLINE);

		for (StatisticSampling<PopulationState> sf : samplings) {
			sf.printTimeSeries(new PrintStream("data/chord/chord_"+REPLICA+"_"+N+"_"+sf.getName()+"_.data"),';');
		}


         */



    }




}
