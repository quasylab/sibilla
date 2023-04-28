package it.unicam.quasylab.sibilla.shell.expression;

import it.unicam.quasylab.sibilla.shell.SibillaScriptBaseVisitor;
import it.unicam.quasylab.sibilla.shell.SibillaScriptParser;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;

public class ArithmeticExpressionVisitor extends SibillaScriptBaseVisitor<ToDoubleFunction<Map<String, Double>>> {

    @Override
    public ToDoubleFunction<Map<String, Double>> visitIntValue(SibillaScriptParser.IntValueContext ctx) {
        return map -> Double.parseDouble(ctx.getText());
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitRealValue(SibillaScriptParser.RealValueContext ctx) {
        return map -> Double.parseDouble(ctx.getText());
    }

    @Override
    public  ToDoubleFunction<Map<String, Double>> visitInfinity(SibillaScriptParser.InfinityContext ctx) {
        return map -> ctx.getText().equals("-inf") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitReferenceExpression(SibillaScriptParser.ReferenceExpressionContext ctx) {
        return map -> map.get(ctx.getText());
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExponentExpression(SibillaScriptParser.ExponentExpressionContext ctx) {
        return map -> Math.pow(
                ctx.left.accept(this).applyAsDouble(map),
                ctx.right.accept(this).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitAddSubExpression(SibillaScriptParser.AddSubExpressionContext ctx) {
        return map -> getOperator(ctx.op.getText()).applyAsDouble(
                ctx.left.accept(this).applyAsDouble(map),
                ctx.right.accept(this).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitMulDivExpression(SibillaScriptParser.MulDivExpressionContext ctx) {
        return map -> getOperator(ctx.op.getText()).applyAsDouble(
                ctx.left.accept(this).applyAsDouble(map),
                ctx.right.accept(this).applyAsDouble(map));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitBracketExpression(SibillaScriptParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitIfThenElseExpression(SibillaScriptParser.IfThenElseExpressionContext ctx) {
        return map ->( ctx.guard.accept(getBooleanExpressionVisitor()).test(map) ?ctx.thenBranch.accept(this).applyAsDouble(map):ctx.elseBranch.accept(this).applyAsDouble(map));
    }

    private BooleanExpressionVisitor getBooleanExpressionVisitor(){
        return new BooleanExpressionVisitor(this);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitUnaryExpression(SibillaScriptParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-"))
            return map -> -ctx.arg.accept(this).applyAsDouble(map);
        else
            return ctx.arg.accept(this);
    }


    public DoubleBinaryOperator getOperator(String op) {
        if (op.equals("+")) {return Double::sum;}
        if (op.equals("-")) {return (x,y) -> x-y; }
        if (op.equals("%")) {return (x,y) -> x%y; }
        if (op.equals("*")) {return (x,y) -> x*y; }
        if (op.equals("/")) {return (x,y) -> x/y; }
        if (op.equals("//")) {return (x,y) -> (y==0.0?0.0:x/y); }
        return (x,y) -> Double.NaN;
    }


}
