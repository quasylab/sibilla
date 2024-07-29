package it.unicam.quasylab.sibilla.tools.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.expression.ExpressionInterpreter;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

class SynthesizerTest {

    @Test
    void minimize() {

        HyperRectangle searchSpace = new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        );

        ToDoubleFunction<Map<String,Double>> objFunction = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        //List<Predicate<Map<String,Double>>> constraints = ExpressionInterpreter.getPredicatesFromExpression(" y>0");
        String[] constraints = new String[]{"y>0"};
        Synthesizer s = new Synthesizer(
                "pso",
                "rf",
                "lhs",
                true,
                objFunction,
                searchSpace,
                1000,
                0.9,
                constraints,
                new Properties(),
                true,true);

        s.searchOptimalSolution();
        for(SynthesisRecord r : s.getSynthesisRecords()){
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            System.out.println(r.info(true));
        }


    }
}