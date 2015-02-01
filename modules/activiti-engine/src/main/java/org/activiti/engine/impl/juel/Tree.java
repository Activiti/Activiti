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

import java.lang.reflect.Method;
import java.util.Collection;

import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.FunctionMapper;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.javax.el.VariableMapper;


/**
 * Parsed expression, usually created by a {@link org.activiti.engine.impl.juel.TreeBuilder}.
 * The {@link #bind(FunctionMapper, VariableMapper)} method is used to create
 * {@link org.activiti.engine.impl.juel.Bindings}, which are needed at evaluation time to
 * lookup functions and variables. The tree itself does not contain such information,
 * because it would make the tree depend on the function/variable mapper supplied at
 * parse time.
 * 
 * @author Christoph Beck
 */
public class Tree {
	private final ExpressionNode root;
	private final Collection<FunctionNode> functions;
	private final Collection<IdentifierNode> identifiers;
	private final boolean deferred;

	/**
	 * 
	 * Constructor.
	 * @param root root node
	 * @param functions collection of function nodes
	 * @param identifiers collection of identifier nodes
	 */
	public Tree(ExpressionNode root, Collection<FunctionNode> functions, Collection<IdentifierNode> identifiers, boolean deferred) {
		super();
		this.root = root;
		this.functions = functions;
		this.identifiers = identifiers;
		this.deferred = deferred;
	}

	/**
	 * Get function nodes (in no particular order)
	 */
	public Iterable<FunctionNode> getFunctionNodes() {
		return functions;
	}
	
	/**
	 * Get identifier nodes (in no particular order)
	 */
	public Iterable<IdentifierNode> getIdentifierNodes() {
		return identifiers;
	}
	
	/**
	 * @return root node
	 */
	public ExpressionNode getRoot() {
		return root;
	}
	
	public boolean isDeferred() {
		return deferred;
	}
	
	@Override
	public String toString() {
		return getRoot().getStructuralId(null);
	}

	/**
	 * Create a bindings.
	 * @param fnMapper the function mapper to use
	 * @param varMapper the variable mapper to use
	 * @return tree bindings
	 */
	public Bindings bind(FunctionMapper fnMapper, VariableMapper varMapper) {
		return bind(fnMapper, varMapper, null);
	}

	/**
	 * Create a bindings.
	 * @param fnMapper the function mapper to use
	 * @param varMapper the variable mapper to use
	 * @param converter custom type converter
	 * @return tree bindings
	 */
	public Bindings bind(FunctionMapper fnMapper, VariableMapper varMapper, TypeConverter converter) {
		Method[] methods = null;
		if (!functions.isEmpty()) {
			if (fnMapper == null) {
				throw new ELException(LocalMessages.get("error.function.nomapper"));
			}
			methods = new Method[functions.size()];
			for (FunctionNode node: functions) {
				String image = node.getName();
				Method method = null;
				int colon = image.indexOf(':');
				if (colon < 0) {
					method = fnMapper.resolveFunction("", image);
				} else {
					method = fnMapper.resolveFunction(image.substring(0, colon), image.substring(colon + 1));
				}
				if (method == null) {
					throw new ELException(LocalMessages.get("error.function.notfound", image));
				}
				if (node.isVarArgs() && method.isVarArgs()) {
					if (method.getParameterTypes().length > node.getParamCount() + 1) {
						throw new ELException(LocalMessages.get("error.function.params", image));
					}
				} else {
					if (method.getParameterTypes().length != node.getParamCount()) {
						throw new ELException(LocalMessages.get("error.function.params", image));
					}
				}
				methods[node.getIndex()] = method;
			}
		}
		ValueExpression[] expressions = null;
		if (!identifiers.isEmpty()) {
			expressions = new ValueExpression[identifiers.size()];
			for (IdentifierNode node: identifiers) {
				ValueExpression expression = null;
				if (varMapper != null) {
					expression = varMapper.resolveVariable(node.getName());
				}
				expressions[node.getIndex()] = expression;
			}
		}
		return new Bindings(methods, expressions, converter);
	}
}
