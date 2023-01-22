package it.unicam.quasylab.sibilla.core.optimization.surrogate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_NO_SUCH_SURROGATE;
/**
 * Provides static methods for the creation of Surrogates.
 *
 * @author      Lorenzo Matteucci
 */
public class SurrogateFactory {
    /**
     * Produces a surrogate given the name of it and its properties
     *
     * @author      Lorenzo Matteucci
     */

    public static Surrogate getSurrogate(String surrogateName, Properties properties){
        if(surrogateName.equals("rfr"))
            return new RandomForestSurrogate(properties);
        if(surrogateName.equals("gtb"))
            return new GradientTreeBoostSurrogate(properties);
        if(surrogateName.equals("rbf"))
            return new RBFNetworkSurrogate(properties);
        else
            throw new IllegalArgumentException(EXCEPT_NO_SUCH_SURROGATE + " : "
                    + surrogateName + "\n the available surrogates are: \n"+
                    getSurrogatesList().stream().reduce("",(a,b)-> a + b + "\n"));
    }

    public static List<String> getSurrogatesList(){
        return  Arrays.stream(new String[]{
                "rfr", // Random Forest
                "gtb", // GradientTreeBoostSurrogate
                "rbf"  // Radial Basis Function network
        }).toList();
    }
}
