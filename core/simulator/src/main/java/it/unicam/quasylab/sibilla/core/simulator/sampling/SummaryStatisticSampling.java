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

package it.unicam.quasylab.sibilla.core.simulator.sampling;

import it.unicam.quasylab.sibilla.core.models.MeasureFunction;
import it.unicam.quasylab.sibilla.core.models.State;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.function.Function;


/**
 * @author loreti
 *
 */
public class SummaryStatisticSampling<S extends State> extends StatisticSampling<S> {

	private final SummaryStatistics[] data;

	public static <S extends State> StatisticSampling<S> measure(String name, int samplings, double deadline, MeasureFunction<S> m) {
		return new SummaryStatisticSampling<>(samplings, deadline / samplings,
				new Measure<>() {

					@Override
					public double measure(S t) {
						return m.apply(t);
					}

					@Override
					public String getName() {
						return name;
					}

				});

	}



	public SummaryStatisticSampling(int samples, double dt, Measure<S> measure) {
		super(measure, dt);
		this.data = new SummaryStatistics[samples];
		init();
	}

	@Override
	protected void init() {
		for (int i = 0; i < data.length; i++) {
			data[i] = new SummaryStatistics();
		}
	}

	@Override
	protected synchronized void recordSample(int i, double v) {
		this.data[i].addValue(v);
	}


	@Override
	public void printTimeSeries(Function<String, String> nameFunction, char separator, double significance) throws FileNotFoundException {

		String fileName = nameFunction.apply(this.getName());
		PrintStream out = new PrintStream(fileName);
		double time = 0.0;
		for (int i = 0; i < this.data.length; i++) {
			double ci = getConfidenceInterval(i,significance);
			out.println(""+time + separator 
					+ this.data[i].getMean() 
					+ separator + ci);
			time += dt;
		}
		out.close();
	}
	
	
	private double getConfidenceInterval(int i, double significance) {
		TDistribution tDist = new TDistribution(this.data[i].getN());
		double a = tDist.inverseCumulativeProbability(1.0 -significance/2);
		return a*this.data[i].getStandardDeviation() / Math.sqrt(this.data[i].getN());
	}

	@Override
	public int getSize() {
		return data.length;
	}

	@Override
	protected double[] getDataRow(int i) {
		return new double[] { getTimeOfIndex(i), data[i].getMean(), data[i].getStandardDeviation(), getConfidenceInterval(i, 0.05)};
	}

}