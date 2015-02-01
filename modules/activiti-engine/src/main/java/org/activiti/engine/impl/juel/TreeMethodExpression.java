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
import java.io.PrintWriter;
import java.util.Arrays;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.FunctionMapper;
import org.activiti.engine.impl.javax.el.MethodInfo;
import org.activiti.engine.impl.javax.el.VariableMapper;


/**
 * A method expression is ready to be evaluated (by calling either
 * {@link #invoke(ELContext, Object[])} or {@link #getMethodInfo(ELContext)}).
 *
 * Instances of this class are usually created using an {@link ExpressionFactoryImpl}.
 * 
 * @author Christoph Beck
 */
public final class TreeMethodExpression extends org.activiti.engine.impl.javax.el.MethodExpression {
	private static final long serialVersionUID = 1L;

	private final TreeBuilder builder;
	private final Bindings bindings;
	private final String expr;
	private final Class<?> type;
	private final Class<?>[] types;
	private final boolean deferred;

	private transient ExpressionNode node;

	private String structure;

	/**
	 * Create a new method expression.
	 * The expression must be an lvalue expression or literal text.
	 * The expected return type may be <code>null</code>, meaning "don't care".
	 * If it is an lvalue expression, the parameter types must not be <code>null</code>.
	 * If it is literal text, the expected return type must not be <code>void</code>.
	 * @param store used to get the parse tree from.
	 * @param functions the function mapper used to bind functions
	 * @param variables the variable mapper used to bind variables
	 * @param expr the expression string
	 * @param returnType the expected return type (may be <code>null</code>)
	 * @param paramTypes the expected parameter types (must not be <code>null</code> for lvalues)
	 */
	public TreeMethodExpression(TreeStore store, FunctionMapper functions, VariableMapper variables, TypeConverter converter, String expr, Class<?> returnType, Class<?>[] paramTypes) {
		super();

		Tree tree = store.get(expr);

		this.builder = store.getBuilder();
		this.bindings = tree.bind(functions, variables, converter);
		this.expr = expr;
		this.type = returnType;
		this.types = paramTypes;
		this.node = tree.getRoot();
		this.deferred = tree.isDeferred();

		if (node.isLiteralText()) {
			if (returnType == void.class) {
				throw new ELException(LocalMessages.get("error.method.literal.void", expr));
			}
		} else if (!node.isMethodInvocation()) {
			if (!node.isLeftValue()) {
				throw new ELException(LocalMessages.get("error.method.invalid", expr));
			}
			if (paramTypes == null) {
				throw new ELException(LocalMessages.get("error.method.notypes"));
			}
		}
	}

	private String getStructuralId() {
		if (structure == null) {
			structure = node.getStructuralId(bindings);
		}
		return structure;
	}
	
  /**
   * Evaluates the expression and answers information about the method
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * @return method information or <code>null</code> for literal expressions
   * @throws ELException if evaluation fails (e.g. suitable method not found)
   */
	@Override
	public MethodInfo getMethodInfo(ELContext context) throws ELException {
		return node.getMethodInfo(bindings, context, type, types);
	}

	@Override
	public String getExpressionString() {
		return expr;
	}

	/**
   * Evaluates the expression and invokes the method.
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * @param paramValues
   * @return method result or <code>null</code> if this is a literal text expression
   * @throws ELException if evaluation fails (e.g. suitable method not found)
   */
	@Override
	public Object invoke(ELContext context, Object[] paramValues) throws ELException {
		return node.invoke(bindings, context, type, types, paramValues);
	}

	/**
	 * @return <code>true</code> if this is a literal text expression
	 */
	@Override
	public boolean isLiteralText() {
		return node.isLiteralText();
	}

	/**
	 * @return <code>true</code> if this is a method invocation expression
	 */
	@Override
	public boolean isParmetersProvided() {
		return node.isMethodInvocation();
	}
	
	/**
	 * Answer <code>true</code> if this is a deferred expression (starting with <code>#{</code>)
	 */
	public boolean isDeferred() {
		return deferred;
	}

	/**
	 * Expressions are compared using the concept of a <em>structural id</em>:
   * variable and function names are anonymized such that two expressions with
   * same tree structure will also have the same structural id and vice versa.
	 * Two method expressions are equal if
	 * <ol>
	 * <li>their builders are equal</li>
	 * <li>their structural id's are equal</li>
	 * <li>their bindings are equal</li>
	 * <li>their expected types match</li>
	 * <li>their parameter types are equal</li>
	 * </ol>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			TreeMethodExpression other = (TreeMethodExpression)obj;
			if (!builder.equals(other.builder)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			if (!Arrays.equals(types, other.types)) {
				return false;
			}			
			return getStructuralId().equals(other.getStructuralId()) && bindings.equals(other.bindings);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getStructuralId().hashCode();
	}

	@Override
	public String toString() {
		return "TreeMethodExpression(" + expr + ")";
	}

	/**
	 * Print the parse tree.
	 * @param writer
	 */
	public void dump(PrintWriter writer) {
		NodePrinter.dump(writer, node);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			node = builder.build(expr).getRoot();
		} catch (ELException e) {
			throw new IOException(e.getMessage());
		}
	}	
}
