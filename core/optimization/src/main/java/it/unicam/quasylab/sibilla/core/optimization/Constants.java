package it.unicam.quasylab.sibilla.core.optimization;

import java.util.Map;
import java.util.function.Function;

public final class Constants {

    public static final String DEFAULT_INTERVAL_ID = "V";
    public static final String DEFAULT_TRAINING_SET_NAME = "Training Set";
    public static final String DEFAULT_COLUMN_RESULT_NAME = "result";

    public static final String EXCEPT_INTERVALS_WITH_SAME_ID = "there cannot be more intervals with the same identifier";
    public static final String EXCEPT_LOWER_BIGGER_THAN_UPPER = "there cannot be more intervals with the same identifier";
    public static final String EXCEPT_NO_SUCH_SAMPLING_STRATEGY = "there are no sampling strategies with the name provided";

    public static final String EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM = "there are no optimization algorithms with the name provided";
    public static final String EXCEPT_NO_SUCH_SURROGATE = "there are no surrogates with the name provided";

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
    public static final Function<Map<String,Double>,Double> EGG_HOLDER_FUNCTION = dictionary ->{
        double x = dictionary.get("x");
        double y = dictionary.get("y");
        return (-1)*(y+47)*Math.sin(Math.sqrt(Math.abs(y+(x/2)+47)))-x*Math.sin(Math.sqrt(Math.abs(x-(y+47))));
    };

    private Constants(){}
}
