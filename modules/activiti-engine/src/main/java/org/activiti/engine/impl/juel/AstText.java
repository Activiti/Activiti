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
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.ValueReference;


public final class AstText extends AstNode {
	private final String value;

	public AstText(String value) {
		this.value = value;
	}

	public boolean isLiteralText() {
		return true;
	}

	public boolean isLeftValue() {
		return false;
	}
	
	public boolean isMethodInvocation() {
		return false;
	}

	public Class<?> getType(Bindings bindings, ELContext context) {
		return null;
	}

	public boolean isReadOnly(Bindings bindings, ELContext context) {
		return true;
	}

	public void setValue(Bindings bindings, ELContext context, Object value) {
		throw new ELException(LocalMessages.get("error.value.set.rvalue", getStructuralId(bindings)));
	}

	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		return null;
	}
	
	@Override 
	public Object eval(Bindings bindings, ELContext context) {
		return value;
	}

	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return null;
	}

	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		return returnType == null ? value : bindings.convert(value, returnType);
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}	

	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		int end = value.length() - 1;
		for (int i = 0; i < end; i++) {
			char c = value.charAt(i);
			if ((c == '#' || c == '$') && value.charAt(i + 1) == '{') {
				b.append('\\');
			}
			b.append(c);
		}
		if (end >= 0) {
			b.append(value.charAt(end));
		}
	}

	public int getCardinality() {
		return 0;
	}

	public AstNode getChild(int i) {
		return null;
	}
}
