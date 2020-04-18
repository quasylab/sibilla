/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package quasylab.sibilla.core.simulator.ds;

/**
 * @author loreti
 *
 */
public class FormalTemplateField implements TemplateField {

	protected Class<?> clazz;

	public FormalTemplateField(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean match(Object o) {
		return clazz.isInstance(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return clazz.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormalTemplateField) {
			return this.clazz == ((FormalTemplateField) obj).clazz;
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
		return "?{" + clazz.toString() + "}";
	}

	@Override
	public boolean implies(TemplateField f) {
		if (f instanceof FormalTemplateField) {
			return ((FormalTemplateField) f).clazz.isAssignableFrom(this.clazz);
		}
		return false;
	}

}
