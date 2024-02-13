package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Variable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public class ExpressionGenerator extends ExtendedNBABaseVisitor<ExpressionFunction> {

    private final SymbolTable table;

    public ExpressionGenerator(SymbolTable table) {
        this.table = table;
    }
    @Override
    public ExpressionFunction visitExponentExpression(ExtendedNBAParser.ExponentExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (context) -> SibillaValue.eval(
                Math::pow,
                left.eval(context),
                right.eval(context)
        );
    }

    @Override
    public ExpressionFunction visitReferenceExpression(ExtendedNBAParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        return (context) -> context.getAgentValues().get(name);
    }

    @Override
    public ExpressionFunction visitSenderReferenceExpression(ExtendedNBAParser.SenderReferenceExpressionContext ctx) {
        String name = ctx.ID().getText();
        return (context) -> context.getSenderValues().get(name);
    }

    @Override
    public ExpressionFunction visitIntValue(ExtendedNBAParser.IntValueContext ctx) {
        int integer = Integer.parseInt(ctx.getText());
        return (context) -> new SibillaInteger(integer);
    }

    @Override
    public ExpressionFunction visitBracketExpression(ExtendedNBAParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ExpressionFunction visitRealValue(ExtendedNBAParser.RealValueContext ctx) {
        double doubleValue = Double.parseDouble(ctx.getText());
        return (context) -> new SibillaDouble(doubleValue);
    }

    @Override
    public ExpressionFunction visitIfThenElseExpression(ExtendedNBAParser.IfThenElseExpressionContext ctx) {
        ExpressionFunction guard = ctx.guard.accept(this);
        ExpressionFunction thenBranch = ctx.thenBranch.accept(this);
        ExpressionFunction elseBranch = ctx.elseBranch.accept(this);

        return (context) -> guard.eval(context).booleanOf()
                ? thenBranch.eval(context)
                : elseBranch.eval(context);
    }

    @Override
    public ExpressionFunction visitNegationExpression(ExtendedNBAParser.NegationExpressionContext ctx) {
        ExpressionFunction expr = ctx.arg.accept(this);
        return (context) -> SibillaValue.not(expr.eval(context));
    }

    @Override
    public ExpressionFunction visitTrueValue(ExtendedNBAParser.TrueValueContext ctx) {
        return (context) -> SibillaBoolean.TRUE;
    }

    @Override
    public ExpressionFunction visitRelationExpression(ExtendedNBAParser.RelationExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (context) -> SibillaBoolean.of(
                SibillaValue.getRelationOperator(operator)
                        .test(
                                left.eval(context),
                                right.eval(context)
                        )
        );
    }

    @Override
    public ExpressionFunction visitOrExpression(ExtendedNBAParser.OrExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (context) -> SibillaValue.or(
                left.eval(context),
                right.eval(context)
        );
    }

    @Override
    public ExpressionFunction visitFalseValue(ExtendedNBAParser.FalseValueContext ctx) {
        return (context) -> SibillaBoolean.FALSE;
    }

    @Override
    public ExpressionFunction visitAndExpression(ExtendedNBAParser.AndExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (context) -> SibillaValue.and(
                left.eval(context),
                right.eval(context)
        );
    }

    @Override
    protected ExpressionFunction defaultResult() {
        return (context) -> SibillaValue.ERROR_VALUE;
    }


    @Override
    public ExpressionFunction visitMulDivExpression(ExtendedNBAParser.MulDivExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (context) -> SibillaValue
                .getOperator(operator)
                .apply(
                        left.eval(context),
                        right.eval(context)
                );
    }

    @Override
    public ExpressionFunction  visitAddSubExpression(ExtendedNBAParser.AddSubExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (context) -> SibillaValue
                .getOperator(operator)
                .apply(
                        left.eval(context),
                        right.eval(context)
                );
    }

    @Override
    public ExpressionFunction visitUnaryExpression(ExtendedNBAParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            ExpressionFunction expr = ctx.arg.accept(this);
            return (context) -> SibillaValue.minus(expr.eval(context));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public ExpressionFunction visitPopulationFractionExpression(ExtendedNBAParser.PopulationFractionExpressionContext ctx) {
        BiPredicate<Integer, ExpressionContext> predicate = new AgentPredicateGenerator(this.table).visitAgent_predicate(ctx.agent_predicate());
        return (context) -> SibillaValue.of(context.getState().fractionOf(predicate));
    }

    @Override
    public ExpressionFunction visitPopulationSizeExpression(ExtendedNBAParser.PopulationSizeExpressionContext ctx) {
        BiPredicate<Integer, ExpressionContext> predicate = new AgentPredicateGenerator(this.table).visitAgent_predicate(ctx.agent_predicate());
        return (context) -> SibillaValue.of(context.getState().numberOf(predicate));
    }

}
