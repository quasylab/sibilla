package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SampleStrategyFactory;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.util.*;
import java.util.function.Function;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

public class TrainingSet extends Table {
    private Function<Map<String,Double>,Double> function;
    private HyperRectangle searchSpace;

    private String resultColumnName;

    public TrainingSet(HyperRectangle searchSpace, String samplingStrategyName,int trainingSetSize, Function<Map<String,Double>,Double> function){
        this(searchSpace,samplingStrategyName,trainingSetSize,function,DEFAULT_COLUMN_RESULT_NAME);
    }
    public TrainingSet(HyperRectangle searchSpace, String samplingStrategyName, int trainingSetSize, Function<Map<String,Double>,Double> function,String resultColumnName){
        super(DEFAULT_TRAINING_SET_NAME);
        this.function = function;
        this.searchSpace = searchSpace;
        this.resultColumnName = resultColumnName;
        Table samples = SampleStrategyFactory.getSample(samplingStrategyName,trainingSetSize,searchSpace);
        this.addColumns(samples.columns().toArray(Column[]::new));
        this.addColumns(computeResultColumn(samples,function,resultColumnName));
    }

    private DoubleColumn computeResultColumn(Table input, Function<Map<String,Double>,Double> function, String columnID){
        List<Map<String,Double>> listOfRowAsMap = toMapList(input);
        Double[] results = new Double[listOfRowAsMap.size()];
        for (int i = 0; i < listOfRowAsMap.size(); i++) {
            results[i] = function.apply(listOfRowAsMap.get(i));
        }
        return DoubleColumn.create(columnID,results);
    }

    private TrainingSet(Table trainingSet,HyperRectangle searchSpace, Function<Map<String,Double>,Double> function,String resultColumnName){
        super(DEFAULT_TRAINING_SET_NAME,trainingSet.columns());
        this.searchSpace = searchSpace;
        this.function = function;
        this.resultColumnName = resultColumnName;
    }


    private List<Map<String,Double>> toMapList(Table table){
        List<Map<String,Double>> mapList = new ArrayList<>();
        for (int i = 0; i < table.rowCount(); i++) {
            Row row = table.row(i);
            Map<String,Double> rowMap = new HashMap<>();
            for (int j = 0; j < table.columnCount(); j++) {
                rowMap.put(table.column(j).name(),row.getDouble(j));
            }
            mapList.add(rowMap);
        }
        return mapList;
    }

    public DoubleColumn getResultColumn(){
        return (DoubleColumn) this.column(DEFAULT_COLUMN_RESULT_NAME);
    }

    public double getResultSD(){
        return getResultColumn().standardDeviation();
    }

    public double getResultMode(){
        if (this.getResultSD() ==0.0)
            return this.getResultColumn().get(0);
        List<Double> columnResultValues = getResultColumn().asList();
        HashMap<Double,Integer> mapValueCount = new HashMap<>();
        double mode = Double.NaN;
        int currentBestCount = 0;
        for (double value: columnResultValues) {
            if(mapValueCount.containsKey(value))
                mapValueCount.replace(value, mapValueCount.get(value)+1);
            else
                mapValueCount.put(value,1);
            if(mapValueCount.get(value) > currentBestCount){
                mode = value;
                currentBestCount = mapValueCount.get(value);
            }
        }
        return mode;
    }

    public TrainingSet dropNumberOfResultRowsEqualTo(double value, int numberOfRowToDrop){
        if(numberOfRowToDrop>= this.rowCount())
            throw new IllegalArgumentException("you cannot drop more rows than the training set contains ");
        List<Integer> rowToDropList = new LinkedList<>();
        for (int i = 0; i < numberOfRowToDrop; i++) {
            if(this.getResultColumn().get(i) == value)
                rowToDropList.add(i);
        }
        return new TrainingSet( dropRows( rowToDropList.stream().mapToInt(i->i).toArray()),
                this.getSearchSpace(),
                this.getFunction(),
                this.getResultColumnName()
        );
    }


    public double getResultMean(){
       return getResultColumn().mean();
    }

    public TrainingSet filterBy(HyperRectangle searchSpace){
        List<Integer> rowToDropList = new LinkedList<>();
        Map<String, Interval> searchSpaceMap = searchSpace.getIntervalsMappedByID();
        for (int i = 0; i < this.rowCount(); i++) {
            Row currentRow = this.row(i);
            for (String key: searchSpaceMap.keySet()) {
                Interval currentInterval = searchSpaceMap.get(key);
                double valueInRow = currentRow.getDouble(key);
                if(!currentInterval.contains(valueInRow)){
                    rowToDropList.add(i);
                    break;
                }
            }
        }
        return new TrainingSet( dropRows( rowToDropList.stream().mapToInt(i->i).toArray()),
                this.getSearchSpace(),
                this.getFunction(),
                this.getResultColumnName());
    }

    public TrainingSet appendTrainingSet(TrainingSet trainingSetToAppend){
        if(this.columnNames().equals(trainingSetToAppend.columnNames())){
            int size = trainingSetToAppend.rowCount();
            for (int i = 0; i < size; i++) {
                this.append(trainingSetToAppend.row(i));
            }
            return this;
        }else
            throw new IllegalArgumentException("the training set to be added must have " +
                    "the same structure as the current training set");

    }

    public Function<Map<String, Double>, Double> getFunction() {
        return function;
    }

    public HyperRectangle getSearchSpace() {
        return searchSpace;
    }

    public String getResultColumnName() {
        return resultColumnName;
    }
}
