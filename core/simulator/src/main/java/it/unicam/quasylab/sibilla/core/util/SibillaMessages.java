/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.util;

/**
 * Utility class used to build error and info messages.
 */
public class SibillaMessages {

    public static final String ILLEGAL_TIME_IN_TIMESTEP = "A time step must be a value greater than 0 (%g is used).";
    public static final String A_POSITIVE_VALUE_IS_EXPECTED = "A value greater than 0 is expected (%g is used).";;
    private static final String ILLEGAL_NUMBER_OF_PARAMETERS = "Illegal number of parameters: expected %d are %d!";


    /**
     * A time step must be a value greater than 0.
     *
     * @param time used time.
     * @return error message.
     */
    public static String createdTimeStepWithNonPositiveTime(double time) {
        return String.format(ILLEGAL_TIME_IN_TIMESTEP,time);
    }

    /**
     * A value greater than 0 is expexted.
     *
     * @param value used value.
     * @return error message.
     */
    public static String aPositiveValueIsExpected(double value) {
        return String.format(A_POSITIVE_VALUE_IS_EXPECTED,value);
    }

    public static String wrongNumberOfParameters(int expected, int actual) {
        return String.format(ILLEGAL_NUMBER_OF_PARAMETERS,expected,actual);
    }
}
