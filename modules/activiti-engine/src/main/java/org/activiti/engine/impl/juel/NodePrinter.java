/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.juel;

import java.io.PrintWriter;
import java.util.Stack;

/**
 * Node pretty printer for debugging purposes.
 *
 * @author Christoph Beck
 */
public class NodePrinter {
	private static boolean isLastSibling(Node node, Node parent) {
		if (parent != null) {
			return node == parent.getChild(parent.getCardinality() - 1);
		}
		return true;
	}
	
	private static void dump(PrintWriter writer, Node node, Stack<Node> predecessors) {
		if (!predecessors.isEmpty()) {
			Node parent = null;
			for (Node predecessor: predecessors) {
				if (isLastSibling(predecessor, parent)) {
					writer.print("   ");
				} else {
					writer.print("|  ");
				}
				parent = predecessor;
			}
			writer.println("|");
		}
		Node parent = null;
		for (Node predecessor: predecessors) {
			if (isLastSibling(predecessor, parent)) {
				writer.print("   ");
			} else {
				writer.print("|  ");
			}
			parent = predecessor;
		}
		writer.print("+- ");
		writer.println(node.toString());

		predecessors.push(node);
		for (int i = 0; i < node.getCardinality(); i++) {
			dump(writer, node.getChild(i), predecessors);
		}
		predecessors.pop();
	}

	public static void dump(PrintWriter writer, Node node) {
		dump(writer, node, new Stack<Node>());
	}
}
