package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.Type;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.Variable;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

public class ExpressionValidator extends DataOrientedPopulationModelBaseVisitor<Boolean> {
    protected final SymbolTable table;
    protected final List<ModelBuildingError> errors;
    protected final List<Variable> localVariables;
    protected final Type type;

    public ExpressionValidator(SymbolTable table, List<ModelBuildingError> errors, List<Variable> localVariables, Type type) {
        this.table = table;
        this.errors = errors;
        this.localVariables = localVariables;
        this.type = type;
    }

    protected boolean checkAssignment(Type assignment, ParserRuleContext ctx) {
        if(type != null && !type.assignmentCompatible(assignment)) {
            this.errors.add(ModelBuildingError.unexpectedType(type,  ctx));
            return false;
        }
        return true;
    }

    protected boolean checkReference(String name, ParserRuleContext ctx) {
        Optional<Variable> var = localVariables.stream().filter(v -> v.name().equals(name)).findFirst();
        if(var.isEmpty()) {
            this.errors.add(ModelBuildingError.unknownSymbol(name, ctx.start.getLine(), ctx.start.getCharPositionInLine()));
            return false;
        }
        return checkAssignment(var.get().type(), ctx);
    }

    @Override
    public Boolean visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        if(!checkAssignment(Type.BOOLEAN, ctx)) {
            return false;
        }
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();

        return checkReference(name, ctx);
    }

    @Override
    public Boolean visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        return checkAssignment(Type.INTEGER, ctx);
    }

    @Override
    public Boolean visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return checkAssignment(Type.BOOLEAN, ctx);
    }

    @Override
    public Boolean visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        ExpressionValidator realValidator = new ExpressionValidator(table,errors,localVariables,Type.REAL);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(realValidator) && ctx.right.accept(realValidator);
    }

    @Override
    public Boolean visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        String name = "sender." + ctx.ID().getText();
        return checkReference(name, ctx);
    }

    @Override
    public Boolean visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables,Type.BOOLEAN);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator);
    }

    @Override
    public Boolean visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(this.table,this.errors,this.localVariables, Type.BOOLEAN);
        return ctx.guard.accept(booleanValidator) && ctx.thenBranch.accept(this) && ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        return checkAssignment(Type.BOOLEAN, ctx);
    }

    @Override
    public Boolean visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        return checkAssignment(Type.REAL, ctx);
    }

    @Override
    public Boolean visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables,Type.BOOLEAN);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator);
    }

    @Override
    public Boolean visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.expr().accept(this);
    }
    @Override
    public Boolean visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }
    @Override
    public Boolean visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }
}
