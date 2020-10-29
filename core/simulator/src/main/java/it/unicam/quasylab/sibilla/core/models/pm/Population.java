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

package it.unicam.quasylab.sibilla.core.models.pm;

import java.io.Serializable;

public class Population implements Serializable {

    private static final long serialVersionUID = 5501961970972786801L;

    private int index;

    private int size;

    /**
     * @param index
     * @param size
     */
    public Population(int index, int size) {
        super();
        this.index = index;
        this.size = size;
    }

    public Population(int s) {
        this(s,1);
    }

    public int getIndex() {
        return index;
    }

    public int getSize() {
        return size;
    }

}