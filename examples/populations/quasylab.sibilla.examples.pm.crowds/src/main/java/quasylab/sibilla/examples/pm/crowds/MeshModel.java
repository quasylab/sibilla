package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.Measure;
import quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Nicola DG
 *
 */

public class MeshModel extends PopulationModelDefinition {


    public static double LAMBDA_S=5.0;
    public static double P_F= 0.75;
    public static int H = 4; //altezza della rete.
    public static int N = 4;
    public final static int SAMPLINGS= 500;
    public final static double DEADLINE = 10;
    public final static int TASKS= 5;
    private final static int REPLICA = 1000;

    public MeshModel() {
        super();
        setParameter("H",H);
        setParameter("N",N);
    }

    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        PopulationRegistry reg = new PopulationRegistry();
        int N = (int) getParameter("N");
        int H = (int) getParameter( "H");

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

        return reg;
    }

    @Override
    protected List<PopulationRule> getRules() {
        PopulationRegistry r = new PopulationRegistry();
        int N = (int) getParameter("N");
        int H = (int) getParameter( "H");
        List<PopulationRule> rules = new LinkedList<>();

        rules.add(new ReactionRule(
                "M1->A"+0+0,
                new Population[] {new Population(r.indexOf("A",0,0)), new Population(r.indexOf("M1"))},
                new Population[] {new Population(r.indexOf("AM",0,0))},
                (t,s) -> LAMBDA_S/H
        ));

        rules.add(new ReactionRule(
                "M2->A"+0+H,
                new Population[] {new Population(r.indexOf("A",0,H)), new Population(r.indexOf("M2"))},
                new Population[] {new Population(r.indexOf("AM",0,H))},
                (t,s) -> LAMBDA_S/H
        ));

        //regole movimento nel crowd

        for (int i =0; i<N-1; i++) {
            //Variabili utili
            int k= i+1;
            int q= H-1;
            int w= H-2;
            //regole nodo basso, esclusa l'ultima colonna

            rules.add(new ReactionRule( //Spostamento avanti
                    "A"+i+0+"->A"+k+0,
                    new Population[] {new Population (r.indexOf("AM",i,0)), new Population (r.indexOf("A",k,0))},
                    new Population[] {new Population (r.indexOf("A",i,0)), new Population (r.indexOf("AM",k,0))},
                    (t,s) -> LAMBDA_S/3
            ));

            rules.add(new ReactionRule( //Spostamento sopra
                    "A"+i+0+"->A"+i+1,
                    new Population[] {new Population (r.indexOf("AM",i,0)), new Population (r.indexOf("A",i,1))},
                    new Population[] {new Population (r.indexOf("A",i,0)), new Population (r.indexOf("AM",i,1))},
                    (t,s) -> LAMBDA_S/3
            ));

            rules.add(new ReactionRule( //Spostamento diagonale sopra
                    "A"+i+0+"->A"+k+1,
                    new Population[] {new Population (r.indexOf("AM",i,0)), new Population (r.indexOf("A",k,1))},
                    new Population[] {new Population (r.indexOf("A",i,0)), new Population (r.indexOf("AM",k,1))},
                    (t,s) -> LAMBDA_S/3
            ));

            //regole nodo alto, esclusa l'ultima colonna

            rules.add(new ReactionRule( //Spostamento avanti
                    "A"+i+q+"->A"+k+q,
                    new Population [] {new Population(r.indexOf("AM",i,q)), new Population (r.indexOf("A",k,q))},
                    new Population [] {new Population(r.indexOf("A",i,q)), new Population (r.indexOf("AM",k,q))},
                    (t,s) -> LAMBDA_S/3
            ));

            rules.add(new ReactionRule( //Spostamento sotto
                    "A"+i+q+"->A"+i+w,
                    new Population [] {new Population(r.indexOf("AM",i,q)), new Population (r.indexOf("A",i,w))},
                    new Population [] {new Population(r.indexOf("A",i,q)), new Population (r.indexOf("AM",i,w))},
                    (t,s) -> LAMBDA_S/3
            ));

            rules.add(new ReactionRule( //Spostamento diagonale sotto
                    "A"+i+q+"->A"+k+w,
                    new Population [] {new Population(r.indexOf("AM",i,q)), new Population (r.indexOf("A",k,w))},
                    new Population [] {new Population(r.indexOf("A",i,q)), new Population (r.indexOf("AM",k,w))},
                    (t,s) -> LAMBDA_S/3
            ));

        }

        for (int i = 0; i < N-1; i++) {
            for (int j = 1; j < H-1; j++) {

                //variabili utili
                int k=i+1;
                int q=j+1;
                int w=j-1;

                rules.add(new ReactionRule( //Spostamento avanti
                        "A"+i+j+"->A"+k+j,
                        new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",k,j))},
                        new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",k,j))},
                        (t,s) -> LAMBDA_S/5
                ));

                rules.add(new ReactionRule( //Spostamento diagonale sopra
                        "A"+i+j+"->A"+k+q,
                        new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",k,q))},
                        new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",k,q))},
                        (t,s) -> LAMBDA_S/5
                ));

                rules.add(new ReactionRule( //Spostamento sopra
                        "A"+i+j+"->A"+i+q,
                        new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",i,q))},
                        new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",i,q))},
                        (t,s) -> LAMBDA_S/5
                ));

                rules.add(new ReactionRule( //Spostamento diagonale sotto
                        "A"+i+j+"->A"+k+w,
                        new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",k,w))},
                        new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",k,w))},
                        (t,s) -> LAMBDA_S/5
                ));

                rules.add(new ReactionRule( //Spostamento sotto
                        "A"+i+j+"->A"+i+w,
                        new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",i,w))},
                        new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",i,w))},
                        (t,s) -> LAMBDA_S/5
                ));

            }
        }

        //regole uscita

        //
        for (int j=1; j<H-1; j++) {
            int i=N-1;
            int q=j-1;
            int w=j+1;

            rules.add(new ReactionRule( //arrivo a destinazione
                    "A"+i+j+"->D",
                    new Population[] {new Population(r.indexOf("AM",i,j))},
                    new Population[] {new Population(r.indexOf("A",i,j))},
                    (t,s) -> LAMBDA_S*(1-P_F)
            ));

            rules.add(new ReactionRule(//Spostamento sotto
                    "A"+i+j+"->A"+i+q,
                    new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",i,q))},
                    new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",i,q))},
                    (t,s) -> (LAMBDA_S*P_F)/2
            ));

            rules.add(new ReactionRule(//Spostamento sopra
                    "A"+i+j+"->A"+i+w,
                    new Population[] {new Population(r.indexOf("AM",i,j)), new Population(r.indexOf("A",i,w))},
                    new Population[] {new Population(r.indexOf("A",i,j)), new Population(r.indexOf("AM",i,w))},
                    (t,s) -> (LAMBDA_S*P_F)/2
            ));
        }

        int z = N-1;
        int x = H-1;
        int c = H-2;

        rules.add(new ReactionRule(
                "A"+z+0+"->D",
                new Population[] {new Population(r.indexOf("AM",z,0))},
                new Population[] {new Population(r.indexOf("A",z,0))},
                (t,s) -> LAMBDA_S*(1-P_F)
        ));

        rules.add(new ReactionRule(
                "A"+z+0+"->A"+z+1,
                new Population[] {new Population(r.indexOf("AM",z,0)), new Population(r.indexOf("A",z,1))},
                new Population[] {new Population(r.indexOf("A",z,0)), new Population(r.indexOf("AM",z,1))},
                (t,s) -> LAMBDA_S*P_F
        ));

        rules.add(new ReactionRule(
                "A"+z+x+"->D",
                new Population[] {new Population(r.indexOf("AM",z,x))},
                new Population[] {new Population(r.indexOf("A",z,x))},
                (t,s) -> LAMBDA_S*(1-P_F)
        ));

        rules.add(new ReactionRule(
                "A"+z+x+"->A"+z+c,
                new Population[] {new Population(r.indexOf("AM",z,x)), new Population(r.indexOf("A",z,c))},
                new Population[] {new Population(r.indexOf("A",z,x)), new Population(r.indexOf("AM",z,c))},
                (t,s) -> LAMBDA_S*P_F
        ));
        return rules;
    }

    @Override
    protected List<Measure<PopulationState>> getMeasures() {
        int N = (int) getParameter("N");
        int H = (int) getParameter("H");
        PopulationRegistry reg = getRegistry();
        LinkedList<Measure<PopulationState>> toReturn = new LinkedList<>();
        toReturn.add(new SimpleMeasure<>("MESSAGES", s -> runningMessages(H,N,reg,s)));
        return toReturn;
    }

    @Override
    protected void registerStates() {
        setDefaultStateBuilder(new SimpleStateBuilder<>(0,args -> initialState(args)));
    }

    private PopulationState initialState(double ... parameters) {
        int N = (int) getParameter("N");
        int H = (int) getParameter("H");
        PopulationRegistry reg = getRegistry();
        LinkedList<Population> pop = new LinkedList<>();
        pop.add(new Population(reg.indexOf("M1"),1));
        for( int i=0 ; i<N ; i++ ) {
            for (int j = 0; j < H; j++) {
                pop.add(new Population(reg.indexOf("A", i,j), 1));
            }
        }
        return new PopulationState(reg.size(),pop.toArray(new Population[0]));

    }

    public static double runningMessages( int H, int N, PopulationRegistry reg, PopulationState s ) {
        double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
        for( int i=0 ; i<N ; i++ ) {
            for (int j = 0; j < H; j++) {
                sum += s.getOccupancy(reg.indexOf("AM", i, j));
            }
        }
        return sum;
    }



}
