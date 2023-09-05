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

package it.unicam.quasylab.sibilla.core.models.slam.agents;


import it.unicam.quasylab.sibilla.core.models.slam.DeliveredMessage;
import it.unicam.quasylab.sibilla.core.models.slam.MessageTag;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

/**
 * The class <code>AgentMessage</code> represents a message exchanged among agents. Each message contains a tag, that
 * is a string, and a sequence of doubles. Each tag is also identified by an integer.
 */
public class AgentMessage {

    private final MessageTag tag;
    private final SibillaValue[] items;
    private final Predicate<SlamAgent> target;

    /**
     * Creates a new message with the given tag and the given items.
     *
     * @param tag message tag.
     * @param target a predicate used to check if an agent can receive or not the message.
     * @param items message items.
     */
    public AgentMessage(MessageTag tag, SibillaValue[] items, Predicate<SlamAgent> target) {
        this.tag = tag;
        this.items = items;
        this.target = target;
    }

    /**
     * Returns message tag.
     *
     * @return message tag.
     */
    public MessageTag getTag() {
        return tag;
    }

    /**
     * Returns the number of items in the message.
     *
     * @return the number of items in the message.
     */
    public int getNumberOfItems() {
        return items.length;
    }

    /**
     * Returns the ith item in the message. An {@link IndexOutOfBoundsException} is thrown if <code>i</code>
     * is not a valid index.
     *
     * @param i item index.
     * @return the ith item in the message.
     */
    public SibillaValue getItem(int i) {
        return items[i];
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentMessage that = (AgentMessage) o;
        return getTag().equals(that.getTag()) && Arrays.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getTag());
        result = 31 * result + Arrays.hashCode(items);
        return result;
    }

    @Override
    public String toString() {
        return tag+Arrays.toString(items);
    }


    /**
     * Returns the predicate identifying the target of the message.
     *
     * @return the predicate identifying the target of the message.
     */
    public Predicate<SlamAgent> getTarget() {
        return target;
    }

    public SibillaValue[] getContent() {
        return this.items;
    }
}
