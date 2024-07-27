package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.core.runtime.CommandExecutionException;
import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord;
import it.unicam.quasylab.sibilla.tools.synthesis.Synthesizer;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;


import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.*;

public abstract class SynthesisTask {

    protected TaskName taskName;
    protected double deadline;
    protected int replica;
    protected double dt;

    List<SynthesisRecord> synthesisRecords;

    SynthesisStrategy synthesisStrategy;

    Set<String> modelParameterNames;


    public SynthesisTask(Map<String,Object> taskMap,SynthesisStrategy synthesisStrategy) {

        if(!taskMap.containsKey("type"))
            throw new IllegalArgumentException("taskName not found!");
        taskName = TaskName.getName(taskMap.get("type").toString());

        if(!taskMap.containsKey("simulationSetting"))
            setDefaultSimulationSetting();
        else
            setSimulationSetting(taskMap);
        this.synthesisStrategy = synthesisStrategy;
    }

    public List<SynthesisRecord> getSynthesisResults(){
        return synthesisRecords;
    }

    public SynthesisRecord getSynthesisResult(){
        return synthesisRecords.get(synthesisRecords.size()-1);
    }

    private void setSimulationSetting(Map<String, Object> taskMap) {
        Map<String,Object> simulationSpecs = getAsMap(taskMap.get("simulationSetting"));
        this.deadline = simulationSpecs.containsKey("deadline") ? (double) simulationSpecs.get("deadline") : DEFAULT_DEADLINE;
        this.dt = simulationSpecs.containsKey("dt") ? (double) simulationSpecs.get("dt") : DEFAULT_DT;
        this.replica = simulationSpecs.containsKey("replica") ? (int) simulationSpecs.get("replica") : DEFAULT_REPLICA;

    }

    private void setDefaultSimulationSetting() {
        this.deadline = Common.DEFAULT_DEADLINE;
        this.replica = Common.DEFAULT_REPLICA;
        this.dt = Common.DEFAULT_DT;
    }

    protected Synthesizer generateSynthesizer(ToDoubleFunction<Map<String,Double>> objectiveFunction, boolean isMinimization) {

        return new Synthesizer(
                synthesisStrategy.getOptimizationName(),
                synthesisStrategy.getSurrogateName(),
                synthesisStrategy.getSamplingName(),
                isMinimization,
                objectiveFunction,
                synthesisStrategy.getSearchSpace(),
                synthesisStrategy.getDataSetSize(),
                synthesisStrategy.getTrainingDatasetPortion(),
                synthesisStrategy.getConstraints() == null ? new String[]{} : synthesisStrategy.getConstraints(),
                synthesisStrategy.getProperties(),
                true,
                synthesisStrategy.getPerformInfill()

        );
    }

    public abstract SynthesisRecord execute(SibillaRuntime runtime) throws CommandExecutionException, StlModelGenerationException;


    protected String[] availableModules(){
        return SibillaModule.MODULES.stream()
                .map(SibillaModule::getModuleName)
                .toList().toArray(new String[0]);
    }



    protected void setModelParameters(SibillaRuntime sr, Map<String, Double> parameters) {
        for (String paramName : parameters.keySet()) {
            if(this.modelParameterNames.contains(paramName))
                sr.setParameter(paramName, parameters.get(paramName));
        }
    }

    protected void setModelParameterNames(SibillaRuntime sr){
        this.modelParameterNames = Set.of(sr.getParameters());
    }

}
