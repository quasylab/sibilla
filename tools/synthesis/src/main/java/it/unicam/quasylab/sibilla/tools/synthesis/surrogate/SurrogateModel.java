package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

/**
 * An object representing a surrogate model, each surrogate must be able to:
 * <ul>
 *     <li>Predict</li>
 *     <li>Be trained</li>
 *     <li>Return metrics</li>
 * </ul>
 *
 * @author      Lorenzo Matteucci
 */
public interface SurrogateModel {

    /**
     * Return a function that use the surrogate
     *
     * @return the function trained by using the training set
     */
    ToDoubleFunction<Map<String,Double>> getSurrogateFunction(boolean performTraining);

    ToDoubleFunction<Map<String,Double>> getSurrogateFunction();

    void fit();
    /**
     * Return an RegressionMetrics object that contains the surrogate metrics.
     * This metrics information includes:
     * <ul>
     *  <li> <strong>fitTime</strong> the time needed to train the surrogate
     *  <li> <strong>scoreTime</strong> the score time
     *  <li> <strong>size</strong> the validation data size
     *  <li> <strong>rss</strong> the residual sum of squares(RSS)
     *       A small RSS indicates a tight fit of the model to the data.
     *       It is used as an optimality criterion in parameter
     *       selection and model selection.
     *  <li> <strong>MSE</strong> the mean squared error (MSE) It measures
     *      the variance of the residuals.
     *  <li> <strong>RMSE</strong> the Root Mean Squared Error (RMSE) is the square
     *      root of Mean Squared error.Measures the mean square magnitude of errors.
     *      Root square is taken to make the units of the error be the same as the
     *      units of the target. This measure gives more weight to large deviations
     *      such as outliers, since large differences squared become larger and small
     *      (smaller than 1) differences squared become smaller.
     *  <li> <strong>MAD</strong> the mean absolute Deviation (MAD). This is a
     *  spread metric similar to standard deviation but meant to be more robust
     *  to outliers. Instead of taking means of squares as the sd, MAD takes
     *  medians of absolutes making it more robust.
     *  <li> <strong>R2</strong> The coefficient of determination or R-squared
     *  represents the proportion of the variance in the dependent variable
     *  which is explained by the linear regression model. It is a scale-free
     *  score i.e. irrespective of the values being small or large,
     *  the value of R square will be less than one the closer to 1 the better
     *  Is a measure of the ratio of variability that your model can capture
     *  vs the natural variability in the target variable.
     *
     * </ul>
     * @see <a href="https://medium.com/analytics-vidhya/mae-mse-rmse-coefficient-of-determination-adjusted-r-squared-which-metric-is-better-cd0326a5697e">Useful website</a>
     * @return   the surrogate metrics
     */
    SurrogateMetrics getInSampleMetrics();
    SurrogateMetrics getOutOfSampleMetrics();
    void setProperties( Properties properties);
    Properties getProperties();
    void setProperty(String key,String value);
    void setSeed(long seed);


}















//public interface SurrogateModel {
//
//    /**
//     * Return a function that use the surrogate
//     *
//     * @param trainingSet the training set used to train the surrogate
//     * @param properties the properties for the surrogate model
//     * @return the function trained by using the training set
//     */
//    default ToDoubleFunction<Map<String,Double>> getSurrogateFunction(TrainingSet trainingSet, Properties properties){
//        this.setProperties(properties);
//        this.fit(trainingSet);
//        List<String> orderedNames = trainingSet.columnNames();
//        orderedNames.remove(orderedNames.size()-1);
//        return input -> this.predict(orderedNames.stream().map(input::get).toArray(Double[]::new));
//    };
//
//    /**
//     * Return a predicted value by passing it a vector of parameters
//     * to the surrogate regression model
//     *
//     * @param  inputVector the input vector
//     * @return      the predicted Value
//     */
//    double predict(Double[] inputVector);
//    /**
//     * Train the surrogate model by passing a training set
//     *
//     * @param  trainingSet The training set with which the surrogate
//     *                     is trained.
//     */
//    void fit(TrainingSet trainingSet);
//
//    /**
//     * Return an RegressionMetrics object that contains the surrogate metrics.
//     * This metrics information includes:
//     * <ul>
//     *  <li> <strong>fitTime</strong> the time needed to train the surrogate
//     *  <li> <strong>scoreTime</strong> the score time
//     *  <li> <strong>size</strong> the validation data size
//     *  <li> <strong>rss</strong> the residual sum of squares(RSS)
//     *       A small RSS indicates a tight fit of the model to the data.
//     *       It is used as an optimality criterion in parameter
//     *       selection and model selection.
//     *  <li> <strong>MSE</strong> the mean squared error (MSE) It measures
//     *      the variance of the residuals.
//     *  <li> <strong>RMSE</strong> the Root Mean Squared Error (RMSE) is the square
//     *      root of Mean Squared error.Measures the mean square magnitude of errors.
//     *      Root square is taken to make the units of the error be the same as the
//     *      units of the target. This measure gives more weight to large deviations
//     *      such as outliers, since large differences squared become larger and small
//     *      (smaller than 1) differences squared become smaller.
//     *  <li> <strong>MAD</strong> the mean absolute Deviation (MAD). This is a
//     *  spread metric similar to standard deviation but meant to be more robust
//     *  to outliers. Instead of taking means of squares as the sd, MAD takes
//     *  medians of absolutes making it more robust.
//     *  <li> <strong>R2</strong> The coefficient of determination or R-squared
//     *  represents the proportion of the variance in the dependent variable
//     *  which is explained by the linear regression model. It is a scale-free
//     *  score i.e. irrespective of the values being small or large,
//     *  the value of R square will be less than one the closer to 1 the better
//     *  Is a measure of the ratio of variability that your model can capture
//     *  vs the natural variability in the target variable.
//     *
//     * </ul>
//     * @see <a href="https://medium.com/analytics-vidhya/mae-mse-rmse-coefficient-of-determination-adjusted-r-squared-which-metric-is-better-cd0326a5697e">Useful website</a>
//     * @return   the surrogate metrics
//     */
//    SurrogateMetrics getInSampleMetrics();
//    SurrogateMetrics getOutOfSampleMetrics(TrainingSet outOfSampleTrainingSet);
//    void setProperties( Properties properties);
//}
