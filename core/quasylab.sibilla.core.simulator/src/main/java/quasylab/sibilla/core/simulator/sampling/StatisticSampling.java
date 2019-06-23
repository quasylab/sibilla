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
package quasylab.sibilla.core.simulator.sampling;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.function.Function;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;


/**
 * @author loreti
 *
 */
public class StatisticSampling<S> implements SamplingFunction<S> {

	private SummaryStatistics[][] data;
	private Measure<S> measure;
	private double last_measure[];
	private double dt;
	private double next_time[];
	private int current_index[];
	private double new_measure[];
	private int task_slots;

	public StatisticSampling(int samples, double dt, Measure<S> measure) { //TODO: won't work in multi-task simulation!!
		this(samples, dt, measure, 1);
	}

	public StatisticSampling(int samples, double dt, Measure<S> measure, int task_slots) {
		this.data = new SummaryStatistics[task_slots][samples];
		this.measure = measure;
		this.dt = dt;
		this.task_slots = task_slots;
		init();
	}

	private void init() {
		for (int i = 0; i < task_slots; i++){
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = new SummaryStatistics();
			}
		}
		new_measure = new double[task_slots];
		last_measure = new double[task_slots];

	}

	@Override
	public void sample(double time, S context, int slot) {
		this.new_measure[slot] = measure.measure(context);
		if ((time >= this.next_time[slot]) && (this.current_index[slot] < this.data[0].length)) { // TODO: improve this condition
			recordMeasure(time, slot);
		} else {
			this.last_measure[slot] = this.new_measure[slot];
		}
	}

	private void recordMeasure(double time, int slot) {
		while ((this.next_time[slot]<time)&&(this.current_index[slot]<this.data[0].length)) { // TODO: improve this condition
			this.recordSample(slot);
		} 
		this.last_measure = this.new_measure;		
		if (this.next_time[slot] == time) {
			this.recordSample(slot);
		}
	}
	
	private void recordSample(int slot) {
		this.data[slot][this.current_index[slot]].addValue(this.last_measure[slot]);
		this.current_index[slot]++;
		this.next_time[slot] += this.dt;
	}
	

	@Override
	public void end(double time) {
		for(int i = 0; i < task_slots; i++){
			while (this.current_index[i] < this.data[0].length){ // TODO: improve this condition
				this.data[i][this.current_index[i]].addValue(this.last_measure[i]);
				this.current_index[i]++;
				this.next_time[i] += this.dt;
			}
		}
	}

	@Override
	public void start() {
		this.current_index = new int[task_slots];
		this.next_time = new double[task_slots];
	}

	public void printTimeSeries(PrintStream out) {
		double time;
		for (int i = 0; i < task_slots; i++){
			time = 0.0;
			out.println("----------------Results of TASK "+ i + "----------------");
			for (int j = 0; j < this.data[0].length; j++) { // TODO: improve this condition
				out.println(time + "\t" + this.data[i][j].getMean() + "\t" + this.data[i][j].getStandardDeviation());
				time += dt;
			}
		}
	}

	public void printTimeSeries(PrintStream out, char separator) {
		double time;
		for (int i = 0; i < task_slots; i++){ // TODO: improve this condition
			time = 0.0;
			out.println("----------------Results of TASK "+ i + "----------------");
			for (int j = 0; j < this.data[0].length; j++) {
				out.println(""+time + separator 
						+ this.data[i][j].getMean() 
						+ separator + this.data[i][j].getStandardDeviation());
				time += dt;
			}
		}
	}
	
	
	public void printName(PrintStream out){
		out.print(this.measure.getName());
	}
	
	public void printlnName(PrintStream out){
		out.println(this.measure.getName());
	}

	@Override
	public LinkedList<SimulationTimeSeries> getSimulationTimeSeries( int replications, int slot ) {
		SimulationTimeSeries stt = new SimulationTimeSeries(measure.getName(), dt, replications, data[slot]);
		LinkedList<SimulationTimeSeries> toReturn = new LinkedList<>();
		toReturn.add(stt);
		return toReturn;
	}

	public int getSize() {
		return data.length;
	}
	
	public static <S> StatisticSampling<S> measure( String name, int samplings, double deadline, Function<S,Double> m, int slots) {
		return new StatisticSampling<S>(samplings, deadline/samplings, 
				new Measure<S>() {

			@Override
			public double measure(S t) {
				// TODO Auto-generated method stub
				return m.apply( t );
			}

			@Override
			public String getName() {
				return name;
			}

		}, slots);
		
	}

	public static <S> StatisticSampling<S> measure( String name, int samplings, double deadline, Function<S,Double> m) {  //TODO: wont work in multi-task simulation
		return new StatisticSampling<S>(samplings, deadline/samplings, 
				new Measure<S>() {

			@Override
			public double measure(S t) {
				// TODO Auto-generated method stub
				return m.apply( t );
			}

			@Override
			public String getName() {
				return name;
			}

		});
		
	}

}
