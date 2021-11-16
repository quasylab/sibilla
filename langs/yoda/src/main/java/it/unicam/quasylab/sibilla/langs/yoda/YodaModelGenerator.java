package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class YodaModelGenerator {

    private final CodePointCharStream source;
    private ModelValidator validator;
    private final ErrorCollector errorCollector = new ErrorCollector();
    private boolean validated = false;
    private YodaModelParser.ModelContext parseTree;




    public YodaModelGenerator(CodePointCharStream source){ this.source = source; }

    public YodaModelGenerator(String string){ this(CharStreams.fromString(string));}

    public ParseTree getParseTree(){
        if (this.parseTree==null){
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        YodaModelLexer lexer = new YodaModelLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        YodaModelParser parser = new YodaModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener(errorCollector);
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();

    }
}
