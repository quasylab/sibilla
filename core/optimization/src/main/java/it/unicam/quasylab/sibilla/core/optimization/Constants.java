package it.unicam.quasylab.sibilla.core.optimization;

import tech.tablesaw.api.Table;

import java.util.Map;
import java.util.function.Function;
/**
 * A class to handle constants shared by multiple classes
 *
 * @author      Lorenzo Matteucci
 */
public final class Constants {

    public static final String DEFAULT_INTERVAL_ID = "V";
    public static final String DEFAULT_TRAINING_SET_NAME = "Training Set";
    public static final String DEFAULT_COLUMN_RESULT_NAME = "result";

    public static final String EXCEPT_INTERVALS_WITH_SAME_ID = "there cannot be more intervals with the same identifier";
    public static final String EXCEPT_LOWER_BIGGER_THAN_UPPER = "there cannot be more intervals with the same identifier";
    public static final String EXCEPT_NO_SUCH_SAMPLING_STRATEGY = "there are no sampling strategies with the name provided";

    public static final String EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM = "there are no optimization algorithms with the name provided";
    public static final String EXCEPT_NO_SUCH_SURROGATE = "there are no surrogates with the name provided";

    public static final String EXCEPT_ILLEGAL_CENTER_SIZE = "the array passed as parameter must have a size equal to the number of intervals of the hyper-rectangle";
    public static final String EXCEPT_ILLEGAL_STEP = "the step must be strictly greater than zero";

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
    public static final Function<Map<String,Double>,Double> EGG_HOLDER_FUNCTION = dictionary ->{
        double x = dictionary.get("x");
        double y = dictionary.get("y");
        return (-1)*(y+47)*Math.sin(Math.sqrt(Math.abs(y+(x/2)+47)))-x*Math.sin(Math.sqrt(Math.abs(x-(y+47))));
    };
    /**
     * In mathematical optimization, the Rosenbrock function is a non-convex function,
     * introduced by Howard H. Rosenbrock in 1960, which is used as a performance test
     * problem for optimization algorithms.
     *
     * <b> MINIMA : </b> has exactly one minimum for N=3 (at (1, 1, 1)) and
     * exactly two minima for 4 <= N <= 7 the global minimum of all ones
     * and a local minimum near (-1,1, ... ,1).
     *
     * @see    <a href=https://en.wikipedia.org/wiki/Rosenbrock_function">Rosenbrock_function</a>
     */
    public static final Function<Map<String,Double>,Double> ROSENBROCK_FUNCTION = (
            map -> {
                String[] keyList = map.keySet().toArray(new String[0]);
                double sum = 0.0;
                for (int i = 0; i < keyList.length - 1 ; i++){
                    sum += 100 * Math.pow(map.get(keyList[i+1]) - Math.pow(map.get(keyList[i]),2),2)+Math.pow((1-map.get(keyList[i])),2);
                }
                return sum;
            }
    );


    private Constants(){}

}
