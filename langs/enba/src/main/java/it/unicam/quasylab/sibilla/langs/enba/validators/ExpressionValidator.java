package it.unicam.quasylab.sibilla.langs.enba.validators;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Type;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Variable;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
import java.util.Optional;

public class ExpressionValidator extends ExtendedNBABaseVisitor<Boolean> {
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
    public Boolean visitNegationExpression(ExtendedNBAParser.NegationExpressionContext ctx) {
        if(!checkAssignment(Type.BOOLEAN, ctx)) {
            return false;
        }
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExponentExpression(ExtendedNBAParser.ExponentExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitReferenceExpression(ExtendedNBAParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();

        return checkReference(name, ctx);
    }

    @Override
    public Boolean visitIntValue(ExtendedNBAParser.IntValueContext ctx) {
        return checkAssignment(Type.INTEGER, ctx);
    }

    @Override
    public Boolean visitTrueValue(ExtendedNBAParser.TrueValueContext ctx) {
        return checkAssignment(Type.BOOLEAN, ctx);
    }

    @Override
    public Boolean visitRelationExpression(ExtendedNBAParser.RelationExpressionContext ctx) {
        ExpressionValidator realValidator = new ExpressionValidator(table,errors,localVariables,Type.REAL);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(realValidator) && ctx.right.accept(realValidator);
    }

    @Override
    public Boolean visitBracketExpression(ExtendedNBAParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitSenderReferenceExpression(ExtendedNBAParser.SenderReferenceExpressionContext ctx) {
        String name = "sender." + ctx.ID().getText();
        return checkReference(name, ctx);
    }

    @Override
    public Boolean visitOrExpression(ExtendedNBAParser.OrExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables,Type.BOOLEAN);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator);
    }

    @Override
    public Boolean visitIfThenElseExpression(ExtendedNBAParser.IfThenElseExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(this.table,this.errors,this.localVariables, Type.BOOLEAN);
        return ctx.guard.accept(booleanValidator) && ctx.thenBranch.accept(this) && ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitFalseValue(ExtendedNBAParser.FalseValueContext ctx) {
        return checkAssignment(Type.BOOLEAN, ctx);
    }

    @Override
    public Boolean visitRealValue(ExtendedNBAParser.RealValueContext ctx) {
        return checkAssignment(Type.REAL, ctx);
    }

    @Override
    public Boolean visitAndExpression(ExtendedNBAParser.AndExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables,Type.BOOLEAN);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator);
    }

    @Override
    public Boolean visitMulDivExpression(ExtendedNBAParser.MulDivExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitAddSubExpression(ExtendedNBAParser.AddSubExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.left.accept(this) && ctx.right.accept(this);
    }

    @Override
    public Boolean visitUnaryExpression(ExtendedNBAParser.UnaryExpressionContext ctx) {
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.expr().accept(this);
    }
    @Override
    public Boolean visitPopulationFractionExpression(ExtendedNBAParser.PopulationFractionExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }
    @Override
    public Boolean visitPopulationSizeExpression(ExtendedNBAParser.PopulationSizeExpressionContext ctx) {
        this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }
}
