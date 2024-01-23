package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.core.models.dopm.functions.ExpressionFunction;

import java.util.function.Predicate;

public class ExpressionGenerator extends DataOrientedPopulationModelBaseVisitor<ExpressionFunction> {
    public ExpressionGenerator() {

    }


    @Override
    public ExpressionFunction visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.eval(
                Math::pow,
                left.eval(agent,sender,state),
                right.eval(agent,sender,state)
        );
    }

    @Override
    public ExpressionFunction visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        return (agent, sender, state) -> agent.solve(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public ExpressionFunction visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        String name = ctx.ID().getText();
        return (agent, sender, state) -> sender.solve(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public ExpressionFunction visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        int integer = Integer.parseInt(ctx.getText());
        return (agent, sender, state) -> new SibillaInteger(integer);
    }

    @Override
    public ExpressionFunction visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ExpressionFunction visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        double doubleValue = Double.parseDouble(ctx.getText());
        return (agent, sender, state) -> new SibillaDouble(doubleValue);
    }

    @Override
    public ExpressionFunction visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        ExpressionFunction guard = ctx.guard.accept(this);
        ExpressionFunction thenBranch = ctx.thenBranch.accept(this);
        ExpressionFunction elseBranch = ctx.elseBranch.accept(this);

        return (agent, sender, state) -> guard.eval(agent, sender, state).booleanOf()
                ? thenBranch.eval(agent, sender, state)
                : elseBranch.eval(agent, sender, state);
    }

    @Override
    public ExpressionFunction visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        ExpressionFunction expr = ctx.arg.accept(this);
        return (agent, receiver, state) -> SibillaValue.not(expr.eval(agent, receiver, state));
    }

    @Override
    public ExpressionFunction visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return (agent, sender, state) -> SibillaBoolean.TRUE;
    }

    @Override
    public ExpressionFunction visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaBoolean.of(
                SibillaValue.getRelationOperator(operator)
                        .test(
                                left.eval(agent, sender, state),
                                right.eval(agent, sender, state)
                        )
        );
    }

    @Override
    public ExpressionFunction visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.or(
                left.eval(agent, sender, state),
                right.eval(agent, sender, state)
        );
    }

    @Override
    public ExpressionFunction visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        return (agent, sender, state) -> SibillaBoolean.FALSE;
    }

    @Override
    public ExpressionFunction visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.and(
                left.eval(agent, sender, state),
                right.eval(agent, sender, state)
        );
    }

    @Override
    protected ExpressionFunction defaultResult() {
        return (agent, sender, state) -> SibillaValue.ERROR_VALUE;
    }


    @Override
    public ExpressionFunction visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaValue
                .getOperator(operator)
                .apply(
                        left.eval(agent,sender,state),
                        right.eval(agent, sender, state)
                );
    }

    @Override
    public ExpressionFunction  visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        ExpressionFunction left = ctx.left.accept(this);
        ExpressionFunction right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaValue
                .getOperator(operator)
                .apply(
                        left.eval(agent, sender, state),
                        right.eval(agent, sender, state)
                );
    }

    @Override
    public ExpressionFunction visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            ExpressionFunction expr = ctx.arg.accept(this);
            return (agent, sender, state) -> SibillaValue.minus(expr.eval(agent, sender, state));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public ExpressionFunction visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return (agent, sender, state) -> state != null
                ? SibillaValue.of(state.fractionOf(predicate))
                : SibillaValue.ERROR_VALUE;
    }

    @Override
    public ExpressionFunction visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return (agent, sender, state) -> state != null
                ? SibillaValue.of(state.numberOf(predicate))
                : SibillaValue.ERROR_VALUE;
    }

}
