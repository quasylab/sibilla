package it.unicam.quasylab.sibilla.langs.enba.symbols;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;

public record Variable(String name, Type type, ExtendedNBAParser.Var_declContext context) {
}
