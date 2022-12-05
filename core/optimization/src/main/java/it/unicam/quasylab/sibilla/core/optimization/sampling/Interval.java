package it.unicam.quasylab.sibilla.core.optimization.sampling;


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
    double length();
    void scale(double scaleFactor);
    double getRandomValue();
    void changeCenter(double newCenter);
    boolean contains(double value);
    double getIntervalValueClosestTo(double value);
    Interval getDeepCopy();

  }

