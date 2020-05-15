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
package quasylab.sibilla.core.past.ds;

import java.util.Arrays;

/**
 * @author loreti
 *
 */
public class Tuple {

	private Object[] data;

	public Tuple(Object... data) {
		this.data = data;
	}

	public boolean isInstance(int i, Class<?> clazz) {
		return clazz.isInstance(data[i]);
	}

	public <T> T get(int i, Class<T> clazz) {
		return clazz.cast(data[i]);
	}

	public int size() {
		return data.length;
	}

	public Object get(int i) {
		return data[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) {
			return Arrays.deepEquals(data, ((Tuple) obj).data);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.deepToString(data);
	}
}
