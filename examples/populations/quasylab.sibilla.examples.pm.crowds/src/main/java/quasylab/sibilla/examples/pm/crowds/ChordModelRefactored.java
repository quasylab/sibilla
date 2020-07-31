package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.util.LinkedList;
import java.util.List;

public class ChordModelRefactored implements ModelDefinition<PopulationState>  {

    public static double LAMBDA_S = 5.0;
    public static double P_F = 0.75;
    public static int N = 10;
    public static PopulationRegistry reg = new PopulationRegistry();
    public final static int SAMPLINGS = 100;
    public final static double DEADLINE = 10;
    private final static int TASKS = 5;
    private final static int REPLICA = 1000;

    @Override
    public int stateArity() {
        return 0;
    }

    @Override
    public int modelArity() {
        return 0;
    }

    @Override
    public PopulationState state(double... parameters) {
        return null;
    }

    @Override
    public Model<PopulationState> createModel(double... args) {

        //Inserimento degli agenti all'interno del registro

        for (int i=0; i<N; i++) {
            reg.register("A", i);
        }

        for (int i=0; i<N; i++) {
            reg.register("AM", i);
        }

        reg.register("M1");
        reg.register("M2");

        //inizio regole chord

        List<PopulationRule> rules = new LinkedList<PopulationRule>();

        //regole inserimento nel crowd

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule(
                    "M1->A"+i,
                    new Population[] { new Population(reg.indexOf("A", i)) , new Population(reg.indexOf("M1"))},
                    new Population[] { new Population(reg.indexOf("AM",i))},
                    ( s , t ) -> LAMBDA_S/N
            ));
        }

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule(
                    "M2->A"+i,
                    new Population[] { new Population(reg.indexOf("A", i)) , new Population(reg.indexOf("M2"))},
                    new Population[] { new Population(reg.indexOf("AM",i))},
                    ( s , t ) -> LAMBDA_S/N
            ));
        }

        //regole movimento nel crowd

        for (int i = 0; i < N; i++) {
            int j = (i+1)%N;
            rules.add(new ReactionRule(
                            "A"+i+"->A"+j,
                            new Population[] { new Population(reg.indexOf("AM",i)) , new Population(reg.indexOf("A",j))},
                            new Population[] { new Population(reg.indexOf("A",i)) , new Population(reg.indexOf("AM",j))} ,
                            ( s , t ) -> P_F*LAMBDA_S
                    )
            );
        }

        //regola arrivo a destinazione

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule(
                    "A"+i+"->D",
                    new Population[] { new Population(reg.indexOf("AM",i)) } ,
                    new Population[] { new Population(reg.indexOf("A",i)) } ,
                    ( s , t ) -> (1-P_F)*LAMBDA_S
            ));


        }

        PopulationModel pModel = new PopulationModel();

        pModel.addRules(rules);

        return pModel;
    }


    public static List<StatisticSampling<PopulationState>> getSamplingList(){

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
                        ChordModelRefactored::runningMessages
                )
        );

        return samplings;
    }

    public static double runningMessages( PopulationState s ) {
        double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
        for( int i=0 ; i<N ; i++ ) {
            sum += s.getOccupancy(reg.indexOf("AM",i));
        }
        return sum;
    }

    public static PopulationState initialState(int m ) {
        Population[] population = new Population[N+1];
        for( int i=0 ; i<N ; i++ ) {
            population[i] = new Population( reg.indexOf("A",i ),1);
        }
        population[N] = new Population( reg.indexOf("M"+m),1);
        return new PopulationState(reg.size(),population);
    }
}
