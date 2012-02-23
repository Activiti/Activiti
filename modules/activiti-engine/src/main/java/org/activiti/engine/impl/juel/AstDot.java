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

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;


public class AstDot extends AstProperty {
	protected final String property;
	
	public AstDot(AstNode base, String property, boolean lvalue) {
		super(base, lvalue, true);
		this.property = property;
	}

	@Override
	protected String getProperty(Bindings bindings, ELContext context) throws ELException {
		return property;
	}

	@Override
	public String toString() {
		return ". " + property;
	}

	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		getChild(0).appendStructure(b, bindings);
		b.append(".");
		b.append(property);
	}

	public int getCardinality() {
		return 1;
	}
}
