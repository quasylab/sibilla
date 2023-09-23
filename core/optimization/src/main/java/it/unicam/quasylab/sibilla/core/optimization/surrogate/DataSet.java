package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.util.*;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

public class DataSet extends Table {
    private final ToDoubleFunction<Map<String,Double>> function;
    private final HyperRectangle searchSpace;
    private final String resultColumnName;

    public DataSet(HyperRectangle searchSpace, SamplingTask samplingTask, int dataSetSize, ToDoubleFunction<Map<String,Double>>function){
        this(searchSpace,samplingTask,dataSetSize,function,DEFAULT_COLUMN_RESULT_NAME);
    }
    public DataSet(HyperRectangle searchSpace, SamplingTask samplingTask, int dataSetSize, ToDoubleFunction<Map<String,Double>> function, String resultColumnName){
        super(DEFAULT_DATASET_NAME);
        this.function = function;
        this.searchSpace = searchSpace;
        this.resultColumnName = resultColumnName;
        Table samples = samplingTask.getSampleTable(dataSetSize,searchSpace);
        this.addColumns(samples.columns().toArray(Column[]::new));
        this.addColumns(computeResultColumn(samples,function,resultColumnName));
    }
    private DataSet(Table trainingSet, HyperRectangle searchSpace, ToDoubleFunction<Map<String,Double>> function, String resultColumnName){
        super(DEFAULT_DATASET_NAME,trainingSet.columns());
        this.searchSpace = searchSpace;
        this.function = function;
        this.resultColumnName = resultColumnName;
    }

    private DoubleColumn computeResultColumn(Table input, ToDoubleFunction<Map<String,Double>> function, String columnID){
        List<Map<String,Double>> listOfRowAsMap = toMapList(input);
        Double[] results = new Double[listOfRowAsMap.size()];
        for (int i = 0; i < listOfRowAsMap.size(); i++) {
            System.out.print("<");
            results[i] = function.applyAsDouble(listOfRowAsMap.get(i));
            System.out.print(">");
        }
        return DoubleColumn.create(columnID,results);
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

    public List<Map<String,Double>> toMapList(){
        List<Map<String,Double>> mapList = new ArrayList<>();
        for (int i = 0; i < this.rowCount(); i++) {
            Row row = this.row(i);
            Map<String,Double> rowMap = new HashMap<>();
            for (int j = 0; j < this.columnCount(); j++) {
                rowMap.put(this.column(j).name(),row.getDouble(j));
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

//    public TrainingSet dropNumberOfResultRowsEqualTo(double value, int numberOfRowToDrop){
//        if(numberOfRowToDrop>= this.rowCount())
//            throw new IllegalArgumentException("you cannot drop more rows than the training set contains ");
//        List<Integer> rowToDropList = new LinkedList<>();
//        for (int i = 0; i < numberOfRowToDrop; i++) {
//            if(this.getResultColumn().get(i) == value)
//                rowToDropList.add(i);
//        }
//        return new TrainingSet( dropRows( rowToDropList.stream().mapToInt(i->i).toArray()),
//                this.getSearchSpace(),
//                this.getFunction(),
//                this.getResultColumnName()
//        );
//    }


    public double getResultMean(){
       return getResultColumn().mean();
    }

    public DataSet filterBy(HyperRectangle searchSpace){
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
        return new DataSet( dropRows( rowToDropList.stream().mapToInt(i->i).toArray()),
                this.getSearchSpace(),
                this.getFunction(),
                this.getResultColumnName());
    }

    public DataSet appendTrainingSet(DataSet dataSetToAppend){
        if(this.columnNames().equals(dataSetToAppend.columnNames())){
            int size = dataSetToAppend.rowCount();
            for (int i = 0; i < size; i++) {
                this.append(dataSetToAppend.row(i));
            }
            return this;
        }else
            throw new IllegalArgumentException("the training set to be added must have " +
                    "the same structure as the current training set");

    }


    public DataSet[] trainTestSplit(double trainingPortion){
        Table[] tables = this.sampleSplit(trainingPortion);
        return new DataSet[]{
                new DataSet(tables[0],this.searchSpace,this.function,this.resultColumnName),
                new DataSet(tables[1],this.searchSpace,this.function,this.resultColumnName)
        };
    }

    public ToDoubleFunction<Map<String,Double>> getFunction() {
        return function;
    }

    public HyperRectangle getSearchSpace() {
        return searchSpace;
    }

    public String getResultColumnName() {
        return resultColumnName;
    }
}
