package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;


import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *  could be useful here make a mechanism to re-train
 *  the surrogate in case minimum don't converge
 */
//TODO : DEMO Class, actually not properly working
public class SurrogateOptimizationAdaptiveSearchSpace implements OptimizationStrategy{

    private String surrogateName;
    private String optimizationName;

    private String samplingName;
    private Function<Map<String,Double>,Double> functionToBeSurrogate;

    private Function<Map<String,Double>,Double> surrogateFunction;
    private List<Predicate<Map<String,Double>>> constraints;
    private HyperRectangle searchSpace;

    private HyperRectangle ongoingSearchSpace;
    private Properties surOptProperties;

    private TrainingSetEvalCriteria trainingSetEvalCriteria;
    private TrainingSetRearranger trainSetRearranger;

    private Map<String,Double> optimalSolutionFounded;
    private int trainingSetSize = 1000;
    private int maxIterationResampling = 10;

    public SurrogateOptimizationAdaptiveSearchSpace(String surrogateName,
                                                    String optimizationName,
                                                    Function<Map<String,Double>,Double> functionToBeSurrogate,
                                                    List<Predicate<Map<String,Double>>> constraints,
                                                    String samplingName,
                                                    HyperRectangle searchSpace,
                                                    int trainingSetSize,
                                                    TrainingSetEvalCriteria evalCriteria,
                                                    TrainingSetRearranger tsRearranger,
                                                    Properties surOptProperties){
        this.surrogateName = surrogateName;
        this.optimizationName = optimizationName;
        this.functionToBeSurrogate = functionToBeSurrogate;
        this.constraints =constraints;
        this.searchSpace = searchSpace;
        this.samplingName = Optional.ofNullable(samplingName).orElse("lhs");
        this.surOptProperties = surOptProperties;
        this.trainingSetSize = trainingSetSize;
        this.trainingSetEvalCriteria = Optional.ofNullable(evalCriteria).orElse(ts -> ts.getResultSD() != 0);

        TrainingSetRearranger defaultTsModifier = ts ->{
            HyperRectangle doubledSearchSpace = ts.getSearchSpace().getScaledCopy(2.0);
            TrainingSet HalvedTS = ts.dropNumberOfResultRowsEqualTo(ts.getResultMode(),(int) ts.rowCount()/2);
            return HalvedTS.appendTrainingSet(new TrainingSet(doubledSearchSpace,"lhs",ts.rowCount(),ts.getFunction()));
        };

        this.trainSetRearranger = Optional.ofNullable(tsRearranger).orElse(defaultTsModifier);

    }

    private  Function<Map<String,Double>,Double> generateSurrogateFunction(Surrogate surrogateModel,TrainingSet ts){
        surrogateModel.fit(ts);
        return map -> surrogateModel.predict(map.values().toArray(new Double[0]));
    }


    private TrainingSet generateTrainingSet(int maxIterationResampling, HyperRectangle searchSpace){
        TrainingSet trainingSet = new TrainingSet(searchSpace,samplingName,trainingSetSize,this.functionToBeSurrogate);
        while (!trainingSetEvalCriteria.eval(trainingSet) || maxIterationResampling==0 ){
            trainingSet= trainSetRearranger.rearrange(trainingSet);
            maxIterationResampling--;
        }
        return trainingSet;
    }

    public void setSearchSpaceAsConstraints(){
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            constraints.add( map -> i.getLowerBound() <= map.get(i.getId()) && map.get(i.getId()) <= i.getUpperBound() );
        }
        this.constraints.addAll(constraints);
    }


    private void setUp(int maxIterationResampling, HyperRectangle searchSpace){
        TrainingSet trainingSet = new TrainingSet(searchSpace,samplingName,trainingSetSize,functionToBeSurrogate);
        while (!trainingSetEvalCriteria.eval(trainingSet) || maxIterationResampling==0 ){
            trainingSet= trainSetRearranger.rearrange(trainingSet);
            maxIterationResampling--;
        }
        this.ongoingSearchSpace = trainingSet.getSearchSpace();
        this.surrogateFunction = generateSurrogateFunction(
                SurrogateFactory.getSurrogate(this.surrogateName,this.surOptProperties),
                trainingSet
        );
    }


    @Override
    public Map<String, Double> minimize() {

        TrainingSet trainingSet = generateTrainingSet(this.trainingSetSize,this.searchSpace);
        System.out.println(this.searchSpace);
        return minimizationIteration(3,trainingSet);
//        setUp(this.maxIterationResampling,this.searchSpace);
//
//        return OptimizationStrategyFactory.getOConstrainedOptimizationStrategy(
//                this.optimizationName,
//                this.surrogateFunction,
//                this.constraints,
//                this.ongoingSearchSpace,
//                this.surOptProperties
//        ).minimize();
    }

    private Map<String,Double> minimizationIteration(int iteration, TrainingSet trainingSet){

        Function<Map<String,Double>,Double> surrogateFunc = generateSurrogateFunction(
                SurrogateFactory.getSurrogate(this.surrogateName,this.surOptProperties),
                trainingSet
        );


        Map<String,Double> optimalFound = OptimizationStrategyFactory.getOConstrainedOptimizationStrategy(
                this.optimizationName,
                surrogateFunc,
                this.constraints,
                trainingSet.getSearchSpace(),
                this.surOptProperties
        ).minimize();
        System.out.println(optimalFound.toString());
        if(iteration!=0){
            System.out.println("iteration shrink : " + iteration);
            HyperRectangle newSearchSpace = trainingSet.getSearchSpace().getScaledCopy(0.2);
            newSearchSpace.changeCenter(optimalFound);
            System.out.println("REDUCED : ");
            System.out.println(newSearchSpace);
            TrainingSet newTrainingSet = trainingSet.filterBy(newSearchSpace);
            newTrainingSet.appendTrainingSet(generateTrainingSet(0,newSearchSpace));
            iteration--;
            return minimizationIteration(iteration,newTrainingSet);
        }
        return optimalFound;
    }


//    private void minimizationRecursion(int currentStep){
//        if(currentStep!=0){
//            this.optimalSolutionFounded = OptimizationStrategyFactory.getOConstrainedOptimizationStrategy(
//                    this.optimizationName,
//                    this.surrogateFunction,
//                    this.constraints,
//                    this.ongoingSearchSpace,
//                    this.surOptProperties
//            ).minimize();
//            currentStep--;
//            minimizationRecursion(currentStep);
//        }
//    }

    @Override
    public Map<String, Double> maximize() {
        return null;
    }
}
