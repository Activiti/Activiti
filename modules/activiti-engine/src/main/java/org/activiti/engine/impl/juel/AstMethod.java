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
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueReference;


public class AstMethod extends AstNode {
	private final AstProperty property;
	private final AstParameters params;
	
	public AstMethod(AstProperty property, AstParameters params) {
		this.property = property;
		this.params = params;
	}

	public boolean isLiteralText() {
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

	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		return null;
	}

	public boolean isLeftValue() {
		return false;
	}
	
	public boolean isMethodInvocation() {
		return true;
	}
	
	public final ValueReference getValueReference(Bindings bindings, ELContext context) {
		return null;
	}

	@Override
	public void appendStructure(StringBuilder builder, Bindings bindings) {
		property.appendStructure(builder, bindings);
		params.appendStructure(builder, bindings);
	}

	@Override
	public Object eval(Bindings bindings, ELContext context) {
	  Class<?>[] paramTypes = null;
	  if (params.getCardinality() == 1) {
	    AstNode astNode = params.getChild(0);
      if (astNode instanceof AstString) {
        paramTypes = new Class<?>[params.getCardinality()];
        paramTypes[0] = String.class;
      } else if (astNode instanceof AstBoolean) {
        paramTypes = new Class<?>[params.getCardinality()];
        paramTypes[0] = Boolean.class;
      }
	  }
		return invoke(bindings, context, null, paramTypes, null);
	}

	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		Object base = property.getPrefix().eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", property.getPrefix()));
		}
		Object method = property.getProperty(bindings, context);
		if (method == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(method, String.class);
		paramValues = params.eval(bindings, context);

		context.setPropertyResolved(false);
		Object result = context.getELResolver().invoke(context, base, name, paramTypes, paramValues);
		if (!context.isPropertyResolved()) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, base.getClass()));
		}
//		if (returnType != null && !returnType.isInstance(result)) { // should we check returnType for method invocations?
//			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, base.getClass()));
//		}
		return result;
	}

	public int getCardinality() {
		return 2;
	}

	public Node getChild(int i) {
		return i == 0 ? property : i == 1 ? params : null;
	}

	@Override
	public String toString() {
		return "<method>";
	}
}
