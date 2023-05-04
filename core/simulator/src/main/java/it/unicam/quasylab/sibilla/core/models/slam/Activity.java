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

import it.unicam.quasylab.sibilla.core.models.slam.agents.Agent;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents an activity that is scheduled at a given time.
 */
public abstract class Activity implements Comparable<Activity> {

    private final double scheduledTime;

    /**
     * Creates an activity scheduled at the given time.
     *
     * @param scheduledTime time when the created activity is scheduled.
     */
    public Activity(double scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    @Override
    public int compareTo(Activity o) {
        return 0;
    }

    /**
     * Returns the time unit when the activity is scheduled.
     *
     * @return the time unit when the activity is scheduled.
     */
    public double getScheduledTime() {
        return scheduledTime;
    }

    public abstract void execute(RandomGenerator rg, SlamState state);

    public static class AgentStepActivity extends Activity {

        private final Agent scheduledAgent;

        public AgentStepActivity(Agent scheduledAgent) {
            super(scheduledAgent.timeOfNextStep());
            this.scheduledAgent = scheduledAgent;
        }

        public Agent getScheduledAgent() {
            return this.scheduledAgent;
        }

        @Override
        public void execute(RandomGenerator rg, SlamState state) {
            state.executeAgentStep(rg, scheduledAgent);
        }
    }

    public static class MessageDeliveryActivity extends Activity {

        private final DeliveredMessage message;

        public MessageDeliveryActivity(DeliveredMessage message) {
            super(message.getDeliveryTime());
            this.message = message;
        }

        public DeliveredMessage getMessage() {
            return this.message;
        }

        @Override
        public void execute(RandomGenerator rg, SlamState state) {
            state.deliverMessage(rg, message);
        }
    }

}
