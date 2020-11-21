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

package quasylab.sibilla.examples.agents;

import java.util.Arrays;

public class Obstacle {

	private final int xpos;
	private final int ypos;
	
	public Obstacle(int xpos, int ypos) {
		this.xpos = xpos;
		this.ypos = ypos;
	}
	
	public int getXPos() {
		return xpos;
	}
	
	public int getYPos() {
		return ypos;
	}
	
	public boolean isPlacedAt (int xpos, int ypos) {
		return (this.xpos == xpos)&&(this.ypos==ypos);
	}
}
