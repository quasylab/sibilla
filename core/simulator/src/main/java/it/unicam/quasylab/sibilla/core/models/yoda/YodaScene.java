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

package it.unicam.quasylab.sibilla.core.models.yoda;


import java.io.Serializable;

public interface YodaScene extends Serializable {


    int getWidthInt();

    double getWidthReal();

    int getHeightInt();

    double getHeightReal();


    /**
     * This method add any kind object we want in any coordinates (double)
     *
     * @param posx the position x in the coordinates
     * @param posy the position y in the coordinates
     */
    void addObject(double posx, double posy);

    /**
     * This method add any kind object we want in any coordinates (int)
     *
     * @param posx the position x in the coordinates
     * @param posy the position y in the coordinates
     */
    void addObject(int posx, int posy);

    boolean thereIsSomething(YodaValue ... values);

    boolean thereIsSomething(int posx, int posy);

    boolean thereIsSomething(double posx, double posy);

    //YodaScene generate(RandomGenerator rg, int width, int height, int numberOfObstacles);

}
