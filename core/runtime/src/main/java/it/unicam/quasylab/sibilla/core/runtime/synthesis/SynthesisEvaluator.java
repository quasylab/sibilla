package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.Synthesizer;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.getAsMap;

public class SynthesisEvaluator {

    private final Map<String,Object> data;
    private final SynthesisStrategy strategy;
    private final SynthesisTask task;

    public SynthesisEvaluator(String source) {
        Yaml yaml = new Yaml();

        this.data = yaml.load(source);


        Map<String,Object> strategyMap = getAsMap(this.data.get("synthesisStrategy"));
        Map<String,Object> taskMap = getAsMap(this.data.get("synthesisTask")) ;

        strategy = new SynthesisStrategy(strategyMap);
        task = SynthesisTaskFactory.createTask(getTaskName(), taskMap,strategy);
    }

    public boolean validateData( Map<String,Object> map){
        return data.containsKey("");
    }

    public SynthesisStrategy getStrategy() {
        return strategy;
    }

    public SynthesisTask getTask() {
        return SynthesisTaskFactory.createTask(getTaskName(), getAsMap(this.data.get("synthesisTask")),strategy);
    }

    public TaskName getTaskName() {
        String taskName = getAsMap(this.data.get("synthesisTask")).get("type").toString();
        return TaskName.getName(taskName);
    }
}
