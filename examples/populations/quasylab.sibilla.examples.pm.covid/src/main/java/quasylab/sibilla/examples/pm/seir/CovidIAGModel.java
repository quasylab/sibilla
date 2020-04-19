/**
 *
 */
package quasylab.sibilla.examples.pm.seir;

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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class CovidIAGModel {

    public final static int S = 0;
    public final static int A = 1;
    public final static int G = 2;
    public final static int R = 3;
    public final static int D = 4;
    public final static int IG = 5;
    public final static int IA = 6;

    public final static int SCALE = 100;
    public final static int INIT_S = 99*SCALE;
    public final static int INIT_A = 1*SCALE;
    public final static int INIT_G = 0*SCALE;
    public final static int INIT_R = 0*SCALE;
    public final static int INIT_D = 0*SCALE;
    public final static int INIT_IG = 0*SCALE;
    public final static int INIT_IA = 0*SCALE;
    public final static double N = INIT_S + INIT_A + INIT_G + INIT_R + INIT_D+INIT_IG+INIT_IG;

    public final static double LAMBDA_MEET = 4;
    public final static double PROB_TRANSMISSION = 0.1;
    public final static double LAMBDA_R_A = 1 / 7.0;
    public final static double LAMBDA_R_G = 1 / 15.0;
    public final static double LAMBDA_I_G = 1 / 3.0;
    public final static double LAMBDA_I_A = 1 / 5.0;

    public final static int SAMPLINGS = 120;
    public final static double DEADLINE = 120;
    private static final int REPLICA = 10;
    private final static int TASKS = 5;
    private final static double PROB_ASINT = 0.8;
    private final static double PROB_A_G = 0.5;
    private final static double PROB_DEATH = 0.02;


    public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
        PopulationRule rule_S_A_A = new ReactionRule(
                "S->A",
                new Specie[] { new Specie(S), new Specie(A)} ,
                new Specie[] { new Specie(A), new Specie(A)},
                s -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(A)/N));

        PopulationRule rule_S_G_A = new ReactionRule(
                "S->A",
                new Specie[] { new Specie(S), new Specie(A)} ,
                new Specie[] { new Specie(G), new Specie(A)},
                s -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(A)/N));

        PopulationRule rule_S_A_G = new ReactionRule(
                "S->A",
                new Specie[] { new Specie(S), new Specie(G)} ,
                new Specie[] { new Specie(A), new Specie(G)},
                s -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(G)/N));

        PopulationRule rule_S_G_G = new ReactionRule(
                "S->A",
                new Specie[] { new Specie(S), new Specie(G)} ,
                new Specie[] { new Specie(G), new Specie(G)},
                s -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(G)/N));

        PopulationRule rule_A_R = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(A) },
                new Specie[] { new Specie(R) },
                s -> s.getOccupancy(A)*LAMBDA_R_A*(1-PROB_A_G)
        );

        PopulationRule rule_A_G = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(A) },
                new Specie[] { new Specie(G) },
                s -> s.getOccupancy(A)*LAMBDA_R_A*PROB_A_G
        );

        PopulationRule rule_IA_R = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(IA) },
                new Specie[] { new Specie(R) },
                s -> s.getOccupancy(IA)*LAMBDA_R_A*(1-PROB_A_G)
        );

        PopulationRule rule_IA_IG = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(IA) },
                new Specie[] { new Specie(IG) },
                s -> s.getOccupancy(IA)*LAMBDA_R_A*PROB_A_G
        );

        PopulationRule rule_G_IG = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(G) },
                new Specie[] { new Specie(IG) },
                s -> s.getOccupancy(G)*LAMBDA_I_G
        );

        PopulationRule rule_A_IA = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(A) },
                new Specie[] { new Specie(IA) },
                s -> s.getOccupancy(A)*LAMBDA_I_A
        );


        PopulationRule rule_G_R = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(G) },
                new Specie[] { new Specie(R) },
                s -> s.getOccupancy(G)*LAMBDA_R_G*(1-PROB_DEATH)
        );

        PopulationRule rule_G_D = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(G) },
                new Specie[] { new Specie(D) },
                s -> s.getOccupancy(G)*LAMBDA_R_G*PROB_DEATH
        );

        PopulationRule rule_IG_R = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(IG) },
                new Specie[] { new Specie(R) },
                s -> s.getOccupancy(IG)*LAMBDA_R_G*(1-PROB_DEATH)
        );

        PopulationRule rule_IG_D = new ReactionRule(
                "I->R",
                new Specie[] { new Specie(IG) },
                new Specie[] { new Specie(D) },
                s -> s.getOccupancy(IG)*LAMBDA_R_G*PROB_DEATH
        );

        PopulationModel f = new PopulationModel();
        f.addState("initial", initialState());
        f.addRule(rule_S_A_A);
        f.addRule(rule_S_G_A);
        f.addRule(rule_S_A_G);
        f.addRule(rule_S_G_G);
        f.addRule(rule_A_G);
        f.addRule(rule_A_R);
        f.addRule(rule_G_R);
        f.addRule(rule_G_D);
        f.addRule(rule_G_IG);
        f.addRule(rule_IG_R);
        f.addRule(rule_IG_R);
        f.addRule(rule_IA_IG);
        f.addRule(rule_A_IA);
        f.addRule(rule_IA_R);

        StatisticSampling<PopulationState> fsSamp =
                StatisticSampling.measure("Fraction Infected",
                        SAMPLINGS, DEADLINE,
                        s -> s.getOccupancy(S)/N) ;
        StatisticSampling<PopulationState> fagSamp =
                StatisticSampling.measure("Fraction Infected",
                        SAMPLINGS, DEADLINE,
                        s -> (s.getOccupancy(A)+s.getOccupancy(G)+s.getOccupancy(IG)+s.getOccupancy(IA))/N) ;
        StatisticSampling<PopulationState> faSamp =
                StatisticSampling.measure("Fraction Infected",
                        SAMPLINGS, DEADLINE,
                        s -> (s.getOccupancy(A)+s.getOccupancy(IA))/N) ;
        StatisticSampling<PopulationState> fgSamp =
                StatisticSampling.measure("Fraction Infected",
                        SAMPLINGS, DEADLINE,
                        s -> (s.getOccupancy(G)+s.getOccupancy(IG))/N) ;
        StatisticSampling<PopulationState> frSamp =
                StatisticSampling.measure("Fraction Recovered",
                        SAMPLINGS, DEADLINE,
                        s -> s.getOccupancy(R)/N) ;
        StatisticSampling<PopulationState> fdSamp =
                StatisticSampling.measure("Fraction Recovered",
                        SAMPLINGS, DEADLINE,
                        s -> s.getOccupancy(D)/N) ;
        StatisticSampling<PopulationState> figSamp =
                StatisticSampling.measure("Fraction Recovered",
                        SAMPLINGS, DEADLINE,
                        s -> s.getOccupancy(IG)/N) ;
        StatisticSampling<PopulationState> fiaSamp =
                StatisticSampling.measure("Fraction Recovered",
                        SAMPLINGS, DEADLINE,
                        s -> s.getOccupancy(IA)/N) ;

//		StatisticSampling<PopulationModel> eSamp = StatisticSampling.measure("#E", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(E)) ;
//		StatisticSampling<PopulationModel> iSamp = StatisticSampling.measure("#I", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(I)) ;
//		StatisticSampling<PopulationModel> rSamp = StatisticSampling.measure("#R", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(R)) ;

        // SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f );
        SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );

        SamplingFunction<PopulationState> sf = new SamplingCollection<>(fsSamp,faSamp,fagSamp,fgSamp,fdSamp,frSamp,figSamp,fiaSamp);

        sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE, true);

        faSamp.printTimeSeries(new PrintStream("data/covid_iag_A_.data"),';');
        fagSamp.printTimeSeries(new PrintStream("data/covid_iag_AG_.data"),';');
        fgSamp.printTimeSeries(new PrintStream("data/covid_iag_G_.data"),';');
        frSamp.printTimeSeries(new PrintStream("data/covid_iag_R_.data"),';');
        fsSamp.printTimeSeries(new PrintStream("data/covid_iag_S_.data"),';');
        fdSamp.printTimeSeries(new PrintStream("data/covid_iag_D_.data"),';');
        figSamp.printTimeSeries(new PrintStream("data/covid_iag_IG_.data"),';');
        fiaSamp.printTimeSeries(new PrintStream("data/covid_iag_IA_.data"),';');
        System.exit(0);
    }


    public static PopulationState initialState() {
        return new PopulationState( new int[] { INIT_S, INIT_A, INIT_G, INIT_R, INIT_D, INIT_IG , INIT_IA } );
    }
}