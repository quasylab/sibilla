package it.unicam.quasylab.sibilla.langs.dopm.symbols;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;

public record Variable(String name, Type type, DataOrientedPopulationModelParser.Var_declContext context) {
}
