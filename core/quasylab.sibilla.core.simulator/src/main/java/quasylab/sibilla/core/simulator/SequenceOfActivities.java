/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.simulator;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author loreti
 *
 */
public class SequenceOfActivities implements Activity {

	@Override
	public String toString() {
		if (activities.length > 0) {
			return activities[0].toString();
		} else {
			return "...";
		}
	}

	private Activity[] activities;

	public SequenceOfActivities(Activity... activities) {
		this.activities = activities;
	}

	@Override
	public boolean execute(RandomGenerator r, double now,double dt) {
		boolean result = true;
		for (Activity activity : activities) {
			result = activity.execute(r,now,dt);
			if (!result) {
				return result;
			}
		}
		return result;
	}

	@Override
	public String getName() {
		String toReturn = "";
		for( int i=0 ; i<activities.length ; i++ ) {
			toReturn += activities[i].getName();
		}
		return toReturn;
	}

}
