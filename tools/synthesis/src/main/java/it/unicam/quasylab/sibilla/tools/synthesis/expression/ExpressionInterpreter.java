package it.unicam.quasylab.sibilla.tools.synthesis.expression;

import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsLexer;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The {@code ExpressionInterpreter} class provides methods to parse and interpret
 * arithmetic and boolean expressions. It leverages ANTLR-generated lexer and parser
 * for processing expressions.
 */
public class ExpressionInterpreter {

    /**
     * Parses the given expression string and returns the parse tree.
     *
     * @param expression the expression to parse
     * @return the parse tree representing the parsed expression
     */
    private static ParseTree getParseTree(String expression) {
        CharStream input = CharStreams.fromString(expression);
        SynthesisExpressionsLexer lexer = new SynthesisExpressionsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SynthesisExpressionsParser parser = new SynthesisExpressionsParser(tokens);
        return parser.expression();
    }

    /**
     * Parses an arithmetic expression and returns a function that evaluates the expression
     * based on a given variable context.
     *
     * @param expression the arithmetic expression to parse
     * @return a function that evaluates the parsed expression with a given variable context
     */
    public static ToDoubleFunction<Map<String, Double>> getArithmeticExpression(String expression) {
        ParseTree tree = getParseTree(expression);
        ArithmeticExpressionVisitor visitor = new ArithmeticExpressionVisitor();
        return visitor.visit(tree);
    }

    /**
     * Parses a boolean expression and returns a predicate that evaluates the expression
     * based on a given variable context.
     *
     * @param expression the boolean expression to parse
     * @return a predicate that evaluates the parsed expression with a given variable context
     */
    public static Predicate<Map<String, Double>> getPredicateFromExpression(String expression) {
        ParseTree tree = getParseTree(expression);
        BooleanExpressionVisitor visitor = new BooleanExpressionVisitor();
        return visitor.visit(tree);
    }

    public static List<Predicate<Map<String, Double>>> getPredicatesFromExpression(String... expressions){
        List<Predicate<Map<String, Double>>> predicates = new ArrayList<>();
        for (String expression : expressions) {
            predicates.add(getPredicateFromExpression(expression));
        }
        return predicates;
    }
}
