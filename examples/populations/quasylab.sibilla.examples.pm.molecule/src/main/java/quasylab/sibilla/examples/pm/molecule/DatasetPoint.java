package quasylab.sibilla.examples.pm.molecule;

import java.util.ArrayList;

public class DatasetPoint implements Comparable{

    public ArrayList<Double> datasetPointInput = new ArrayList<>();
    public ArrayList<Double> datasetPointOutput = new ArrayList<>();

    public DatasetPoint(){

    }

    public void addSingleInput(double input){
        datasetPointInput.add(input);
    }

    public void addSingleOutput(double input){
        datasetPointOutput.add(input);
    }

    public void addListInput(ArrayList<Double> input){
        datasetPointInput.addAll(input);
    }

    public void addListOutput(ArrayList<Double> input){
        datasetPointOutput.addAll(input);
    }

    public String getCSVInputString(){
        String dataPointString = "";

        for (double dataInp :datasetPointInput ) {
            dataPointString = dataPointString + dataInp + ",";
        }

        return dataPointString.substring(0,dataPointString.length() -1);
    }

    public String getCSVOutputString(){
        String dataPointString = "";

        for (double dataInp :datasetPointOutput ) {
            dataPointString = dataPointString + dataInp + ",";
        }

        return dataPointString.substring(0,dataPointString.length() -1);
    }

    public String getCSVString(){
        return getCSVInputString()+","+getCSVOutputString();
    }

    @Override
    public String toString() {

        String dataPointString = "";

        for (double dataInp :datasetPointInput ) {
            dataPointString = dataPointString + dataInp + " ";
        }

        dataPointString = dataPointString + " --> ";

        for (double dataOutp :datasetPointOutput ) {
            dataPointString = dataPointString + dataOutp + " ";
        }

        return dataPointString;
    }

    @Override
    public int compareTo(Object o) {
        int ret = 0;

        DatasetPoint other = (DatasetPoint) o;

        if(this.datasetPointInput.get(0)>other.datasetPointInput.get(0))
            ret = 1;
        else
            ret = -1;

        return ret;
    }
}
