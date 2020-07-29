package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.util.LinkedList;
import java.util.List;

public class TierModelRefactored implements ModelDefinition<PopulationState> {

    public static double LAMBDA_S = 10.0;
    public static int H = 5; //altezza del tier
    public static int N = 5; //numero di tier
    public static PopulationRegistry reg = new PopulationRegistry();
    public final static int SAMPLINGS = 500;
    public final static double DEADLINE = 10;
    private final static int TASKS = 5;
    private static final int REPLICA = 1000;

    public static int init_H = 5; //altezza del tier
    public static int init_N = 5; //numero di tier

    public static int TOT = H*N; //numero totale di nodi nel crowd


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
        if (parameters.length != 2) {
            return new PopulationState( new int[] { init_H, init_N } );
        } else {
            return new PopulationState( new int[] {
                    (int) parameters[H],
                    (int) parameters[N] });
        }
    }

    @Override
    public Model<PopulationState> createModel(double... args) {

        reg.register("M1");
        reg.register("M2");

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < H; j++) {
                reg.register("A", i, j);
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < H; j++) {
                reg.register("AM", i, j);
            }
        }

        //inizio regole tier

        List<PopulationRule> rules = new LinkedList<PopulationRule>();

        //regole inserimento nel crowd

        for (int j = 0; j < H; j++) {
            rules.add(new ReactionRule(
                    "M1->A"+0+j,
                    new Population[] { new Population(reg.indexOf("A",0, j)), new Population(reg.indexOf("M1"))} ,
                    new Population[] { new Population(reg.indexOf("AM",0,j))},
                    (t,s) -> LAMBDA_S/H ));
        }

        for (int j = 0; j < H; j++) {
            rules.add(new ReactionRule(
                    "M2->A"+0+j,
                    new Population[] { new Population(reg.indexOf("A",0, j)), new Population(reg.indexOf("M2"))} ,
                    new Population[] { new Population(reg.indexOf("AM",0,j))},
                    (t,s) -> LAMBDA_S/H ));
        }


        //regole movimento nel crowd

        for(int i=0; i<N-1; i++) { //Per ogni tier, tranne l'ultimo
            for(int j=0; j<H; j++) { //per ogni nodo del tier
                for(int k=0; k<H;k++) { //per ogni nodo del tier successivo
                    int l=i+1;
                    rules.add(new ReactionRule(
                            "A"+i+j+"->A"+l+k,
                            new Population[] { new Population(reg.indexOf("AM",i,j)) , new Population(reg.indexOf("A",l,k))} ,
                            new Population[] { new Population(reg.indexOf("A",i,j)) , new Population(reg.indexOf("AM",l,k))} ,
                            (t,s) -> LAMBDA_S/H
                    ));
                }
            }
        }

        for (int i = 0; i < H; i++) {
            int j=N-1;
            rules.add(new ReactionRule(
                    "A"+j+i+"->D",
                    new Population[] { new Population(reg.indexOf("AM",j,i))},
                    new Population[] { new Population(reg.indexOf("A",j,i))},
                    (t,s) -> LAMBDA_S
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
                        TierModelRefactored::runningMessages
                )
        );

        return samplings;
    }

    public static double runningMessages( PopulationState s ) {
        double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
        for( int i=0 ; i<N ; i++ ) {
            for (int j = 0; j < H; j++) {
                sum += s.getOccupancy(reg.indexOf("AM",i,j));
            }

        }
        return sum;
    }

    public static double runningMessages( PopulationState s , int i ) {
        double sum = 0.0;
        for (int j = 0; j < H; j++) {
            sum += s.getOccupancy(reg.indexOf("AM",i,j));
        }
        return sum;
    }


    public static PopulationState initialState(int m ) {
        Population[] population = new Population[N+1];
        for( int i=0 ; i<N ; i++ ) {
            for (int j = 0; j <H; j++) {
                population[i] = new Population( reg.indexOf("A",i,j ),1);
            }

        }
        population[N] = new Population( reg.indexOf("M"+m),1);
        return new PopulationState(reg.size(),population);
    }


}
