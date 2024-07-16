package it.unicam.quasylab.sibilla.tools.synthesis.expression;

import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsBaseVisitor;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsParser;

import java.util.Map;
import java.util.function.Predicate;

public class BooleanExpressionVisitor extends SynthesisExpressionsBaseVisitor<Predicate<Map<String,Double>>> {

    private final ArithmeticExpressionVisitor arithmeticEvaluator;

    public BooleanExpressionVisitor(){
        this.arithmeticEvaluator = new ArithmeticExpressionVisitor();
    }

    public BooleanExpressionVisitor(ArithmeticExpressionVisitor arithmeticExpressionVisitor){
        this.arithmeticEvaluator = arithmeticExpressionVisitor;
    }

    @Override
    public Predicate<Map<String, Double>> visitExpressionFalse(SynthesisExpressionsParser.ExpressionFalseContext ctx) {
        return map -> false;
    }

    @Override
    public Predicate<Map<String, Double>> visitOrExpression(SynthesisExpressionsParser.OrExpressionContext ctx) {
//        Predicate<Map<String, Double>> left = visit(ctx.left);
//        Predicate<Map<String, Double>> right = visit(ctx.right);
//        return map -> left.test(map) || right.test(map);
        return map -> ctx.left.accept(this).test(map)||ctx.right.accept(this).test(map);
    }

    @Override
    public Predicate<Map<String, Double>> visitIfThenElseExpression(SynthesisExpressionsParser.IfThenElseExpressionContext ctx) {
        Predicate<Map<String, Double>> condition = visit(ctx.guard);
        Predicate<Map<String, Double>> thenBranch = visit(ctx.thenBranch);
        Predicate<Map<String, Double>> elseBranch = visit(ctx.elseBranch);
        return map -> condition.test(map) ? thenBranch.test(map) : elseBranch.test(map);
    }

    @Override
    public Predicate<Map<String, Double>> visitExpressionBracket(SynthesisExpressionsParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Predicate<Map<String, Double>> visitExpressionTrue(SynthesisExpressionsParser.ExpressionTrueContext ctx) {
        return map -> true;
    }

    @Override
    public Predicate<Map<String, Double>> visitAndExpression(SynthesisExpressionsParser.AndExpressionContext ctx) {
//        Predicate<Map<String, Double>> left = visit(ctx.left);
//        Predicate<Map<String, Double>> right = visit(ctx.right);
//        return map -> left.test(map) && right.test(map);
        return map -> ctx.left.accept(this).test(map)&&ctx.right.accept(this).test(map);
    }

    @Override
    public Predicate<Map<String, Double>> visitRelationExpression(SynthesisExpressionsParser.RelationExpressionContext ctx) {
        var left = arithmeticEvaluator.visit(ctx.left);
        var right = arithmeticEvaluator.visit(ctx.right);
        return switch (ctx.op.getText()) {
            case "<" -> map -> left.applyAsDouble(map) < right.applyAsDouble(map);
            case "<=" -> map -> left.applyAsDouble(map) <= right.applyAsDouble(map);
            case "==" -> map -> left.applyAsDouble(map) == right.applyAsDouble(map);
            case "!=" -> map -> left.applyAsDouble(map) != right.applyAsDouble(map);
            case ">=" -> map -> left.applyAsDouble(map) >= right.applyAsDouble(map);
            case ">" -> map -> left.applyAsDouble(map) > right.applyAsDouble(map);
            default -> throw new IllegalArgumentException("Unknown operator: " + ctx.op.getText());
        };
    }

    @Override
    public Predicate<Map<String, Double>> visitNegationExpression(SynthesisExpressionsParser.NegationExpressionContext ctx) {
        Predicate<Map<String, Double>> pred = visit(ctx.argument);
        return map -> pred.negate().test(map);
    }
}
