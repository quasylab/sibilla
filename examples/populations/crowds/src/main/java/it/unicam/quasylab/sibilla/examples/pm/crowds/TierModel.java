package it.unicam.quasylab.sibilla.examples.pm.crowds;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;

import java.util.LinkedList;
import java.util.List;

public class TierModel {

//    public static double LAMBDA_S = 10.0;
//    public static int H = 5; //altezza del tier
//    public static int N = 5; //numero di tier
//    public final static int SAMPLINGS = 500;
//    public final static double DEADLINE = 10;
//    private final static int TASKS = 5;
//    private static final int REPLICA = 1000;
//
//    public static int init_H = 5; //altezza del tier
//    public static int init_N = 5; //numero di tier
//
//    public static int TOT = H*N; //numero totale di nodi nel crowd
//
//    public TierModel() {
//        super();
//        setParameter("H",H);
//        setParameter("N",N);
//    }
//
//    @Override
//    protected PopulationRegistry generatePopulationRegistry() {
//        PopulationRegistry reg = new PopulationRegistry();
//        int N = (int) getValue("N");
//        int H = (int) getValue( "H");
//
//        reg.register("M1");
//        reg.register("M2");
//
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < H; j++) {
//                reg.register("A", i, j);
//            }
//        }
//
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < H; j++) {
//                reg.register("AM", i, j);
//            }
//        }
//
//        return reg;
//    }
//
//    @Override
//    protected List<PopulationRule> getRules() {
//        PopulationRegistry reg = new PopulationRegistry();
//        int N = (int) getValue("N");
//        int H = (int) getValue( "H");
//        List<PopulationRule> rules = new LinkedList<>();
//
//        for (int j = 0; j < H; j++) {
//            rules.add(new ReactionRule(
//                    "M1->A"+0+j,
//                    new Population[] { new Population(reg.indexOf("A",0, j)), new Population(reg.indexOf("M1"))} ,
//                    new Population[] { new Population(reg.indexOf("AM",0,j))},
//                    (t,s) -> LAMBDA_S/H ));
//        }
//
//        for (int j = 0; j < H; j++) {
//            rules.add(new ReactionRule(
//                    "M2->A"+0+j,
//                    new Population[] { new Population(reg.indexOf("A",0, j)), new Population(reg.indexOf("M2"))} ,
//                    new Population[] { new Population(reg.indexOf("AM",0,j))},
//                    (t,s) -> LAMBDA_S/H ));
//        }
//
//
//        //regole movimento nel crowd
//
//        for(int i=0; i<N-1; i++) { //Per ogni tier, tranne l'ultimo
//            for(int j=0; j<H; j++) { //per ogni nodo del tier
//                for(int k=0; k<H;k++) { //per ogni nodo del tier successivo
//                    int l=i+1;
//                    rules.add(new ReactionRule(
//                            "A"+i+j+"->A"+l+k,
//                            new Population[] { new Population(reg.indexOf("AM",i,j)) , new Population(reg.indexOf("A",l,k))} ,
//                            new Population[] { new Population(reg.indexOf("A",i,j)) , new Population(reg.indexOf("AM",l,k))} ,
//                            (t,s) -> LAMBDA_S/H
//                    ));
//                }
//            }
//        }
//
//        for (int i = 0; i < H; i++) {
//            int j=N-1;
//            rules.add(new ReactionRule(
//                    "A"+j+i+"->D",
//                    new Population[] { new Population(reg.indexOf("AM",j,i))},
//                    new Population[] { new Population(reg.indexOf("A",j,i))},
//                    (t,s) -> LAMBDA_S
//            ));
//        }
//        return rules;
//    }
//
//    @Override
//    protected List<Measure<PopulationState>> getMeasures() {
//        int N = (int) getValue("N");
//        int H = (int) getValue("H");
//        PopulationRegistry reg = getRegistry();
//        LinkedList<Measure<PopulationState>> toReturn = new LinkedList<>();
//        toReturn.add(new SimpleMeasure<>("MESSAGES", s -> runningMessages(H,N,reg,s)));
//        return toReturn;
//    }
//
//    @Override
//    protected void registerStates() {
//        setDefaultState(new StateBuilder<>(0, args -> initialState(args)));
//    }
//
//    private PopulationState initialState(double ... parameters) {
//        int N = (int) getValue("N");
//        int H = (int) getValue("H");
//        PopulationRegistry reg = getRegistry();
//        LinkedList<Population> pop = new LinkedList<>();
//        pop.add(new Population(reg.indexOf("M1"),1));
//        for( int i=0 ; i<N ; i++ ) {
//            for (int j = 0; j < H; j++) {
//                pop.add(new Population(reg.indexOf("A", i,j), 1));
//            }
//        }
//        return new PopulationState(reg.size(),pop.toArray(new Population[0]));
//
//    }
//
//    public static double runningMessages( int H, int N, PopulationRegistry reg, PopulationState s ) {
//        double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
//        for( int i=0 ; i<N ; i++ ) {
//            for (int j = 0; j < H; j++) {
//                sum += s.getOccupancy(reg.indexOf("AM", i, j));
//            }
//        }
//        return sum;
//    }


}
