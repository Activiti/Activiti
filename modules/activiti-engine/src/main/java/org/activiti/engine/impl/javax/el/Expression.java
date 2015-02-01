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

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Base class for the expression subclasses {@link ValueExpression} and {@link MethodExpression},
 * implementing characterstics common to both. All expressions must implement the equals() and
 * hashCode() methods so that two expressions can be compared for equality. They are redefined
 * abstract in this class to force their implementation in subclasses. All expressions must also be
 * Serializable so that they can be saved and restored. Expressions are also designed to be
 * immutable so that only one instance needs to be created for any given expression String /
 * {@link FunctionMapper}. This allows a container to pre-create expressions and not have to reparse
 * them each time they are evaluated.
 */
public abstract class Expression implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Determines whether the specified object is equal to this Expression. The result is true if
	 * and only if the argument is not null, is an Expression object that is the of the same type
	 * (ValueExpression or MethodExpression), and has an identical parsed representation. Note that
	 * two expressions can be equal if their expression Strings are different. For example,
	 * ${fn1:foo()} and ${fn2:foo()} are equal if their corresponding FunctionMappers mapped fn1:foo
	 * and fn2:foo to the same method.
	 * 
	 * @param obj
	 *            the Object to test for equality.
	 * @return true if obj equals this Expression; false otherwise.
	 */
	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Returns the original String used to create this Expression, unmodified. This is used for
	 * debugging purposes but also for the purposes of comparison (e.g. to ensure the expression in
	 * a configuration file has not changed). This method does not provide sufficient information to
	 * re-create an expression. Two different expressions can have exactly the same expression
	 * string but different function mappings. Serialization should be used to save and restore the
	 * state of an Expression.
	 * 
	 * @return The original expression String.
	 */
	public abstract String getExpressionString();

	/**
	 * Returns the hash code for this Expression. See the note in the {@link #equals(Object)} method
	 * on how two expressions can be equal if their expression Strings are different. Recall that if
	 * two objects are equal according to the equals(Object) method, then calling the hashCode
	 * method on each of the two objects must produce the same integer result. Implementations must
	 * take special note and implement hashCode correctly.
	 * 
	 * @return The hash code for this Expression.
	 * @see #equals(Object)
	 * @see Hashtable
	 * @see Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Returns whether this expression was created from only literal text. This method must return
	 * true if and only if the expression string this expression was created from contained no
	 * unescaped EL delimeters (${...} or #{...}).
	 * 
	 * @return true if this expression was created from only literal text; false otherwise.
	 */
	public abstract boolean isLiteralText();
}
