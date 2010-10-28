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

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.FunctionMapper;
import org.activiti.engine.impl.javax.el.ValueReference;
import org.activiti.engine.impl.javax.el.VariableMapper;


/**
 * A value expression is ready to be evaluated (by calling either
 * {@link #getType(ELContext)}, {@link #getValue(ELContext)}, {@link #isReadOnly(ELContext)}
 * or {@link #setValue(ELContext, Object)}.
 * 
 * Instances of this class are usually created using an {@link ExpressionFactoryImpl}.
 *
 * @author Christoph Beck
 */
public final class TreeValueExpression extends org.activiti.engine.impl.javax.el.ValueExpression {
	private static final long serialVersionUID = 1L;

	private final TreeBuilder builder;
	private final Bindings bindings;
	private final String expr;
	private final Class<?> type;
	private final boolean deferred;

	private transient ExpressionNode node;

	private String structure;

	/**
	 * Create a new value expression.
	 * @param store used to get the parse tree from.
	 * @param functions the function mapper used to bind functions
	 * @param variables the variable mapper used to bind variables
	 * @param expr the expression string
	 * @param type the expected type (may be <code>null</code>)
	 */
	public TreeValueExpression(TreeStore store, FunctionMapper functions, VariableMapper variables, TypeConverter converter, String expr, Class<?> type) {
		super();

		Tree tree = store.get(expr);

		this.builder = store.getBuilder();
		this.bindings = tree.bind(functions, variables, converter);
		this.expr = expr;
		this.type = type;
		this.node = tree.getRoot();
		this.deferred = tree.isDeferred();
		
		if (type == null) {
			throw new NullPointerException(LocalMessages.get("error.value.notype"));
		}
	}

	private String getStructuralId() {
		if (structure == null) {
			structure = node.getStructuralId(bindings);
		}
		return structure;
	}
	
	@Override
	public Class<?> getExpectedType() {
		return type;
	}

	@Override
	public String getExpressionString() {
		return expr;
	}

  /**
   * Evaluates the expression as an lvalue and answers the result type.
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * and to determine the result from the last base/property pair
   * @return lvalue evaluation type or <code>null</code> for rvalue expressions
   * @throws ELException if evaluation fails (e.g. property not found, type conversion failed, ...)
   */
	@Override
	public Class<?> getType(ELContext context) throws ELException {
		return node.getType(bindings, context);
	}

  /**
   * Evaluates the expression as an rvalue and answers the result.
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * and to determine the result from the last base/property pair
   * @return rvalue evaluation result
   * @throws ELException if evaluation fails (e.g. property not found, type conversion failed, ...)
   */
	@Override
	public Object getValue(ELContext context) throws ELException {
		return node.getValue(bindings, context, type);
	}

	/**
   * Evaluates the expression as an lvalue and determines if {@link #setValue(ELContext, Object)}
   * will always fail.
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * and to determine the result from the last base/property pair
   * @return <code>true</code> if {@link #setValue(ELContext, Object)} always fails.
   * @throws ELException if evaluation fails (e.g. property not found, type conversion failed, ...)
	 */
	@Override
	public boolean isReadOnly(ELContext context) throws ELException {
		return node.isReadOnly(bindings, context);
	}

	/**
   * Evaluates the expression as an lvalue and assigns the given value.
   * @param context used to resolve properties (<code>base.property</code> and <code>base[property]</code>)
   * and to perform the assignment to the last base/property pair
   * @throws ELException if evaluation fails (e.g. property not found, type conversion failed, assignment failed...)
	 */
	@Override
	public void setValue(ELContext context, Object value) throws ELException {
		node.setValue(bindings, context, value); 
	}

	/**
	 * @return <code>true</code> if this is a literal text expression
	 */
	@Override
	public boolean isLiteralText() {
		return node.isLiteralText();
	}

	@Override
	public ValueReference getValueReference(ELContext context) {
		return node.getValueReference(bindings, context);
	}

	/**
	 * Answer <code>true</code> if this could be used as an lvalue.
	 * This is the case for eval expressions consisting of a simple identifier or
	 * a nonliteral prefix, followed by a sequence of property operators (<code>.</code> or <code>[]</code>)
	 */
	public boolean isLeftValue() {
		return node.isLeftValue();
	}
	
	/**
	 * Answer <code>true</code> if this is a deferred expression (containing
	 * sub-expressions starting with <code>#{</code>)
	 */
	public boolean isDeferred() {
		return deferred;
	}
	
	/**
	 * Expressions are compared using the concept of a <em>structural id</em>:
   * variable and function names are anonymized such that two expressions with
   * same tree structure will also have the same structural id and vice versa.
	 * Two value expressions are equal if
	 * <ol>
	 * <li>their structural id's are equal</li>
	 * <li>their bindings are equal</li>
	 * <li>their expected types are equal</li>
	 * </ol>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			TreeValueExpression other = (TreeValueExpression)obj;
			if (!builder.equals(other.builder)) {
				return false;
			}
			if (type != other.type) {
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
		return "TreeValueExpression(" + expr + ")";
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
