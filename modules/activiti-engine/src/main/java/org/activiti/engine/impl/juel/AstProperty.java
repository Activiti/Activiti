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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueReference;


public abstract class AstProperty extends AstNode {
	protected final AstNode prefix;
	protected final boolean lvalue;
	protected final boolean strict; // allow null as property value?
	
	public AstProperty(AstNode prefix, boolean lvalue, boolean strict) {
		this.prefix = prefix;
		this.lvalue = lvalue;
		this.strict = strict;
	}

	protected abstract Object getProperty(Bindings bindings, ELContext context) throws ELException;

	protected AstNode getPrefix() {
		return prefix;
	}

	public ValueReference getValueReference(Bindings bindings, ELContext context) {
		return new ValueReference(prefix.eval(bindings, context), getProperty(bindings, context));
	}
	
	@Override
	public Object eval(Bindings bindings, ELContext context) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			return null;
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			return null;
		}
		context.setPropertyResolved(false);
		Object result = context.getELResolver().getValue(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	public final boolean isLiteralText() {
		return false;
	}

	public final boolean isLeftValue() {
		return lvalue;
	}
	
	public boolean isMethodInvocation() {
		return false;
	}

	public Class<?> getType(Bindings bindings, ELContext context) {
		if (!lvalue) {
			return null;
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		Class<?> result = context.getELResolver().getType(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	public boolean isReadOnly(Bindings bindings, ELContext context) throws ELException {
		if (!lvalue) {
			return true;
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		boolean result = context.getELResolver().isReadOnly(context, base, property);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
		return result;
	}

	public void setValue(Bindings bindings, ELContext context, Object value) throws ELException {
		if (!lvalue) {
			throw new ELException(LocalMessages.get("error.value.set.rvalue", getStructuralId(bindings)));
		}
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", "null", base));
		}
		context.setPropertyResolved(false);
		context.getELResolver().setValue(context, base, property, value);
		if (!context.isPropertyResolved()) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.property.notfound", property, base));
		}
	}
	
	protected Method findMethod(String name, Class<?> clazz, Class<?> returnType, Class<?>[] paramTypes) {
		Method method = null;
		try {
			method = clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, clazz));
		}
		if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
			throw new MethodNotFoundException(LocalMessages.get("error.property.method.notfound", name, clazz));
		}
		return method;
	}
	
	public MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(property, String.class);
		Method method = findMethod(name, base.getClass(), returnType, paramTypes);
		return new MethodInfo(method.getName(), method.getReturnType(), paramTypes);
	}

	public Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues) {
		Object base = prefix.eval(bindings, context);
		if (base == null) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.base.null", prefix));
		}
		Object property = getProperty(bindings, context);
		if (property == null && strict) {
			throw new PropertyNotFoundException(LocalMessages.get("error.property.method.notfound", "null", base));
		}
		String name = bindings.convert(property, String.class);
		Method method = findMethod(name, base.getClass(), returnType, paramTypes);
		try {
			return method.invoke(base, paramValues);
		} catch (IllegalAccessException e) {
			throw new ELException(LocalMessages.get("error.property.method.access", name, base.getClass()));
		} catch (IllegalArgumentException e) {
			throw new ELException(LocalMessages.get("error.property.method.invocation", name, base.getClass()), e);
		} catch (InvocationTargetException e) {
			throw new ELException(LocalMessages.get("error.property.method.invocation", name, base.getClass()), e.getCause());
		}
	}

	public AstNode getChild(int i) {
		return i == 0 ? prefix : null;
	}
}
