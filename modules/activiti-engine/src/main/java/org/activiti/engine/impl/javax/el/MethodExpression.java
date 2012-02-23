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
 * An Expression that refers to a method on an object. The
 * {@link ExpressionFactory#createMethodExpression(ELContext, String, Class, Class[])} method can be
 * used to parse an expression string and return a concrete instance of MethodExpression that
 * encapsulates the parsed expression. The {@link FunctionMapper} is used at parse time, not
 * evaluation time, so one is not needed to evaluate an expression using this class. However, the
 * {@link ELContext} is needed at evaluation time. The {@link #getMethodInfo(ELContext)} and
 * {@link #invoke(ELContext, Object[])} methods will evaluate the expression each time they are
 * called. The {@link ELResolver} in the ELContext is used to resolve the top-level variables and to
 * determine the behavior of the . and [] operators. For any of the two methods, the
 * {@link ELResolver#getValue(ELContext, Object, Object)} method is used to resolve all properties
 * up to but excluding the last one. This provides the base object on which the method appears. If
 * the base object is null, a PropertyNotFoundException must be thrown. At the last resolution, the
 * final property is then coerced to a String, which provides the name of the method to be found. A
 * method matching the name and expected parameters provided at parse time is found and it is either
 * queried or invoked (depending on the method called on this MethodExpression). See the notes about
 * comparison, serialization and immutability in the {@link Expression} javadocs.
 * 
 * @see ELResolver
 * @see Expression
 * @see ExpressionFactory
 */
public abstract class MethodExpression extends Expression {
	private static final long serialVersionUID = 1L;

	/**
	 * Evaluates the expression relative to the provided context, and returns information about the
	 * actual referenced method.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @return The context of this evaluation
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if one of the property resolutions failed because a specified variable or
	 *             property does not exist or is not readable.
	 * @throws MethodNotFoundException
	 *             if no suitable method can be found.
	 * @throws ELException
	 *             if an exception was thrown while performing property or variable resolution. The
	 *             thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	public abstract MethodInfo getMethodInfo(ELContext context);

	/**
	 * If a String literal is specified as the expression, returns the String literal coerced to the
	 * expected return type of the method signature. An ELException is thrown if expectedReturnType
	 * is void or if the coercion of the String literal to the expectedReturnType yields an error
	 * (see Section "1.16 Type Conversion" of the EL specification). If not a String literal,
	 * evaluates the expression relative to the provided context, invokes the method that was found
	 * using the supplied parameters, and returns the result of the method invocation. Any
	 * parameters passed to this method is ignored if isLiteralText() is true.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param params
	 *            The parameters to pass to the method, or null if no parameters.
	 * @return the result of the method invocation (null if the method has a void return type).
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if one of the property resolutions failed because a specified variable or
	 *             property does not exist or is not readable.
	 * @throws MethodNotFoundException
	 *             if no suitable method can be found.
	 * @throws ELException
	 *             if a String literal is specified and expectedReturnType of the MethodExpression
	 *             is void or if the coercion of the String literal to the expectedReturnType yields
	 *             an error (see Section "1.16 Type Conversion").
	 * @throws ELException
	 *             if an exception was thrown while performing property or variable resolution. The
	 *             thrown exception must be included as the cause property of this exception, if
	 *             available. If the exception thrown is an InvocationTargetException, extract its
	 *             cause and pass it to the ELException constructor.
	 */
	public abstract Object invoke(ELContext context, Object[] params);

	/**
	 * Return whether this MethodExpression was created with parameters.
	 * 
	 * <p>
	 * This method must return <code>true</code> if and only if parameters are specified in the EL,
	 * using the expr-a.expr-b(...) syntax.
	 * </p>
	 * 
	 * @return <code>true</code> if the MethodExpression was created with parameters,
	 *         <code>false</code> otherwise.
	 * @since 2.2
	 */
	public boolean isParmetersProvided() {
		return false;
	}
}
