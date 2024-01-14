package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.*;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.BaseSymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.exceptions.DuplicatedSymbolException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelValidator extends DataOrientedPopulationModelBaseVisitor<Boolean> {

    private final List<ModelBuildingError> errors;
    private final SymbolTable table;

    public ModelValidator() {
        this.errors = new LinkedList<>();
        this.table = new BaseSymbolTable();
    }

    public ModelValidator(List<ModelBuildingError> errors) {
        this.errors = errors;
        this.table = new BaseSymbolTable();
    }

    @Override
    public Boolean visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        for(DataOrientedPopulationModelParser.ElementContext ectx : ctx.element()) {
            if(!ectx.accept(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitSystem_declaration(DataOrientedPopulationModelParser.System_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addSystem(name, ctx);
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return ctx.system_composition().accept(this);
    }

    @Override
    public Boolean visitSystem_composition(DataOrientedPopulationModelParser.System_compositionContext ctx) {
        for(DataOrientedPopulationModelParser.Agent_instantationContext actx : ctx.agent_instantation()) {
            if(!checkAgentInstantation(actx)) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkAgentInstantation(DataOrientedPopulationModelParser.Agent_instantationContext ctx) {
        String species = ctx.agent_expression().name.getText();
        if(!this.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.agent_expression().name.getLine(), ctx.agent_expression().name.getCharPositionInLine()));
            return false;
        }
        if(ctx.agent_expression().vars != null) {
            for (DataOrientedPopulationModelParser.Var_assContext c : ctx.agent_expression().vars.var_ass()) {
                String name = c.getText();
                if(this.table.isDefined(name)) {
                    this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
                    return false;
                }
                if(c.expr().accept(new NumberExpressionValidator(this.errors,new ArrayList<>()))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean visitSpecies_declaration(DataOrientedPopulationModelParser.Species_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addSpecies(name, ctx);
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitRule_declaration(DataOrientedPopulationModelParser.Rule_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addRule(name, ctx);
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return ctx.body.accept(this);
    }

    @Override
    public Boolean visitRule_body(DataOrientedPopulationModelParser.Rule_bodyContext ctx) {
        List<String> sender_variables = new ArrayList<>();
        if(!checkOutputTransition(ctx.output, sender_variables)) {
            return false;
        }
        sender_variables = sender_variables.stream().map(c -> "sender." + c).toList();
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : ctx.inputs.input_transition()) {
            if(!checkInputTransition(ictx, new ArrayList<>(sender_variables))) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkOutputTransition(DataOrientedPopulationModelParser.Output_transitionContext ctx, List<String> variables) {
        return checkAgentPredicate(ctx.pre, variables) &&
                ctx.rate.accept(new PopulationExpressionValidator(this.errors, variables, this.table)) &&
                checkAgentExpression(ctx.post, variables);
    }

    private Boolean checkInputTransition(DataOrientedPopulationModelParser.Input_transitionContext ctx, List<String> variables) {
        return checkAgentPredicate(ctx.pre, variables) &&
                ctx.sender_predicate.accept(new BooleanExpressionValidator(this.errors, new NumberExpressionValidator(this.errors,variables))) &&
                ctx.probability.accept(new PopulationExpressionValidator(this.errors, variables, this.table)) &&
                checkAgentExpression(ctx.post, variables);
    }
    private Boolean checkAgentPredicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx, List<String> variables) {
        String species = ctx.name.getText();
        if(!this.table.isASpecies(species)) {
           this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
           return false;
        }
        if(ctx.vars != null) {
            for (TerminalNode c : ctx.vars.ID()) {
                String name = c.getText();
                if(this.table.isDefined(name)) {
                    this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
                    return false;
                }
                if(variables.contains(name)) {
                    this.errors.add(ModelBuildingError.duplicatedLocalVariable(name, c.getSymbol().getLine(), c.getSymbol().getCharPositionInLine()));
                    return false;
                }
                variables.add(name);
            }
        }
        return ctx.predicate.accept(new BooleanExpressionValidator(this.errors, new NumberExpressionValidator(this.errors, variables)));
    }

    private Boolean checkAgentExpression(DataOrientedPopulationModelParser.Agent_expressionContext ctx, List<String> variables) {
        String species = ctx.name.getText();
        if(!this.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        if(ctx.vars != null) {
            for (DataOrientedPopulationModelParser.Var_assContext c : ctx.vars.var_ass()) {
                String name = c.name.getText();
                if(this.table.isDefined(name)) {
                    this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
                    return false;
                }
                if(!variables.contains(name)) {
                    this.errors.add(ModelBuildingError.unknownSymbol(name, c.start.getLine(), c.start.getCharPositionInLine()));
                    return false;
                }
                if(c.expr().accept(new NumberExpressionValidator(this.errors,variables))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        return checkAgentPredicate(ctx, new ArrayList<>());
    }

    @Override
    public Boolean visitMeasure_declaration(DataOrientedPopulationModelParser.Measure_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addMeasure(name, ctx);
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return ctx.expr().accept(new PopulationExpressionValidator(this.errors, new ArrayList<>(), this.table));
    }

    @Override
    public Boolean visitPredicate_declaration(DataOrientedPopulationModelParser.Predicate_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addPredicate(name, ctx);
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return ctx.expr().accept(new BooleanExpressionValidator(this.errors, new PopulationExpressionValidator(this.errors,new ArrayList<>(),this.table)));
    }

    public List<ModelBuildingError> getErrors() {
        return errors;
    }

    public SymbolTable getTable() {
        return table;
    }

    public int getNumberOfValidationErrors() {
        return errors.size();
    }
}
