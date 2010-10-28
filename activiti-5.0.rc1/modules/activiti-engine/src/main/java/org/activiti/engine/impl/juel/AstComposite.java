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

import java.util.List;

import org.activiti.engine.impl.javax.el.ELContext;


public class AstComposite extends AstRightValue {
	private final List<AstNode> nodes;

	public AstComposite(List<AstNode> nodes) {
		this.nodes = nodes;
	}

	@Override 
	public Object eval(Bindings bindings, ELContext context) {
		StringBuilder b = new StringBuilder(16);
		for (int i = 0; i < getCardinality(); i++) {
			b.append(bindings.convert(nodes.get(i).eval(bindings, context), String.class));
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return "composite";
	}	

	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		for (int i = 0; i < getCardinality(); i++) {
			nodes.get(i).appendStructure(b, bindings);
		}
	}

	public int getCardinality() {
		return nodes.size();
	}

	public AstNode getChild(int i) {
		return nodes.get(i);
	}
}
