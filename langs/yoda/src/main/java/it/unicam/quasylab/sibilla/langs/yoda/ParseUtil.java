package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class ParseUtil {
    private static final String DUPLICATED_ID_ERROR = "Identifier %s has been already used at line %d:%d.";

    public static ParseError duplicatedIdentifierError(String name, Token duplicated, Token original){
        return new ParseError(
                String.format(DUPLICATED_ID_ERROR, name, original.getLine(), original.getCharPositionInLine()),
                duplicated.getLine(),
                duplicated.getCharPositionInLine());
    }
}
