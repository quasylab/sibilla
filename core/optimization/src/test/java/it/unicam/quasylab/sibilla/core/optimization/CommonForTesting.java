package it.unicam.quasylab.sibilla.core.optimization;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public class CommonForTesting {

    /**
     * <b>Description : </b> The Egg holder function is a difficult function to optimize,
     * because of the large number of local minima.<br>
     *
     * <b>Input Domain : </b> The function is usually evaluated for x,y
     * between -512 and 512 <br>
     *
     * <b>Global Minimum : </b>  minimum: -959.6407 at ( x = 512 , y = 404.2319 )  <br>
     *
     * @see    <a href=https://www.sfu.ca/~ssurjano/egg.html">function reference</a>
     */
    //@SuppressWarnings({"UnusedDeclaration"})
    public static final ToDoubleFunction<Map<String,Double>> EGG_HOLDER_FUNCTION = dictionary ->{
        double x = dictionary.get("x");
        double y = dictionary.get("y");
        return (-1)*(y+47)*Math.sin(Math.sqrt(Math.abs(y+(x/2)+47)))-x*Math.sin(Math.sqrt(Math.abs(x-(y+47))));
    };
    /**
     * In mathematical optimization, the Rosenbrock function is a non-convex function,
     * introduced by Howard H. Rosenbrock in 1960, which is used as a performance test
     * problem for optimization algorithms.
     *
     *
     * In the mapping the variables are named "x0" "x1" "x2" ... and so on
     *
     * <b> MINIMA : </b> has exactly one minimum for N=3 (at (1, 1, 1)) and
     * exactly two minima for 4 <= N <= 7 the global minimum of all ones
     * and a local minimum near (-1,1, ... ,1).
     *
     * @see    <a href=https://en.wikipedia.org/wiki/Rosenbrock_function">Rosenbrock_function</a>
     */
    public static final ToDoubleFunction<Map<String,Double>> ROSENBROCK_FUNCTION = (
            map -> {
                String[] keyList = map.keySet().toArray(new String[0]);
                double sum = 0.0;
                for (int i = 0; i < keyList.length - 1 ; i++){
                    sum += 100 * Math.pow(map.get(keyList[i+1]) - Math.pow(map.get(keyList[i]),2),2)+Math.pow((1-map.get(keyList[i])),2);
                }
                return sum;
            }
    );

    /**
     *
     * z = x*exp(-(x^2+y^2))
     *
     * In the mapping, the variables are named "x" and "y"
     *
     * Local
     * Min : x = - 0.707  y = 0
     * Max : x =   0.707  y = 0
     *
     * @see    <a href=https://www.wolframalpha.com/input?i=x*exp%28-%28x%5E2%2By%5E2%29%29</a>
     */
    public static final ToDoubleFunction<Map<String,Double>> SIMPLE_FUNCTION = map ->{
        double x = map.get("x");
        double y = map.get("y");
        return x*Math.exp(-(Math.pow(x,2.0)+Math.pow(y,2.0)));
    };




    static public String getCsvFromTable(Table table){
        StringBuilder csvString = new StringBuilder();

        List<String> titles = table.columnNames();
        int numberOfColumn = table.columnCount();
        int numberOfRow = table.rowCount();


        for (int i = 0; i < numberOfColumn-1; i++) {
            csvString.append(titles.get(i)).append(",");
        }

        csvString.append(titles.get(numberOfColumn-1)).append("\n");

        for (int i = 0; i < numberOfRow; i++) {
            Row row = table.row(i);
            for (int j = 0; j < numberOfColumn-1; j++) {
                csvString.append(row.getDouble(j)).append(",");
            }
            csvString.append(row.getDouble(numberOfColumn-1)).append("\n");
        }
        return csvString.toString();
    }

    public static String convertListToCSV(List<Map<String, Double>> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // add the column headers to the CSV string
        Map<String, Double> firstRow = list.get(0);
        for (String key : firstRow.keySet()) {
            sb.append(key).append(",");
        }
        sb.deleteCharAt(sb.length() - 1); // remove the last comma
        sb.append("\n");

        // add the data rows to the CSV string
        for (Map<String, Double> row : list) {
            for (Double value : row.values()) {
                sb.append(value).append(",");
            }
            sb.deleteCharAt(sb.length() - 1); // remove the last comma
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Example :
     *
     * String myString = "Hello, world!";
     * String myFilePath = "/path/to/myfile.csv";
     * writeStringToCSV(myString, myFilePath);
     *
     * @param inputString the string
     * @param filePath the path
     */
    public static void writeStringToCSV(String inputString, String filePath) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath);
            fileWriter.append(inputString);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public boolean validatePredictedResult(double correct, double predicted, double maxError) {
        return Math.abs(correct-predicted) <= maxError;
    }
}
