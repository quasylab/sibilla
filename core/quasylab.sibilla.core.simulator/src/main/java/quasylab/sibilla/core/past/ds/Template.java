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
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package quasylab.sibilla.core.past.ds;

import java.util.Arrays;

/**
 * @author loreti
 *
 */
public class Template {

	private TemplateField[] fields;

	public Template(TemplateField... fields) {
		this.fields = fields;
	}

	public int size() {
		return fields.length;
	}

	public boolean match(Tuple t) {
		if (size() != t.size()) {
			return false;
		}
		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].match(t.get(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean match(int i, Object o) {
		return fields[i].match(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(fields);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Template) {
			return Arrays.deepEquals(fields, ((Template) obj).fields);
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
		return Arrays.deepToString(fields);
	}

	public TemplateField get(int i) {
		return fields[i];
	}

	public boolean implies(Template t) {
		if (size() != t.size()) {
			return false;
		}

		return true;
	}
}
