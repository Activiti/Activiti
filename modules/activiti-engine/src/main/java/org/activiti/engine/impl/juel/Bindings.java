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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.ValueExpression;


/**
 * Bindings, usually created by a {@link org.activiti.engine.impl.juel.Tree}.
 *
 * @author Christoph Beck
 */
public class Bindings implements TypeConverter {
	private static final long serialVersionUID = 1L;

	private static final Method[] NO_FUNCTIONS = new Method[0];
	private static final ValueExpression[] NO_VARIABLES = new ValueExpression[0];

	/**
	 * Wrap a {@link Method} for serialization.
	 */
	private static class MethodWrapper implements Serializable {
		private static final long serialVersionUID = 1L;

		private transient Method method;
		private MethodWrapper(Method method) {
			this.method = method;
		}
		private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
			out.defaultWriteObject();
			out.writeObject(method.getDeclaringClass());
			out.writeObject(method.getName());
			out.writeObject(method.getParameterTypes());
		}
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			Class<?> type = (Class<?>)in.readObject();
			String name = (String)in.readObject();
			Class<?>[] args = (Class<?>[])in.readObject();
			try {
				method = type.getDeclaredMethod(name, args);
			} catch (NoSuchMethodException e) {
				throw new IOException(e.getMessage());
			}
		}	
	}

	private transient Method[] functions;
	private final ValueExpression[] variables;
	private final TypeConverter converter;

	/**
	 * Constructor.
	 */
	public Bindings(Method[] functions, ValueExpression[] variables) {
		this(functions, variables, TypeConverter.DEFAULT);
	}

	/**
	 * Constructor.
	 */
	public Bindings(Method[] functions, ValueExpression[] variables, TypeConverter converter) {
		super();

		this.functions = functions == null || functions.length == 0 ? NO_FUNCTIONS : functions;
		this.variables = variables == null || variables.length == 0 ? NO_VARIABLES : variables;
		this.converter = converter == null ? TypeConverter.DEFAULT : converter;
	}
	
	/**
	 * Get function by index.
	 * @param index function index
	 * @return method
	 */
	public Method getFunction(int index) {
		return functions[index];
	}
	
	/**
	 * Test if given index is bound to a function.
	 * This method performs an index check.
	 * @param index identifier index
	 * @return <code>true</code> if the given index is bound to a function
	 */
	public boolean isFunctionBound(int index) {
		return index >= 0 && index < functions.length;
	}
	
	/**
	 * Get variable by index.
	 * @param index identifier index
	 * @return value expression
	 */
	public ValueExpression getVariable(int index) {
		return variables[index];
	}

	/**
	 * Test if given index is bound to a variable.
	 * This method performs an index check.
	 * @param index identifier index
	 * @return <code>true</code> if the given index is bound to a variable
	 */
	public boolean isVariableBound(int index) {
		return index >= 0 && index < variables.length && variables[index] != null;
	}
	
	/**
	 * Apply type conversion.
	 * @param value value to convert
	 * @param type target type
	 * @return converted value
	 * @throws ELException
	 */
	public <T> T convert(Object value, Class<T> type) {
		return converter.convert(value, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bindings) {
			Bindings other = (Bindings)obj;
			return Arrays.equals(functions, other.functions)
				&& Arrays.equals(variables, other.variables)
				&& converter.equals(other.converter);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(functions) ^ Arrays.hashCode(variables) ^ converter.hashCode();
	}

	private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
		out.defaultWriteObject();
		MethodWrapper[] wrappers = new MethodWrapper[functions.length];
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new MethodWrapper(functions[i]);
		}
		out.writeObject(wrappers);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		MethodWrapper[] wrappers = (MethodWrapper[])in.readObject();
		if (wrappers.length == 0) {
			functions = NO_FUNCTIONS;
		} else {
			functions = new Method[wrappers.length];
			for (int i = 0; i < functions.length; i++) {
				functions[i] = wrappers[i].method;
			}
		}
	}	
}
