/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

/**
 * @author loreti
 *
 */
public class Trajectory<S> implements Serializable {
	
	private static final long serialVersionUID = -9039722623650234376L;
	private List<Sample<S>> data;
	private double start = Double.NaN;
	private double end = Double.NaN;
	private boolean succesfull; 
	private long generationTime = -1;

	public Trajectory() {
		this.data = new LinkedList<Sample<S>>();
	}
	
	public void add( double time , S value ) {
		if (!Double.isFinite(start)) {
			this.start = time;
		}
		if (!Double.isFinite( this.end )&&(this.end>=time)) {
			throw new IllegalArgumentException();//TODO: Add message!
		}
		this.data.add( new Sample<S>(time,value));
	}
	
	public void sample(SamplingFunction<S> f) {
		if (!Double.isFinite(start)) {
			throw new IllegalArgumentException();
		}
		f.start();
		this.data.stream().forEach(s -> f.sample(s.getTime(), s.getValue()));
		f.end(end);
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}
	
	public int size() {
		return data.size();
	}

	/**
	 * @return the succesfull
	 */
	public boolean isSuccesfull() {
		return succesfull;
	}

	/**
	 * @param succesfull the succesfull to set
	 */
	public void setSuccesfull(boolean succesfull) {
		this.succesfull = succesfull;
	}

	/**
	 * @return the generationTime
	 */
	public long getGenerationTime() {
		return generationTime;
	}

	/**
	 * @param generationTime the generationTime to set
	 */
	public void setGenerationTime(long generationTime) {
		this.generationTime = generationTime;
	}
	

}
