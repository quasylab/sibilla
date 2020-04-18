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
public class ActualTemplateField implements TemplateField {

	private Object o;

	public ActualTemplateField(Object o) {
		this.o = o;
	}

	@Override
	public boolean match(Object o) {
		if (this.o == null) {
			return o == null;
		}
		return this.o.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (o == null ? 0 : o.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ActualTemplateField) {
			Object other = ((ActualTemplateField) obj).o;
			return (o == null ? other == null : o.equals(other));
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
		return (o == null ? "null" : o.toString());
	}

	@Override
	public boolean implies(TemplateField f) {
		if (f instanceof ActualTemplateField) {
			return this.equals(f);
		}
		if (f instanceof FormalTemplateField) {
			return ((FormalTemplateField) f).clazz.isInstance(o);
		}
		return false;
	}

}
