package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.Measure;
import quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.util.LinkedList;
import java.util.List;

public class ChordModel extends PopulationModelDefinition  {

    public static double LAMBDA_S = 5.0;
    public static double P_F = 0.75;
    public static int DEFAULT_N = 2;
    public final static int SAMPLINGS = 100;
    public final static double DEADLINE = 10;
    private final static int TASKS = 5;
    private final static int REPLICA = 1000;

    public ChordModel() {
        super();
        setParameter("N",DEFAULT_N);
    }

    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        PopulationRegistry reg = new PopulationRegistry();
        int N = (int) getParameter("N");
        for (int i=0; i<N; i++) {
            reg.register("A", i);
        }

        for (int i=0; i<N; i++) {
            reg.register("AM", i);
        }

        reg.register("M1");
        reg.register("M2");
        return reg;
    }

    @Override
    protected List<PopulationRule> getRules() {
        int N = (int) getParameter("N");
        PopulationRegistry reg = getRegistry();
        List<PopulationRule> rules = new LinkedList<>();

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
        return rules;
    }

    @Override
    protected List<Measure<PopulationState>> getMeasures() {
        int N = (int) getParameter("N");
        PopulationRegistry reg = getRegistry();
        LinkedList<Measure<PopulationState>> toReturn = new LinkedList<>();
        toReturn.add(new SimpleMeasure<>("MESSAGES",s -> runningMessages(N,reg,s)));
        return toReturn;
    }

    @Override
    protected void registerStates() {
        int N = (int) getParameter("N");
        setDefaultStateBuilder(new SimpleStateBuilder<>(0,args -> initialState(N,args)));
    }

    private PopulationState initialState(int N, double ... parameters) {
        PopulationRegistry reg = getRegistry();
        Population[] pop = new Population[N+1];
        pop[0] = new Population(reg.indexOf("M1"),1);
        for( int i=0 ; i<N ; i++) {
            pop[i+1] = new Population(reg.indexOf("A",i),1);
        }
        return new PopulationState(reg.size(),pop);

    }

    public static double runningMessages( int N, PopulationRegistry reg, PopulationState s ) {
        double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
        for( int i=0 ; i<N ; i++ ) {
            sum += s.getOccupancy(reg.indexOf("AM",i));
        }
        return sum;
    }




}
