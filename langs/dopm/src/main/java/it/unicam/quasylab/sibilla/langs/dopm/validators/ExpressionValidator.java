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
    protected final boolean modelContext;

    protected final Type type;

    public ExpressionValidator(SymbolTable table, List<ModelBuildingError> errors, List<Variable> localVariables, boolean modelContext, Type type) {
        this.table = table;
        this.errors = errors;
        this.localVariables = localVariables;
        this.modelContext = modelContext;
        this.type = type;
    }

    protected boolean checkAssignment(Type assignment, ParserRuleContext ctx) {
        if(type != null && !type.assignmentCompatible(assignment)) {
            this.errors.add(ModelBuildingError.unexpectedType(ctx));
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
        ExpressionValidator realValidator = new ExpressionValidator(table,errors,localVariables,modelContext,Type.REAL);
        if(ctx.op.getText().equals("==")) {
            ExpressionValidator booleanValidator = new ExpressionValidator(table,new ArrayList<>(),localVariables,modelContext,Type.BOOLEAN);

            return checkAssignment(Type.BOOLEAN, ctx) &&
                    (
                            (ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator)) ||
                                    (ctx.left.accept(realValidator) && ctx.right.accept(realValidator))
                    );
        }
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
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables,modelContext,Type.BOOLEAN);
        return checkAssignment(Type.BOOLEAN, ctx) && ctx.left.accept(booleanValidator) && ctx.right.accept(booleanValidator);
    }

    @Override
    public Boolean visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        ExpressionValidator booleanValidator = new ExpressionValidator(this.table,this.errors,this.localVariables, modelContext,Type.BOOLEAN);
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
        ExpressionValidator booleanValidator = new ExpressionValidator(table,errors,localVariables, modelContext, Type.BOOLEAN);
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
    public Boolean visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        if(!modelContext) {
            this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
            return false;
        }
        if(!checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        if(!modelContext) {
            this.errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
            return false;
        }
        if(!checkAssignment(Type.REAL, ctx)) {
            return false;
        }
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        String species = ctx.name.getText();
        if(!this.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        List<Variable> predicateVariables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        ExpressionValidator booleanValidator = new ExpressionValidator(this.table, this.errors, predicateVariables, modelContext, Type.BOOLEAN);
        return ctx.expr().accept(booleanValidator);
    }
}
