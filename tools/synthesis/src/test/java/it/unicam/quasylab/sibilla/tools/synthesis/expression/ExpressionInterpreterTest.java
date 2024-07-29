package it.unicam.quasylab.sibilla.tools.synthesis.expression;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionInterpreterTest {

    @Test
    void testComplexArithmeticExpressions() {
        assertExpressionEquals("2 * (x + 3) - 5 / y", Map.of("x", 2.0, "y", 2.0), 7.5);
        assertExpressionEquals("sin(x) + cos(y) * tan(z)", Map.of("x", Math.PI/2, "y", 0.0, "z", Math.PI/4), 2.0);
        assertExpressionEquals("log(x) + exp(y) - sqrt(z)", Map.of("x", Math.E, "y", 1.0, "z", 4.0), 1.71828);
        assertExpressionEquals("max(x, y) + min(z, w)", Map.of("x", 1.0, "y", 2.0, "z", 3.0, "w", 4.0), 5.0);
        assertExpressionEquals("abs(x - y) / (z + 1)^2", Map.of("x", 5.0, "y", 2.0, "z", 1.0), 0.75);
    }

    @Test
    void testComplexBooleanExpressions() {
        assertBooleanExpression("x > 0 && y < 10 || z == 5", Map.of("x", 1.0, "y", 5.0, "z", 3.0), true);
        assertBooleanExpression("!(x >= y) && (z <= w || v != 0)", Map.of("x", 2.0, "y", 3.0, "z", 4.0, "w", 4.0, "v", 1.0), true);
        assertBooleanExpression("sin(x) > cos(y) && log(z) <= exp(w)", Map.of("x", Math.PI/2, "y", Math.PI/3, "z", Math.E, "w", 0.0), true);
        assertBooleanExpression("(x + y) * z >= w / v && abs(u - t) < 5", Map.of("x", 1.0, "y", 2.0, "z", 3.0, "w", 6.0, "v", 1.0, "u", 7.0, "t", 3.0), true);
        assertBooleanExpression("max(x, y) == min(z, w) || (a % 2 == 0 && b % 2 != 0)", Map.of("x", 3.0, "y", 4.0, "z", 2.0, "w", 1.0, "a", 4.0, "b", 3.0), true);
        assertBooleanExpression("2<=x && x<=5", Map.of("x", 3.0), true);
        assertBooleanExpression("2<=x && x<=5", Map.of("x", 8.0), false);
        assertBooleanExpression("2<=x && x<=5", Map.of("x", 1.0), false);
    }


    @Test
    void testNegation() {
        assertBooleanExpression("!(x > 5)", Map.of("x", 3.0), true);
        assertBooleanExpression("!(x > 5)", Map.of("x", 7.0), false);
        assertBooleanExpression("!(true)", Map.of(), false);
        assertBooleanExpression("!(false)", Map.of(), true);
    }

    @Test
    void testMixedExpressions()  {
        assertBooleanExpression("x * y > z + w ? a <= b : c != d", Map.of("x", 3.0, "y", 4.0, "z", 5.0, "w", 6.0, "a", 1.0, "b", 2.0, "c", 3.0, "d", 3.0), true);
        assertBooleanExpression("sin(x) > 0 ? log(y) < exp(z) : sqrt(w) >= abs(v)", Map.of("x", Math.PI/4, "y", Math.E, "z", 0.5, "w", 4.0, "v", -3.0), true);
        assertExpressionEquals("x > y ? max(z, w) : min(a, b)", Map.of("x", 5.0, "y", 3.0, "z", 1.0, "w", 2.0, "a", 3.0, "b", 4.0), 2.0);
    }

    private void assertExpressionEquals(String expression, Map<String, Double> variables, double expected){
        ToDoubleFunction<Map<String, Double>> func = ExpressionInterpreter.getArithmeticExpression(expression);
        assertEquals(expected, func.applyAsDouble(variables), 0.0001);
    }

    private void assertBooleanExpression(String expression, Map<String, Double> variables, boolean expected)  {
        Predicate<Map<String, Double>> predicate = ExpressionInterpreter.getPredicateFromExpression(expression);
        assertEquals(expected, predicate.test(variables));
    }
}