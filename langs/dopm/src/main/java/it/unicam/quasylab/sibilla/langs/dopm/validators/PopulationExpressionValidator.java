package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class PopulationExpressionValidator extends NumberExpressionValidator {

    private final SymbolTable table;
    public PopulationExpressionValidator(List<ModelBuildingError> errors, List<String> local_variables, SymbolTable table) {
        super(errors, local_variables);
        this.table = table;
    }

    @Override
    public Boolean visitPopulationFractionExpression(DataOrientedPopulationModelParser.PopulationFractionExpressionContext ctx) {
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitPopulationSizeExpression(DataOrientedPopulationModelParser.PopulationSizeExpressionContext ctx) {
        return ctx.agent.accept(this);
    }

    @Override
    public Boolean visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        String species = ctx.name.getText();
        if(!table.isASpecies(species)) {
            super.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(),ctx.name.getCharPositionInLine()));
            return false;
        }
        if(ctx.vars != null) {
            for (TerminalNode c : ctx.vars.ID()) {
                String name = c.getText();
                if(this.table.isDefined(name)) {
                    this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
                    return false;
                }
                if(super.localVariables.contains(name)) {
                    this.errors.add(ModelBuildingError.duplicatedLocalVariable(name, c.getSymbol().getLine(), c.getSymbol().getCharPositionInLine()));
                    return false;
                }
                super.localVariables.add(name);
            }
        }
        BooleanExpressionValidator booleanExpressionValidator = new BooleanExpressionValidator(this.errors, this);
        return ctx.predicate.accept(booleanExpressionValidator);
    }
}
