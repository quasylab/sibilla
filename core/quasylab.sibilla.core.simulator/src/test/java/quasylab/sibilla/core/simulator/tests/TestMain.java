/*******************************************************************************
 * Copyright (c) 2015 QUANTICOL EU Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michele Loreti (University of Firenze) - initial API and implementation
 *******************************************************************************/
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
