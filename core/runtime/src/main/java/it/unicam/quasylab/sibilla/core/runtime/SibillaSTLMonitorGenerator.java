package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.antlr.v4.runtime.CharStreams;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class SibillaSTLMonitorGenerator<S extends State> {

    String sourceCode;
    Map<String, ToDoubleFunction<S>> measuresMap;

    public SibillaSTLMonitorGenerator(File file, Map<String, ToDoubleFunction<S>> measuresMap) throws IOException {
        this(String.valueOf(CharStreams.fromReader(new FileReader(file))),measuresMap);
    }

    public SibillaSTLMonitorGenerator(String sourceCode, Map<String, ToDoubleFunction<S>> measuresMap) {
        this.sourceCode = sourceCode;
        this.measuresMap = measuresMap;
    }

    public StlMonitorFactory<S> getStlMonitorFactory() throws StlModelGenerationException {
        return new StlLoader(sourceCode).getModelFactory(this.measuresMap);
    }


    public QualitativeMonitor<S> getQualitativeMonitor(String name, double[] args) throws StlModelGenerationException {
        return this.getStlMonitorFactory().getQualitativeMonitor(name, args);
    }

    public QuantitativeMonitor<S> getQuantitativeMonitor(String name, double[] args) throws StlModelGenerationException {
        return this.getStlMonitorFactory().getQuantitativeMonitor(name,args);
    }
}

