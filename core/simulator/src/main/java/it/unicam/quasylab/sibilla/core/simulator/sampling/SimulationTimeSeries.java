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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.io.*;

/**
 * @author loreti
 *
 */
public class SimulationTimeSeries {

	private final StatisticalSummary[] data;
	private final double dt;
	private final String name;
	private final int replications;
	private boolean summary = false;


	public SimulationTimeSeries(boolean summary, String name , double dt , int replications , StatisticalSummary[] data ) {
		this.name = name;
		this.dt = dt;
		this.data = data;
		this.replications = replications;
		this.summary = summary;
	}
	
	public String getName() {
		return name;
	}
	
	public double getMean( int i ) {
		return data[i].getMean();
	}
	
	public double getStandardDeviation( int i ) {
		return data[i].getStandardDeviation();
	}
	
	public double getTime( int i ) {
		return i*dt;
	}	
	
	public double getMean( double t ) {
		int i = (int) (t/dt);
		if (i>=data.length) {
			i = data.length-1;
		}
		return getMean( i );
	}

	public double getMax(int i) {
		return data[i].getMax();
	}

	public double getMin(int i) {
		return data[i].getMin();
	}

	public double getMedian(int i) {
		if (data[i] instanceof DescriptiveStatistics) {
			return ((DescriptiveStatistics) data[i]).getPercentile(50);
		} else {
			return Double.NaN;
		}
	}

	public double getQ1(int i) {
		if (data[i] instanceof DescriptiveStatistics) {
			return ((DescriptiveStatistics) data[i]).getPercentile(25);
		} else {
			return Double.NaN;
		}
	}

	public double getQ3(int i) {
		if (data[i] instanceof DescriptiveStatistics) {
			return ((DescriptiveStatistics) data[i]).getPercentile(75);
		} else {
			return Double.NaN;
		}
	}


	public StatisticalSummary[] getData() {
		return data;
	}
	
	public void printTimeSeries( PrintStream out ) {
		out.println(name);
		for( int i=0 ; i<data.length ; i++ ) {
			out.println(getTime(i)+"\t"+getMean(i));
		}
	}
	
	public void saveTo( String path ) throws FileNotFoundException {
		File output = new File( path+File.separator+"_"+name+".dat");
		PrintStream ps = new PrintStream(output);
		printTimeSeries(ps);
		ps.close();
	}
	
	public int getSize() {
		return data.length;
	}
	
	public double getConfidenceInterval( int i ) {
		if (replications<=0) {
			return 0.0;
		} else {
			return this.getStandardDeviation(i)/Math.sqrt( replications );		
		}
	}

	public void writeToCSV(String outputFolder) throws FileNotFoundException {
		writeToCSV(outputFolder,"","");
	}

	public void writeToCSV(String outputFolder, String prefix) throws FileNotFoundException {
		writeToCSV(outputFolder,prefix,"");
	}

	public void writeToCSV(String outputFolder, String prefix, String postfix) throws FileNotFoundException {
		writeToCSV(new File(outputFolder),prefix,postfix);
	}

	public void writeToCSV(File outputFolder, String prefix, String postfix) throws FileNotFoundException {
		File output = new File(outputFolder,prefix+name+postfix+".csv");
		PrintWriter writer = new PrintWriter(output);
		writeToCSV(writer);
		writer.close();
	}

	public void writeToCSV( StringWriter writer ) {
		for( int i=0 ; i<data.length ; i++ ) {
			writer.write(getCSVRow(i, ',')+"\n");
			writer.flush();
		}
	}

	public void writeToCSV( PrintWriter writer ) {
		for( int i=0 ; i<data.length ; i++ ) {
			writer.println(getCSVRow(i,','));
			writer.flush();
		}
	}

	public String getCSVRow(int i, char separator) {
		if (this.summary) {
			return String.format("%f%c%f%c%f%c%f",getTime(i),separator,getMean(i),separator,getStandardDeviation(i),separator,getConfidenceInterval(i));
		} else {
			return String.format("%f%c%f%c%f%c%f%c%f%c%f%c%f",
					getTime(i), separator,
					getMin(i), separator,
					getQ1(i), separator,
					getMean(i), separator,
					getMedian(i), separator,
					getQ3(i), separator,
					getMax(i));
		}
	}
}
