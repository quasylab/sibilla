package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import tech.tablesaw.aggregate.Summarizer;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.util.*;
import java.util.function.Function;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;
import static tech.tablesaw.aggregate.AggregateFunctions.*;

public class TrainingSet extends Table {

    private Summarizer resultsStatistics;

    public TrainingSet(Table sampleSet, Function<Map<String,Double>,Double> function,String resultColumnName){
        super(DEFAULT_TRAINING_SET_NAME,sampleSet.columns());
        List<Map<String,Double>> listOfRowAsMap = toMapList(sampleSet);
        Double[] results = new Double[listOfRowAsMap.size()];
        for (int i = 0; i < listOfRowAsMap.size(); i++) {
            results[i] = function.apply(listOfRowAsMap.get(i));
        }
        this.addColumns(DoubleColumn.create(resultColumnName,results));
        resultsStatistics = this.summarize(DEFAULT_COLUMN_RESULT_NAME,mean,stdDev);
    }

    public TrainingSet(Table sampleSet, Function<Map<String,Double>,Double> function){
        this(sampleSet,function,DEFAULT_COLUMN_RESULT_NAME);
    }

    private TrainingSet(Table trainingSet){
        super(DEFAULT_TRAINING_SET_NAME,trainingSet.columns());
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
        return new TrainingSet( dropRows( rowToDropList.stream().mapToInt(i->i).toArray()));
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



}
