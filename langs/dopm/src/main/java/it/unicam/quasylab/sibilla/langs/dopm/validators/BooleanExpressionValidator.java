package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolType;

import java.util.List;

public class BooleanExpressionValidator extends DataOrientedPopulationModelBaseVisitor<Boolean> {
    private final List<ModelBuildingError> errors;
    private final NumberExpressionValidator numberExpressionValidator;

    public BooleanExpressionValidator(List<ModelBuildingError> errors, NumberExpressionValidator numberExpressionValidator) {
        this.errors = errors;
        this.numberExpressionValidator = numberExpressionValidator;
    }

    @Override
    public Boolean visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        return ctx.left.accept(this.numberExpressionValidator) && ctx.right.accept(this.numberExpressionValidator);
    }

    @Override
    public Boolean visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        return ctx.guard.accept(this) && ctx.thenBranch.accept(this) && ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.NUMBER,ctx));
        return false;
    }
}
