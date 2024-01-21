package it.unicam.quasylab.sibilla.langs.dopm.evaluators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.generators.AgentPredicateGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class PopulationExpressionEvaluator extends DataOrientedPopulationModelBaseVisitor<TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue>> {
    public PopulationExpressionEvaluator() {

    }


    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.eval(Math::pow, left.apply(agent,sender,state), right.apply(agent,sender,state));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        return (agent, sender, state) -> agent.apply(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        String name = ctx.ID().getText();
        return (agent, sender, state) -> agent.apply(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        int integer = Integer.parseInt(ctx.getText());
        return (agent, sender, state) -> new SibillaInteger(integer);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        double doublevalue = Double.parseDouble(ctx.getText());
        return (agent, sender, state) -> new SibillaDouble(doublevalue);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {

        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> guard = ctx.guard.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> thenBranch = ctx.thenBranch.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> elseBranch = ctx.elseBranch.accept(this);

        return (agent, sender, state) -> guard.apply(agent, sender, state).booleanOf() ? thenBranch.apply(agent, sender, state) : elseBranch.apply(agent, sender, state);
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> expr = ctx.arg.accept(this);
        return (agent, receiver, state) -> SibillaValue.not(expr.apply(agent, receiver, state));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return (agent, sender, state) -> SibillaBoolean.TRUE;
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaBoolean.of(SibillaValue.getRelationOperator(operator).test(left.apply(agent, sender, state), right.apply(agent, sender, state)));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.or(left.apply(agent, sender, state), right.apply(agent, sender, state));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        return (agent, sender, state) -> SibillaBoolean.FALSE;
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue>left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender, state) -> SibillaValue.and(left.apply(agent, sender, state), right.apply(agent, sender, state));
    }

    @Override
    protected TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> defaultResult() {
        return (agent, sender, state) -> SibillaValue.ERROR_VALUE;
    }


    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaValue.getOperator(operator).apply(left.apply(agent,sender,state),right.apply(agent, sender,state));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue>  visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> left = ctx.left.accept(this);
        TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender, state) -> SibillaValue.getOperator(operator).apply(left.apply(agent,sender,state),right.apply(agent, sender,state));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> expr= ctx.arg.accept(this);
            return (agent, sender, state) -> SibillaValue.minus(expr.apply(agent, sender, state));
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return (agent, sender, state) -> SibillaValue.of(state.fractionOf(predicate));
    }

    @Override
    public TriFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, DataOrientedPopulationState, SibillaValue> visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        Predicate<Agent> predicate = new AgentPredicateGenerator().visitAgent_predicate(ctx.agent_predicate());
        return (agent, sender, state) -> SibillaValue.of(state.numberOf(predicate));
    }

}
