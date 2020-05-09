/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package quasylab.sibilla.core.simulator.tests;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author loreti
 *
 */
public class TestMain {
	
	public static void main( String[] argv ) {
		
//		Integer i1 = 4;
//		Integer i2 = 5;
//		float x = 2.0F;
//		float y = 3.0F;
//		System.out.println("Equals: "+i1.getClass().equals(i2.getClass()));
//		System.out.println("==: "+(i1.getClass() == i2.getClass()));
//		System.out.println((0.1+0.2)-0.3);
//
//		HashMap<Integer,Integer> v1 = new HashMap<>();
//		v1.put(1, 1);
//		@SuppressWarnings("unchecked")
//		HashMap<Integer,Integer> v2 = (HashMap<Integer, Integer>) v1.clone();
//		v2.put(1, 37);
//		System.out.println(v1.get(1)+" - "+v2.get(1));

		int N = 1000;
		int[] test = new int[N];
		double size = 1000;
		long tot = 0;
		for( int i=0 ; i<size ; i++) {
			long start = System.currentTimeMillis();
			test = Arrays.copyOf(test, test.length);
			long end = System.currentTimeMillis();
			tot += (end-start);
		}
		System.out.println("Average with array: "+(tot/size));
		HashMap<Integer,Integer> mapState = new HashMap<>();
		for( int i=0 ; i<N; i++) {
			mapState.put(i, i);
		}
		tot = 0;
		for( int i=0 ; i<size ; i++) {
			long start = System.currentTimeMillis();
			mapState = (HashMap<Integer, Integer>) mapState.clone();
			long end = System.currentTimeMillis();
			tot += (end-start);
		}
		System.out.println("Average with map: "+(tot/size));
	}

}
