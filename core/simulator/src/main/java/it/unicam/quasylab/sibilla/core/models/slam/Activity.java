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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents an activity that is scheduled at a given time.
 */
public abstract class Activity implements Comparable<Activity> {

    /**
     * Order of the activity.
     */
    private final int activityCounter;


    private final double scheduledTime;

    /**
     * Creates an activity scheduled at the given time.
     *
     * @param scheduledTime time when the created activity is scheduled.
     */
    private Activity(int activityCounter, double scheduledTime) {
        this.activityCounter = activityCounter;
        this.scheduledTime = scheduledTime;
    }

    @Override
    public int compareTo(Activity o) {
        return this.activityCounter -o.activityCounter;
    }

    /**
     * Returns the time unit when the activity is scheduled.
     *
     * @return the time unit when the activity is scheduled.
     */
    public double getScheduledTime() {
        return scheduledTime;
    }

    public abstract SlamState execute(RandomGenerator rg, SlamState state);

    public static class AgentStepActivity extends Activity {

        private final int scheduledAgentId;

        private AgentStepActivity(int activityCounter, double time, int scheduledAgentId) {
            super(activityCounter, time);
            this.scheduledAgentId = scheduledAgentId;
        }


        public int getScheduledAgentId() {
            return this.scheduledAgentId;
        }

        @Override
        public SlamState execute(RandomGenerator rg, SlamState state) {
            return state.executeAgentStep(rg, getScheduledAgentId());
        }
    }

    public static class MessageDeliveryActivity extends Activity {

        private final DeliveredMessage message;


        private  MessageDeliveryActivity(int activityCounter, double time, DeliveredMessage message) {
            super(activityCounter, time);
            this.message = message;
        }

        public DeliveredMessage getMessage() {
            return this.message;
        }

        @Override
        public SlamState execute(RandomGenerator rg, SlamState state) {
            return state.deliverMessage(rg, message);
        }
    }

    public static class ActivityFactory {

        private int activityCounter = 0;

        public Activity.AgentStepActivity agentStepActivity(double time, int scheduledAgentId) {
            return new AgentStepActivity(activityCounter++, time, scheduledAgentId);
        }

        public Activity.MessageDeliveryActivity messageDeliveryActivity(double time, DeliveredMessage deliveredMessage) {
            return new MessageDeliveryActivity(activityCounter++, time, deliveredMessage);
        }

    }

}
