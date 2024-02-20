package it.unicam.quasylab.sibilla.langs.dopm.validators;

import it.unicam.quasylab.sibilla.langs.dopm.*;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.Type;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.Variable;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.exceptions.DuplicatedSymbolException;

import java.util.*;
import java.util.stream.Stream;

public class ModelValidator extends DataOrientedPopulationModelBaseVisitor<Boolean> {

    private final List<ModelBuildingError> errors;
    private final SymbolTable table;
    public ModelValidator(List<ModelBuildingError> errors) {
        this.errors = errors;
        this.table = new SymbolTable();
    }

    @Override
    public Boolean visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        boolean valid = true;
        for(DataOrientedPopulationModelParser.ElementContext ectx : ctx.element()) {
            valid &= ectx.accept(this);
        }
        return valid;
    }

    @Override
    public Boolean visitSpecies_declaration(DataOrientedPopulationModelParser.Species_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addSpecies(name, ctx);
            for (DataOrientedPopulationModelParser.Var_declContext vctx : ctx.var_decl()) {
                this.table.addSpeciesVar(name, vctx);
            }
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
        return true;
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
        return ctx.expr().accept(new ExpressionValidator(this.table, this.errors, new ArrayList<>(), true, Type.REAL));
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
        return ctx.expr().accept(new ExpressionValidator(this.table, this.errors, new ArrayList<>(), true, Type.BOOLEAN));
    }
    @Override
    public Boolean visitSystem_declaration(DataOrientedPopulationModelParser.System_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addSystem(name, ctx);
        } catch(DuplicatedSymbolException e) {
            this.errors.add(ModelBuildingError.duplicatedName(name, this.table.getContext(name), ctx));
            return false;
        }
        return ctx.system_composition().accept(this);
    }
    @Override
    public Boolean visitSystem_composition(DataOrientedPopulationModelParser.System_compositionContext ctx) {
        for(DataOrientedPopulationModelParser.System_componentContext sctx : ctx.system_component()) {
            if(!checkSystemComponent(sctx)) {
                return false;
            }
        }
        return true;
    }
    private Boolean checkSystemComponent(DataOrientedPopulationModelParser.System_componentContext ctx) {
        return checkAgentExpression(ctx.agent_expression(), new ArrayList<>(), false);
    }
    private Boolean checkAgentExpression(DataOrientedPopulationModelParser.Agent_expressionContext ctx, List<Variable> variables, boolean modelContext) {
        String species = ctx.name.getText();
        if(!this.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        List <Variable> speciesVariables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        List<DataOrientedPopulationModelParser.Var_assContext> expressionVars = ctx.var_ass_list().var_ass();
        if(speciesVariables.size() != expressionVars.size()) {
            this.errors.add(ModelBuildingError.incorrectAgentExpression(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        for(int i=0; i<speciesVariables.size(); ++i) {
            Variable currentSpeciesVariable = speciesVariables.get(i);
            DataOrientedPopulationModelParser.Var_assContext currentAssContext = expressionVars.get(i);
            if(!currentSpeciesVariable.name().equals(currentAssContext.name.getText())) {
                this.errors.add(
                        ModelBuildingError.incorrectAgentExpression(
                                species,
                                currentAssContext.name.getLine(),
                                currentAssContext.name.getCharPositionInLine()
                        )
                );
                return false;
            }
            ExpressionValidator validator = new ExpressionValidator(
                                                                this.table,
                                                                this.errors,
                                                                variables,
                                                                modelContext,
                                                                currentSpeciesVariable.type()
            );
            if(!currentAssContext.expr().accept(validator)) {
                return false;
            }
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
        if(ctx.broadcast_rule_body() != null) {
            return checkBroadcastRuleBody(ctx.broadcast_rule_body());
        } else {
            return checkUnicastRuleBody(ctx.unicast_rule_body());
        }
    }

    private boolean checkBroadcastRuleBody(DataOrientedPopulationModelParser.Broadcast_rule_bodyContext ctx) {
        List<Variable> sender_variables = new ArrayList<>();
        if(!checkOutputTransition(ctx.output, sender_variables)) {
            return false;
        }
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : ctx.inputs.input_transition()) {
            if(!checkInputTransition(ictx, new ArrayList<>(sender_variables))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUnicastRuleBody(DataOrientedPopulationModelParser.Unicast_rule_bodyContext ctx) {
        List<Variable> sender_variables = new ArrayList<>();
        if(!checkOutputTransition(ctx.output, sender_variables)) {
            return false;
        }
        Set<String> inputSpeciesSet = new HashSet<>();
        for(DataOrientedPopulationModelParser.Input_transitionContext ictx : ctx.inputs.input_transition()) {
            String preSpecies = ictx.pre.name.getText();
            if(inputSpeciesSet.contains(preSpecies)) {
                errors.add(ModelBuildingError.duplicatedSpecies(preSpecies, ictx.pre.getStart().getLine(), ictx.pre.getStart().getCharPositionInLine()));
                return false;
            } else {
                inputSpeciesSet.add(preSpecies);
            }
            if(!checkInputTransition(ictx, new ArrayList<>(sender_variables))) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkOutputTransition(DataOrientedPopulationModelParser.Output_transitionContext ctx, List<Variable> variables) {
        if(!ctx.pre.accept(this)) {
            return false;
        }
        variables.addAll(this.table.getSpeciesVariables(ctx.pre.name.getText()).orElse(new ArrayList<>()));
        return ctx.rate.accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.REAL)) &&
               checkAgentMutation(ctx.post, variables);
    }

    private Boolean checkInputTransition(DataOrientedPopulationModelParser.Input_transitionContext ctx, List<Variable> variables) {
        if(
                !ctx.pre.accept(this) ||
                !ctx.sender_predicate.accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.BOOLEAN))
        ) {
            return false;
        }
        variables = Stream.concat(
                variables
                    .stream()
                    .map(v -> new Variable("sender."+v.name(), v.type(), v.context())),
                this.table
                        .getSpeciesVariables(ctx.pre.name.getText())
                        .orElse(new ArrayList<>())
                        .stream()
        ).toList();
        return ctx.probability.accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.REAL)) &&
               checkAgentMutation(ctx.post, variables);
    }

    private Boolean checkAgentMutation(DataOrientedPopulationModelParser.Agent_mutationContext ctx, List<Variable> variables) {
        if(ctx.deterministic_mutation != null) {
            return checkAgentExpression(ctx.deterministic_mutation, variables, true);
        }
        for(DataOrientedPopulationModelParser.Stochastic_mutation_tupleContext tctx : ctx.stochastic_mutation_tuple()) {
            if(
                    !tctx.expr().accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.REAL)) ||
                    !checkAgentExpression(tctx.agent_expression(), variables, true)
            ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitAgent_predicate(DataOrientedPopulationModelParser.Agent_predicateContext ctx) {
        return ctx.accept(new ExpressionValidator(this.table, this.errors, new ArrayList<>(), true, Type.BOOLEAN));
    }
    public List<ModelBuildingError> getErrors() {
    	return this.errors;
    }

    public SymbolTable getTable() {
    	return table;
    }

    public int getNumberOfValidationErrors() {
        return errors.size();
    }
}
