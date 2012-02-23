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
package org.activiti.engine.impl.javax.el;

/**
 * The interface to a map between EL variables and the EL expressions they are associated with.
 */
public abstract class VariableMapper {
	/**
	 * Resolves the specified variable name to a ValueExpression.
	 * 
	 * @param variable
	 *            The variable name
	 * @return the ValueExpression assigned to the variable, null if there is no previous assignment
	 *         to this variable.
	 */
	public abstract ValueExpression resolveVariable(String variable);

	/**
	 * Assign a ValueExpression to an EL variable, replacing any previously assignment to the same
	 * variable. The assignment for the variable is removed if the expression is null.
	 * 
	 * @param variable
	 *            The variable name
	 * @param expression
	 *            The ValueExpression to be assigned to the variable.
	 * @return The previous ValueExpression assigned to this variable, null if there is no previous
	 *         assignment to this variable.
	 */
	public abstract ValueExpression setVariable(String variable, ValueExpression expression);
}
