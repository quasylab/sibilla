package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.getAsMap;

public class SynthesisEvaluator {

    private final Map<String,Object> data;
    private final SynthesisStrategy strategy;

    public SynthesisEvaluator(String source) {
        Yaml yaml = new Yaml();
        this.data = yaml.load(source);

        Map<String,Object> strategyMap = getAsMap(this.data.get("synthesisStrategy"));
        Map<String,Object> taskMap = getAsMap(this.data.get("synthesisTask")) ;

        strategy = new SynthesisStrategy(strategyMap);
        //SynthesisTask task = SynthesisTaskFactory.createTask(getTaskName(), taskMap, strategy);
    }

    public SynthesisEvaluator(File sourceFile) throws IOException {
        if (!sourceFile.getName().toLowerCase().endsWith(".yml") &&
                !sourceFile.getName().toLowerCase().endsWith(".yaml")) {
            throw new IllegalArgumentException("The provided file is not a YAML file.");
        }

        String content = Files.readString(sourceFile.toPath());
        Yaml yaml = new Yaml();
        this.data = yaml.load(content);

        Map<String,Object> strategyMap = getAsMap(this.data.get("synthesisStrategy"));
        Map<String,Object> taskMap = getAsMap(this.data.get("synthesisTask")) ;

        strategy = new SynthesisStrategy(strategyMap);

    }

    public SynthesisStrategy getStrategy() {
        return strategy;
    }

    public SynthesisTask getTask()  {
        return SynthesisTaskFactory.createTask(getTaskName(), getAsMap(this.data.get("synthesisTask")),strategy);
    }

    public TaskName getTaskName() {
        String taskName = getAsMap(this.data.get("synthesisTask")).get("type").toString();
        return TaskName.getName(taskName);
    }
}
