package it.unicam.quasylab.sibilla.tools.synthesis.expression;



import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsBaseVisitor;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisExpressionsParser;

import java.util.Map;
import java.util.function.ToDoubleFunction;

public class ArithmeticExpressionVisitor extends SynthesisExpressionsBaseVisitor<ToDoubleFunction<Map<String, Double>>> {



    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionInteger(SynthesisExpressionsParser.ExpressionIntegerContext ctx) {
        return map -> Double.parseDouble(ctx.getText());
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionReal(SynthesisExpressionsParser.ExpressionRealContext ctx) {
        return map -> Double.parseDouble(ctx.getText());
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionInfinity(SynthesisExpressionsParser.ExpressionInfinityContext ctx) {
        return switch (ctx.getText()){
            case "negInf" -> m -> Double.NEGATIVE_INFINITY;
            case "posInf" -> m -> Double.POSITIVE_INFINITY;
            default -> throw new IllegalArgumentException("Unknown: " + ctx.getText() + ". Should be 'negInf' or 'posInf'");
        };
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionPi(SynthesisExpressionsParser.ExpressionPiContext ctx) {
        return m ->  Math.PI;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionE(SynthesisExpressionsParser.ExpressionEContext ctx) {
        return m ->  Math.E;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionACos(SynthesisExpressionsParser.ExpressionACosContext ctx) {
        return map -> Math.acos(Double.parseDouble(ctx.getText()));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionReference(SynthesisExpressionsParser.ExpressionReferenceContext ctx) {
        return map -> map.get(ctx.getText());
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionLog(SynthesisExpressionsParser.ExpressionLogContext ctx) {
        return map -> Math.log(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCos(SynthesisExpressionsParser.ExpressionCosContext ctx) {
        return map -> Math.cos(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionFloor(SynthesisExpressionsParser.ExpressionFloorContext ctx) {
        return map -> Math.floor(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionMin(SynthesisExpressionsParser.ExpressionMinContext ctx) {
        return map -> Math.min(visit(ctx.firstArgument).applyAsDouble(map), visit(ctx.secondArgument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionLog10(SynthesisExpressionsParser.ExpressionLog10Context ctx) {
        return map -> Math.log10(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCosh(SynthesisExpressionsParser.ExpressionCoshContext ctx) {
        return map -> Math.cosh(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionRound(SynthesisExpressionsParser.ExpressionRoundContext ctx) {
        return map -> Math.round(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionUnaryOperator(SynthesisExpressionsParser.ExpressionUnaryOperatorContext ctx) {
        ToDoubleFunction<Map<String, Double>> argFunc = visit(ctx.arg);
        return ctx.op.getText().equals("-") ? map -> -argFunc.applyAsDouble(map) : argFunc;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCeil(SynthesisExpressionsParser.ExpressionCeilContext ctx) {
        return map -> Math.ceil(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionTan(SynthesisExpressionsParser.ExpressionTanContext ctx) {
        return map -> Math.tan(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionMax(SynthesisExpressionsParser.ExpressionMaxContext ctx) {
        return map -> Math.max(visit(ctx.firstArgument).applyAsDouble(map), visit(ctx.secondArgument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionATan(SynthesisExpressionsParser.ExpressionATanContext ctx) {
        return map -> Math.atan(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionBracket(SynthesisExpressionsParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionSin(SynthesisExpressionsParser.ExpressionSinContext ctx) {
        return map -> Math.sin(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionPow(SynthesisExpressionsParser.ExpressionPowContext ctx) {
        return map -> Math.pow(visit(ctx.left).applyAsDouble(map), visit(ctx.right).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionSqrt(SynthesisExpressionsParser.ExpressionSqrtContext ctx) {
        return map -> Math.sqrt(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionExp(SynthesisExpressionsParser.ExpressionExpContext ctx) {
        return map -> Math.exp(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionSinh(SynthesisExpressionsParser.ExpressionSinhContext ctx) {
        return map -> Math.sinh(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionASin(SynthesisExpressionsParser.ExpressionASinContext ctx) {
        return map -> Math.asin(visit(ctx.argument).applyAsDouble(map));
    }



    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionATan2(SynthesisExpressionsParser.ExpressionATan2Context ctx) {
        return map -> Math.atan2(visit(ctx.firstArgument).applyAsDouble(map), visit(ctx.secondArgument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionAbs(SynthesisExpressionsParser.ExpressionAbsContext ctx) {
        return map -> Math.abs(visit(ctx.argument).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitMulDivExpression(SynthesisExpressionsParser.MulDivExpressionContext ctx) {
        ToDoubleFunction<Map<String, Double>> left = visit(ctx.left);
        ToDoubleFunction<Map<String, Double>> right = visit(ctx.right);
        return switch (ctx.op.getText()) {
            case "*" -> map -> left.applyAsDouble(map) * right.applyAsDouble(map);
            case "/" -> map -> left.applyAsDouble(map) / right.applyAsDouble(map);
            case "//" -> map -> (double) Math.floorDiv((long) left.applyAsDouble(map), (long) right.applyAsDouble(map));
            default -> throw new IllegalArgumentException("Unknown operator: " + ctx.op.getText());
        };
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitAddSubExpression(SynthesisExpressionsParser.AddSubExpressionContext ctx) {
        ToDoubleFunction<Map<String, Double>> left = visit(ctx.left);
        ToDoubleFunction<Map<String, Double>> right = visit(ctx.right);
        return switch (ctx.op.getText()) {
            case "+" -> map -> left.applyAsDouble(map) + right.applyAsDouble(map);
            case "-" -> map -> left.applyAsDouble(map) - right.applyAsDouble(map);
            case "%" -> map -> left.applyAsDouble(map) % right.applyAsDouble(map);
            default -> throw new IllegalArgumentException("Unknown operator: " + ctx.op.getText());
        };
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionTanh(SynthesisExpressionsParser.ExpressionTanhContext ctx) {
        return map -> Math.tanh(visit(ctx.argument).applyAsDouble(map));
    }



    private BooleanExpressionVisitor getBooleanExpressionVisitor(){
        return new BooleanExpressionVisitor(this);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitIfThenElseExpression(SynthesisExpressionsParser.IfThenElseExpressionContext ctx) {
        return map ->( ctx.guard.accept(getBooleanExpressionVisitor()).test(map) ?ctx.thenBranch.accept(this).applyAsDouble(map):ctx.elseBranch.accept(this).applyAsDouble(map));
    }
}
