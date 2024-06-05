package it.unicam.quasylab.sibilla.core.optimization.sampling.interval;


/**
 * A class that represent an interval between two numbers
 * An Interval have :
 * <ul>
 * <li>An identifier
 * <li>A lower bound
 * <li>an upper bound
 * </ul>
 *
 * you can also specify whether the interval is continuous or discrete
 *
 * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
 */
public interface Interval {
    String getId();
    double getLowerBound();
    double getUpperBound();
    boolean isDiscrete();
    boolean isContinuous();

    /**
     * Return the length between the lower and upperbound
     * @return the length
     */
    double length();

    /**
     * Scale the length of the interval
     * @param scaleFactor the scale multiplier
     */
    void scale(double scaleFactor);

    /**
     * Return a uniform random number inside the interval
     * @return a random value inside the interval
     */
    double getRandomValue();

    void setSeed(long seed);

    /**
     * Move the interval moving the center
     * @param newCenter the new center
     */
    void changeCenter(double newCenter);
    boolean contains(double value);
    double getClosestValueTo(double value);
    Interval getDeepCopy();

    /**
     * Convert an interval into a discrete step interval, and the step will be
     * the interval's half-length
     *
     * @return The interval in discrete step form
     */
    default DiscreteStepInterval convertToDiscreteStep(){
        return convertToDiscreteStep(this.length()/2);
    }


    /**
     * Convert an interval into a discrete step interval
     *
     * @param step the step
     * @return  The interval in discrete step form
     */
    default DiscreteStepInterval convertToDiscreteStep(double step){
        return new DiscreteStepInterval(this.getId(), this.getLowerBound(), this.getUpperBound(), step);
    }

    /**
     * Convert an interval into a discrete set interval
     *
     * @return  The interval in discrete set form
     */
    default DiscreteSetInterval convertToDiscreteSet(){
        return new DiscreteSetInterval(this.getId(), this.getLowerBound(), this.getUpperBound());
    }

    /**
     * Convert an interval into a continuous set interval
     *
     * @return  The interval in continuous set form
     */
    default ContinuousInterval convertToContinuous(){
        return new ContinuousInterval(this.getId(), this.getLowerBound(),this.getUpperBound());
    }

  }

