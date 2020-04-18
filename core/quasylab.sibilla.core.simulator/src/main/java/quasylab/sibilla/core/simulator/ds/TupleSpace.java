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

import quasylab.sibilla.core.simulator.util.ComposedWeightedStructure;
import quasylab.sibilla.core.simulator.util.WeightedStructure;
import quasylab.sibilla.core.simulator.util.Weighter;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author loreti
 *
 */
public class TupleSpace {

	private Node root;
	private Weighter<Tuple> weighter;

	public TupleSpace() {
		this.root = new Node();
	}

	public class Node {

		int occurrences;

		Tuple t;

		HashMap<Object, Node> nodes;

		public Node() {
			this.occurrences = 0;
			this.t = new Tuple();
			this.nodes = new HashMap<Object, Node>();
		}

		public Node get(Object v) {
			Node n = nodes.get(v);
			if (n == null) {
				n = new Node();
				nodes.put(v, n);
			}
			return n;
		}

		public LinkedList<Node> get(TemplateField f) {
			LinkedList<Node> toReturn = new LinkedList<Node>();
			for (Object o : nodes.keySet()) {
				if (f.match(o)) {
					toReturn.add(nodes.get(o));
				}
			}
			return toReturn;
		}

	}

	public boolean put(Tuple t) {
		Node node = getNode(t);
		if (node.t == null) {
			node.t = t;
		}
		node.occurrences++;
		return true;
	}

	public WeightedStructure<GetActivity> get(Template t) {
		LinkedList<Node> lst = collect(t);
		ComposedWeightedStructure<GetActivity> ws = new ComposedWeightedStructure<GetActivity>();
		for (Node node : lst) {
			if (node.occurrences > 0) {
				ws.add(weight(node), new GetActivity(node));
			}
		}
		return ws;
	}

	public WeightedStructure<Tuple> query(Template t) {
		LinkedList<Node> lst = collect(t);
		ComposedWeightedStructure<Tuple> ws = new ComposedWeightedStructure<Tuple>();
		for (Node node : lst) {
			if (node.occurrences > 0) {
				ws.add(weight(node), node.t);
			}
		}
		return ws;
	}

	private Node getNode(Tuple t) {
		Node toReturn = root;
		for (int i = 0; i < t.size(); i++) {
			toReturn = toReturn.get(t.get(i));
		}
		return toReturn;
	}

	private LinkedList<Node> collect(Template t) {
		LinkedList<Node> pending = new LinkedList<Node>();
		pending.add(root);
		for (int i = 0; i < t.size(); i++) {
			LinkedList<Node> nextPending = new LinkedList<Node>();
			for (Node node : pending) {
				nextPending.addAll(node.get(t.get(i)));
			}
			pending = nextPending;
		}
		return pending;
	}

	public int copiesOf(Tuple t) {
		return getNode(t).occurrences;
	}

	public double weightOf(Tuple t) {
		return weight(getNode(t));
	}

	public double weightOf(Template t) {
		LinkedList<Node> lst = collect(t);
		double d = 0.0;
		for (Node node : lst) {
			if (node.occurrences > 0) {
				d += weight(node);
			}
		}
		return d;
	}

	private double weight(Node node) {
		if (weighter == null) {
			return node.occurrences;
		} else {
			return weighter.weight(node.t, node.occurrences);
		}
	}

	public int copiesOf(Template t) {
		LinkedList<Node> lst = collect(t);
		int count = 0;
		for (Node node : lst) {
			count += node.occurrences;
		}
		return count;
	}

}
