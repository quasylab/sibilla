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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;


/**
 * @author loreti
 *
 */
public class DescriptiveStatisticSampling<S extends State> extends StatisticSampling<S> {

	private final DescriptiveStatistics[] data;

	public static <S extends State> StatisticSampling<S> measure(String name, int samplings, double deadline, MeasureFunction<S> m) {
		return new DescriptiveStatisticSampling<>(samplings, deadline / samplings,
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



	public DescriptiveStatisticSampling(int samples, double dt, Measure<? super S> measure) {
		super(measure, dt);
		this.data = new DescriptiveStatistics[samples];
		init();
	}

	@Override
	protected void init() {
		for (int i = 0; i < data.length; i++) {
			data[i] = new DescriptiveStatistics();
		}
	}




	@Override
	public synchronized void printTimeSeries(Function<String, String> nameFunction, char separator, double significance) throws FileNotFoundException {

		String fileName = nameFunction.apply(this.getName());
		PrintStream out = new PrintStream(fileName);
		double time = 0.0;
		for (int i = 0; i < this.data.length; i++) {
			out.printf("%f",time);//Print time
			out.printf("%c%f",separator, this.data[i].getMin());//Print min value
			out.printf("%c%f",separator, this.data[i].getPercentile(25));//Print first quartile
			out.printf("%c%f",separator, this.data[i].getMean());//Print mean
			out.printf("%c%f",separator, getMedian(i));//Print median
			out.printf("%c%f",separator, this.data[i].getPercentile(25));//Print third quartile
			out.printf("%c%f\n",separator, this.data[i].getMax());//Print first quartile
			time += dt;
		}
		out.close();
	}

	private double getMedian(int i) {
		long n = this.data[i].getN();
		if (n == 0) {
			return Double.NaN;
		}
		int idx = (int) n/2;
		if (n%2==0) {
			double v1 = this.data[i].getElement(idx);
			double v2 = this.data[i].getElement(idx-1);
			return (v1+v2)/2;
		} else {
			return this.data[i].getElement(idx+1);
		}
	}



	@Override
	public synchronized int getSize() {
		return data.length;
	}

	@Override
	protected synchronized void recordValues(double[] values) {
		if (values.length != data.length) {
			throw new IllegalArgumentException();//TODO: Add Message!
		}
		for(int i=0; i<values.length; i++) {
			data[i].addValue(values[i]);
		}
	}

	@Override
	protected synchronized double[] getDataRow(int i) {
		return new double[] {getTimeOfIndex(i),
				data[i].getMin(),
				data[i].getPercentile(25),
				data[i].getMean(),
				data[i].getPercentile(50),
				data[i].getPercentile(75),
				data[i].getMax()
		};
	}

}