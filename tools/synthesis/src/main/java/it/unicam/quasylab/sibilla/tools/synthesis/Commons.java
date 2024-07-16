package it.unicam.quasylab.sibilla.tools.synthesis;

/**
 * A class to handle constants shared by multiple classes
 *
 * @author      Lorenzo Matteucci
 */
public final class Commons {

    public static final String DEFAULT_INTERVAL_ID = "V";
    public static final String DEFAULT_DATASET_NAME = "Training Set";
    public static final String DEFAULT_COLUMN_RESULT_NAME = "result";

    public static final String EXCEPT_INTERVALS_WITH_SAME_ID = "there cannot be more intervals with the same identifier";
    public static final String EXCEPT_LOWER_BIGGER_THAN_UPPER = "lower is bigger than upper";

    public static final String EXCEPT_NO_SUCH_SAMPLING_STRATEGY = "there are no sampling strategies with the name provided";
    public static final String EXCEPT_NO_SUCH_OPTIMIZATION_ALGORITHM = "there are no optimization algorithms with the name provided";
    public static final String EXCEPT_NO_SUCH_SURROGATE = "there are no surrogates with the name provided";

    public static final String EXCEPT_ILLEGAL_CENTER_SIZE = "the array passed as parameter must have a size equal to the number of intervals of the hyper-rectangle";
    public static final String EXCEPT_ILLEGAL_STEP = "the step must be strictly greater than zero";


    private Commons(){}

}
