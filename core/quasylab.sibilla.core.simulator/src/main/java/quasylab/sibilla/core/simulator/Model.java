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
/**
 * 
 */
package quasylab.sibilla.core.simulator;

import java.io.Serializable;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public interface Model<S> extends Serializable {

	public WeightedStructure<StepFunction<S>> getActivities( RandomGenerator r , S s );

//	public S getState( String label );
//	
//	public Set<String> getStateLabels(); 
//
//	public S copy( S state );
//
//	public Function<? super S,Double> getMeasure( String label );
//	
//	public Set<String> getMeasureLabels();
}
