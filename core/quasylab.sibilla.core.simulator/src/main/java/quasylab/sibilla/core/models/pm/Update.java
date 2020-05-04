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
/**
 * 
 */
package quasylab.sibilla.core.models.pm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author loreti
 *
 */
public class Update implements Serializable {

	private static final long serialVersionUID = 5759358996259668600L;
	private Map<Integer, Integer> update;
	private String name;

	public Update(String name) {
		this.update = new HashMap<>();
		this.name = name;
	}

	public Set<Entry<Integer, Integer>> getUpdate() {
		return update.entrySet();
	}

	public synchronized void add(int idx, int c, int p) {
		if (c != p) {
			int drift = update.getOrDefault(idx, 0) + p - c;
			if (drift != 0) {
				update.put(idx, drift);
			} else {
				update.remove(idx);
			}
		}
	}

	public int get(int i) {
		return update.getOrDefault(i, 0);
	}

	public void consume(int idx, int c) {
		this.add(idx, c, 0);
	}

	public void produce(int idx, int p) {
		this.add(idx, 0, p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + ":" + update.toString();
	}

}
