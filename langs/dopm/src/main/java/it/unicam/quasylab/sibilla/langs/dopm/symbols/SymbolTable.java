package it.unicam.quasylab.sibilla.langs.dopm.symbols;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.exceptions.DuplicatedSymbolException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.Set;

public interface SymbolTable {

    void addMeasure(String name, DataOrientedPopulationModelParser.Measure_declarationContext context) throws DuplicatedSymbolException;

    void addPredicate(String name, DataOrientedPopulationModelParser.Predicate_declarationContext context) throws DuplicatedSymbolException;

    void addSpecies(String name, DataOrientedPopulationModelParser.Species_declarationContext context) throws DuplicatedSymbolException;

    void addRule(String name, DataOrientedPopulationModelParser.Rule_declarationContext context) throws DuplicatedSymbolException;

    void addSystem(String name, DataOrientedPopulationModelParser.System_declarationContext context) throws DuplicatedSymbolException;

    String[] rules();

    String[] species();

    String[] systems();

    String[] measures();

    boolean isAMeasure(String name);

    boolean isASpecies(String name);

    boolean isARule(String name);

    ParserRuleContext getContext(String name);

    boolean isDefined(String name);

    DataOrientedPopulationModelParser.Species_declarationContext getSpeciesContext(String name);

    DataOrientedPopulationModelParser.Rule_declarationContext getRuleContext(String name);

    DataOrientedPopulationModelParser.Measure_declarationContext getMeasureContext(String name);

    DataOrientedPopulationModelParser.System_declarationContext getSystemContext(String name);

    Set<String> checkLocalVariables(DataOrientedPopulationModelParser.Var_ass_listContext local_variables) throws DuplicatedSymbolException;

    Set<String> checkLocalVariables(List<Token> args, ParserRuleContext ctx) throws DuplicatedSymbolException;

}
