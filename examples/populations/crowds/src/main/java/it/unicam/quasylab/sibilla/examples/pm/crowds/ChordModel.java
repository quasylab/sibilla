package it.unicam.quasylab.sibilla.examples.pm.crowds;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChordModel {

    public static double LAMBDA_S = 5.0;
    public static double P_F = 0.75;
    public static int DEFAULT_N = 2;
    public static final int SAMPLINGS = 100;
    public static final double DEADLINE = 10;
    private static final int TASKS = 5;
    private static final int REPLICA = 1000;


    public static PopulationRegistry generatePopulationRegistry(EvaluationEnvironment ee) {
        PopulationRegistry reg = new PopulationRegistry();
        int N = ee.get("N").intOf();
        for (int i = 0; i < N; i++) {
            reg.register("A", i);
        }

        for (int i = 0; i < N; i++) {
            reg.register("AM", i);
        }

        reg.register("M1");
        reg.register("M2");
        return reg;
    }

    public static List<PopulationRule> getRules(EvaluationEnvironment ee, PopulationRegistry reg) {
        int N = ee.get("N").intOf();
        List<PopulationRule> rules = new LinkedList<>();

        // regole inserimento nel crowd

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule("M1->A" + i,
                    new Population[] { new Population(reg.indexOf("A", i)), new Population(reg.indexOf("M1")) },
                    new Population[] { new Population(reg.indexOf("AM", i)) }, (s, t) -> LAMBDA_S / N));
        }

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule("M2->A" + i,
                    new Population[] { new Population(reg.indexOf("A", i)), new Population(reg.indexOf("M2")) },
                    new Population[] { new Population(reg.indexOf("AM", i)) }, (s, t) -> LAMBDA_S / N));
        }

        // regole movimento nel crowd

        for (int i = 0; i < N; i++) {
            int j = (i + 1) % N;
            rules.add(new ReactionRule("A" + i + "->A" + j,
                    new Population[] { new Population(reg.indexOf("AM", i)), new Population(reg.indexOf("A", j)) },
                    new Population[] { new Population(reg.indexOf("A", i)), new Population(reg.indexOf("AM", j)) },
                    (s, t) -> P_F * LAMBDA_S));
        }

        // regola arrivo a destinazione

        for (int i = 0; i < N; i++) {
            rules.add(new ReactionRule("A" + i + "->D", new Population[] { new Population(reg.indexOf("AM", i)) },
                    new Population[] { new Population(reg.indexOf("A", i)) }, (s, t) -> (1 - P_F) * LAMBDA_S));

        }
        return rules;
    }

    public static Map<String,Measure<PopulationState>> getMeasures(EvaluationEnvironment ee, PopulationRegistry reg) {
        int N = ee.get("N").intOf();
        HashMap<String,Measure<PopulationState>> toReturn = new HashMap<>();
        toReturn.put("MESSAGES",new SimpleMeasure<>("MESSAGES", s -> runningMessages(N, reg, s)));
        return toReturn;
    }

    public static ParametricDataSet<Function<RandomGenerator,PopulationState>> states(EvaluationEnvironment ee, PopulationRegistry reg) {
        int N = ee.get("N").intOf();
        Population[] pop = new Population[N + 1];
        pop[0] = new Population(reg.indexOf("M1"), 1);
        for (int i = 0; i < N; i++) {
            pop[i + 1] = new Population(reg.indexOf("A", i), 1);
        }
        return ParametricDataSet.newStateSet(rg -> new PopulationState(reg.size(),pop));

    }

    public static double runningMessages(int N, PopulationRegistry reg, PopulationState s) {
        double sum = s.getOccupancy(reg.indexOf("M1")) + s.getOccupancy(reg.indexOf("M2"));
        for (int i = 0; i < N; i++) {
            sum += s.getOccupancy(reg.indexOf("AM", i));
        }
        return sum;
    }

}
