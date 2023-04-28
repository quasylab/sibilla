package it.unicam.quasylab.sibilla.shell.expression;

import it.unicam.quasylab.sibilla.shell.SibillaScriptBaseVisitor;
import it.unicam.quasylab.sibilla.shell.SibillaScriptParser;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class BooleanExpressionVisitor extends SibillaScriptBaseVisitor<Predicate<Map<String,Double>>> {

    private final ArithmeticExpressionVisitor arithmeticExpressionVisitor;

    public BooleanExpressionVisitor(ArithmeticExpressionVisitor arithmeticExpressionVisitor){
        this.arithmeticExpressionVisitor =arithmeticExpressionVisitor;
    }
    @Override
    public Predicate<Map<String,Double>> visitNegationExpression(SibillaScriptParser.NegationExpressionContext ctx) {
        return map -> !ctx.arg.accept(this).test(map);
    }

    @Override
    public Predicate<Map<String,Double>> visitAndExpression(SibillaScriptParser.AndExpressionContext ctx) {
        return map -> ctx.left.accept(this).test(map)&&ctx.right.accept(this).test(map);
    }

    @Override
    public Predicate<Map<String,Double>> visitOrExpression(SibillaScriptParser.OrExpressionContext ctx) {
        return map -> ctx.left.accept(this).test(map)||ctx.right.accept(this).test(map);
    }

    @Override
    public Predicate<Map<String,Double>> visitIfThenElseExpression(SibillaScriptParser.IfThenElseExpressionContext ctx) {
        return map -> (ctx.guard.accept(this).test(map)?ctx.thenBranch.accept(this).test(map):ctx.elseBranch.accept(this).test(map));
    }

    @Override
    public Predicate<Map<String,Double>> visitRelationExpression(SibillaScriptParser.RelationExpressionContext ctx) {
        return map -> getRelationOperator(ctx.op.getText()).apply(ctx.left.accept(arithmeticExpressionVisitor).applyAsDouble(map),ctx.right.accept(arithmeticExpressionVisitor).applyAsDouble(map));
    }
    @Override
    public Predicate<Map<String,Double>> visitFalseValue(SibillaScriptParser.FalseValueContext ctx) {
        return doubleMap -> false;
    }
    @Override
    public Predicate<Map<String,Double>> visitTrueValue(SibillaScriptParser.TrueValueContext ctx) {
        return doubleMap -> true;
    }

    public  BiFunction<Double,Double,Boolean> getRelationOperator(String op) {
        if (op.equals("<"))  { return (x,y) -> x<y; }
        if (op.equals("<="))  { return (x,y) -> x<=y; }
        if (op.equals("=="))  { return Double::equals; }
        if (op.equals("!="))  { return (x,y) -> !x.equals(y); }
        if (op.equals(">"))  { return (x,y) -> x>y; }
        if (op.equals(">="))  { return (x,y) -> x>=y; }
        return (x,y) -> false;
    }

}
