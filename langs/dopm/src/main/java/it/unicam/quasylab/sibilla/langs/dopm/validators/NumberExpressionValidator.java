package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolType;

import java.util.List;

public class NumberExpressionValidator extends DataOrientedPopulationModelBaseVisitor<Boolean> {
    protected final List<ModelBuildingError> errors;
protected final List<String> localVariables;

    public NumberExpressionValidator(List<ModelBuildingError> errors, List<String> local_variables) {
        this.errors = errors;
        this.localVariables = local_variables;
    }

    @Override
    public Boolean visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.ID().getText();
        if(!localVariables.contains(name)) {
            this.errors.add(ModelBuildingError.unknownSymbol(name, ctx.start.getLine(), ctx.start.getCharPositionInLine()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
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
        String name = ctx.ID().getText();
        if(!localVariables.contains("sender."+name)) {
            this.errors.add(ModelBuildingError.unknownSymbol(name, ctx.start.getLine(), ctx.start.getCharPositionInLine()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        BooleanExpressionValidator booleanValidator = new BooleanExpressionValidator(this.errors,this);
        return ctx.guard.accept(booleanValidator) && ctx.thenBranch.accept(this) && ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        this.errors.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        return ctx.expr().accept(this);
    }
}
