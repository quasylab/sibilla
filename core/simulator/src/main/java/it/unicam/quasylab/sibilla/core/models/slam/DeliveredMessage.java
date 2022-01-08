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

package it.unicam.quasylab.sibilla.core.models.slam;

/**
 * Identifies a message that must be delivered at a given agent.
 */
public class DeliveredMessage {


    private final Agent target;
    private final MessageTag tag;
    private final SlamValue[] items;
    private final double deliveryTime;

    /**
     * Creates a new message that must be delivered at agent <code>target</code> at time <code>deliveryTime</code>,
     * having tag <code>tag</code> and items <code>items</code>.
     *
     * @param target message target.
     * @param tag message tag.
     * @param items items in the message.
     * @param deliveryTime time at which the message is delivered at the target.
     */
    public DeliveredMessage(Agent target, MessageTag tag, SlamValue[] items, double deliveryTime) {
        this.target = target;
        this.tag = tag;
        this.items = items;
        this.deliveryTime = deliveryTime;
    }

    /**
     * Returns message target.
     *
     * @return message target.
     */
    public Agent getTarget() {
        return target;
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
     * Returns message delivery time.
     *
     * @return message delivery time.
     */
    public double getDeliveryTime() {
        return deliveryTime;
    }

    /**
     * Returns the <code>idx</code>th item in the message.
     *
     * @param idx item index.
     * @return the <code>idx</code>th item in the message.
     */
    public SlamValue getItem(int idx) {
        return this.items[idx];
    }


}
