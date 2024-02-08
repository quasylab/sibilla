package it.unicam.quasylab.sibilla.langs.enba.validators;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Type;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Variable;

import java.util.ArrayList;
import java.util.List;

public class PopulationExpressionValidator extends ExpressionValidator {
    public PopulationExpressionValidator(SymbolTable table, List<ModelBuildingError> errors, List<Variable> localVariables, Type type) {
        super(table, errors, localVariables, type);
    }
    @Override
    public Boolean visitPopulationSizeExpression(ExtendedNBAParser.PopulationSizeExpressionContext ctx) {
        if(!super.checkAssignment(Type.INTEGER, ctx)) {
            return false;
        }
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitPopulationFractionExpression(ExtendedNBAParser.PopulationFractionExpressionContext ctx) {
        if(!checkAssignment(Type.REAL, ctx)) {
            return false;
        }
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitAgent_predicate(ExtendedNBAParser.Agent_predicateContext ctx) {
        String species = ctx.name.getText();
        if(!super.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        List<Variable> predicateVariables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        ExpressionValidator booleanValidator = new ExpressionValidator(this.table, this.errors, predicateVariables, Type.BOOLEAN);
        return ctx.expr().accept(booleanValidator);
    }
}
