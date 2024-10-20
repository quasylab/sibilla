package it.unicam.quasylab.sibilla.langs.enba.validators;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Type;
import it.unicam.quasylab.sibilla.langs.enba.symbols.Variable;
import it.unicam.quasylab.sibilla.langs.enba.symbols.exceptions.DuplicatedSymbolException;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;
import java.util.stream.Stream;

public class ModelValidator extends ExtendedNBABaseVisitor<Boolean> {

    private final List<ModelBuildingError> errors;
    private final SymbolTable table;

    public ModelValidator() {
        this.errors = new LinkedList<>();
        this.table = new SymbolTable();
    }

    public ModelValidator(List<ModelBuildingError> errors) {
        this.errors = errors;
        this.table = new SymbolTable();
    }

    @Override
    public Boolean visitModel(ExtendedNBAParser.ModelContext ctx) {
        boolean valid = true;
        for(ExtendedNBAParser.ElementContext ectx : ctx.element()) {
            valid &= ectx.accept(this);
        }
        return valid;
    }

    @Override
    public Boolean visitSpecies_declaration(ExtendedNBAParser.Species_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addSpecies(name, ctx);
            for (ExtendedNBAParser.Var_declContext vctx : ctx.var_decl()) {
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
    public Boolean visitChannel_declaration(ExtendedNBAParser.Channel_declarationContext ctx) {
        String name = ctx.name.getText();
        try {
            this.table.addChannel(name, ctx);
            for (ExtendedNBAParser.Var_declContext vctx : ctx.var_decl()) {
                this.table.addChannelVar(name, vctx);
            }
        }
        catch(DuplicatedSymbolException e) {
            this.errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitProcess_declaration(ExtendedNBAParser.Process_declarationContext ctx) {
        String name = ctx.name.getText();

        if(!this.table.isASpecies(name)) {
            this.errors.add(ModelBuildingError.unknownSymbol(name, ctx.start.getLine(), ctx.start.getCharPositionInLine()));
        }

        return checkProcessBody(ctx.body, name);
    }

    public Boolean checkProcessBody(ExtendedNBAParser.Process_bodyContext ctx, String species) {
        if(ctx.nil_process() != null) {
            return true;
        } else if(ctx.conditional_process() != null) {
           return ctx.conditional_process().expr().accept(
                   new ExpressionValidator(
                       this.table,
                       this.errors,
                       this.table.getSpeciesVariables(species).orElse(new ArrayList<>()),
                       true,
                       Type.BOOLEAN
                   )
           )
           && checkProcessBody(ctx.conditional_process().then, species)
           && checkProcessBody(ctx.conditional_process().else_,species);
        } else {
            return checkProcessChoice(ctx.choice_process(), species);
        }
    }

    public Boolean checkProcessChoice(ExtendedNBAParser.Choice_processContext ctx, String species) {
        Map<String, ParserRuleContext> outputs = new HashMap<>();
        Map<String, ParserRuleContext> inputs = new HashMap<>();
        for(ExtendedNBAParser.Action_tupleContext actx : ctx.action_tuple()) {
            if(
                    (actx.input_tuple() != null && !checkInputAction(actx.input_tuple(), inputs, species)) ||
                    (actx.output_tuple() != null && !checkOutputAction(actx.output_tuple(), outputs, species))
            ) {
                return false;
            }
        }
        return true;
    }

    public Boolean checkInputAction(ExtendedNBAParser.Input_tupleContext ctx, Map<String, ParserRuleContext> inputs, String species) {
        String channel = ctx.input_action().channel.getText();

        List<Variable> variables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        List<Variable> channelVariables = this.table.getChannelVariables(channel).orElse(new ArrayList<>());
        List<Variable> senderReceiverVariables = Stream.concat(
                variables.stream(),
                channelVariables.stream().map(v -> new Variable("sender." + v.name(), v.type(), v.context()))
        ).toList();

        return  checkChannelExistence(channel, ctx)
                && checkActionDuplicated(channel, inputs, ctx)
                && checkChannelCompatibility(species, channel)
                && ctx.input_action().probability.accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.REAL))
                && ctx.input_action().predicate.accept(new ExpressionValidator(this.table, this.errors, senderReceiverVariables, true, Type.BOOLEAN))
                && checkAgentMutation(ctx.agent_mutation(), senderReceiverVariables);
    }

    public Boolean checkOutputAction(ExtendedNBAParser.Output_tupleContext ctx, Map<String, ParserRuleContext> outputs, String species) {
        String channel = ctx.output_action().channel.getText();

        List<Variable> variables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        List<Variable> channelVariables = this.table.getChannelVariables(channel).orElse(new ArrayList<>());
        List<Variable> senderReceiverVariables = Stream.concat(
                variables.stream(),
                channelVariables.stream().map(v -> new Variable("receiver." + v.name(), v.type(), v.context()))
        ).toList();

        return  checkChannelExistence(channel, ctx)
                && checkActionDuplicated(channel, outputs, ctx)
                && checkChannelCompatibility(species, channel)
                && ctx.output_action().rate.accept(new ExpressionValidator(this.table, this.errors, variables, true, Type.REAL))
                && ctx.output_action().predicate.accept(new ExpressionValidator(this.table, this.errors, senderReceiverVariables, true, Type.BOOLEAN))
                && checkAgentMutation(ctx.agent_mutation(), variables);
    }

    public Boolean checkChannelExistence(String channel, ParserRuleContext ctx) {
        if(!this.table.isAChannel(channel)) {
            this.errors.add(ModelBuildingError.undefinedChannel(channel, ctx));
            return false;
        }
        return true;
    }

    public Boolean checkActionDuplicated(String channel, Map<String, ParserRuleContext> actions, ParserRuleContext ctx) {
        ParserRuleContext duplicated = actions.get(channel);

        if(duplicated != null) {
            this.errors.add(ModelBuildingError.duplicatedAction(ctx, duplicated));
            return false;
        }

        actions.put(channel, ctx);
        return true;
    }

    public Boolean checkChannelCompatibility(String species, String channel) {
        List<Variable> channelVariables = this.table.getChannelVariables(channel).orElse(new ArrayList<>());
        List<Variable> speciesVariables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        for(Variable curChannelVar : channelVariables) {
            Optional<Variable> speciesVariable = speciesVariables.stream()
                    .filter(v -> v.name().equals(curChannelVar.name()) && v.type() == curChannelVar.type())
                    .findFirst();
            if(speciesVariable.isEmpty()) {
                this.errors.add(ModelBuildingError.incompatibleChannel(species, channel));
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitMeasure_declaration(ExtendedNBAParser.Measure_declarationContext ctx) {
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
    public Boolean visitPredicate_declaration(ExtendedNBAParser.Predicate_declarationContext ctx) {
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
    public Boolean visitSystem_declaration(ExtendedNBAParser.System_declarationContext ctx) {
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
    public Boolean visitSystem_composition(ExtendedNBAParser.System_compositionContext ctx) {
        for(ExtendedNBAParser.System_componentContext sctx : ctx.system_component()) {
            if(!checkSystemComponent(sctx)) {
                return false;
            }
        }
        return true;
    }
    private Boolean checkSystemComponent(ExtendedNBAParser.System_componentContext ctx) {
        return checkAgentExpression(ctx.agent_expression(), new ArrayList<>(), false);
    }
    private Boolean checkAgentExpression(ExtendedNBAParser.Agent_expressionContext ctx, List<Variable> variables, boolean modelContext) {
        String species = ctx.name.getText();
        if(!this.table.isASpecies(species)) {
            this.errors.add(ModelBuildingError.unknownSymbol(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        List <Variable> speciesVariables = this.table.getSpeciesVariables(species).orElse(new ArrayList<>());
        List<ExtendedNBAParser.Var_assContext> expressionVars = ctx.var_ass_list().var_ass();
        if(speciesVariables.size() != expressionVars.size()) {
            this.errors.add(ModelBuildingError.incorrectAgentExpression(species, ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return false;
        }
        for(int i=0; i<speciesVariables.size(); ++i) {
            Variable currentSpeciesVariable = speciesVariables.get(i);
            ExtendedNBAParser.Var_assContext currentAssContext = expressionVars.get(i);
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

    private Boolean checkAgentMutation(ExtendedNBAParser.Agent_mutationContext ctx, List<Variable> variables) {
        if(ctx.deterministic_mutation != null) {
            return checkAgentExpression(ctx.deterministic_mutation, variables, true);
        }
        for(ExtendedNBAParser.Stochastic_mutation_tupleContext tctx : ctx.stochastic_mutation_tuple()) {
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
    public Boolean visitAgent_predicate(ExtendedNBAParser.Agent_predicateContext ctx) {
        return ctx.accept(new ExpressionValidator(this.table, this.errors, new ArrayList<>(), true, Type.BOOLEAN));
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
