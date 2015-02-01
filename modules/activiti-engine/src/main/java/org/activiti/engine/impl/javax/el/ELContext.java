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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Context information for expression evaluation. To evaluate an {@link Expression}, an ELContext
 * must be provided. The ELContext holds:
 * <ul>
 * <li>a reference to the base {@link ELResolver} that will be consulted to resolve model objects
 * and their properties</li>
 * <li>a reference to {@link FunctionMapper} that will be used to resolve EL Functions.</li>
 * <li>a reference to {@link VariableMapper} that will be used to resolve EL Variables.</li>
 * <li>a collection of all the relevant context objects for use by ELResolvers</li>
 * <li>state information during the evaluation of an expression, such as whether a property has been
 * resolved yet</li>
 * </ul>
 * The collection of context objects is necessary because each ELResolver may need access to a
 * different context object. For example, JSP and Faces resolvers need access to a
 * javax.servlet.jsp.JspContext and a javax.faces.context.FacesContext, respectively. Creation of
 * ELContext objects is controlled through the underlying technology. For example, in JSP the
 * JspContext.getELContext() factory method is used. Some technologies provide the ability to add an
 * {@link ELContextListener} so that applications and frameworks can ensure their own context
 * objects are attached to any newly created ELContext. Because it stores state during expression
 * evaluation, an ELContext object is not thread-safe. Care should be taken to never share an
 * ELContext instance between two or more threads.
 */
public abstract class ELContext {
	private final Map<Class<?>, Object> context = new HashMap<Class<?>, Object>();

	private Locale locale;
	private boolean resolved;

	/**
	 * Returns the context object associated with the given key. The ELContext maintains a
	 * collection of context objects relevant to the evaluation of an expression. These context
	 * objects are used by ELResolvers. This method is used to retrieve the context with the given
	 * key from the collection. By convention, the object returned will be of the type specified by
	 * the key. However, this is not required and the key is used strictly as a unique identifier.
	 * 
	 * @param key
	 *            The unique identifier that was used to associate the context object with this
	 *            ELContext.
	 * @return The context object associated with the given key, or null if no such context was
	 *         found.
	 * @throws NullPointerException
	 *             if key is null.
	 */
	public Object getContext(Class<?> key) {
		return context.get(key);
	}

	/**
	 * Retrieves the ELResolver associated with this context. The ELContext maintains a reference to
	 * the ELResolver that will be consulted to resolve variables and properties during an
	 * expression evaluation. This method retrieves the reference to the resolver. Once an ELContext
	 * is constructed, the reference to the ELResolver associated with the context cannot be
	 * changed.
	 * 
	 * @return The resolver to be consulted for variable and property resolution during expression
	 *         evaluation.
	 */
	public abstract ELResolver getELResolver();

	/**
	 * Retrieves the FunctionMapper associated with this ELContext.
	 * 
	 * @return The function mapper to be consulted for the resolution of EL functions.
	 */
	public abstract FunctionMapper getFunctionMapper();

	/**
	 * Get the Locale stored by a previous invocation to {@link #setLocale(Locale)}. If this method
	 * returns non null, this Locale must be used for all localization needs in the implementation.
	 * The Locale must not be cached to allow for applications that change Locale dynamically.
	 * 
	 * @return The Locale in which this instance is operating. Used primarily for message
	 *         localization.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Retrieves the VariableMapper associated with this ELContext.
	 * 
	 * @return The variable mapper to be consulted for the resolution of EL variables.
	 */
	public abstract VariableMapper getVariableMapper();

	/**
	 * Returns whether an {@link ELResolver} has successfully resolved a given (base, property)
	 * pair. The {@link CompositeELResolver} checks this property to determine whether it should
	 * consider or skip other component resolvers.
	 * 
	 * @return The variable mapper to be consulted for the resolution of EL variables.
	 * @see CompositeELResolver
	 */
	public boolean isPropertyResolved() {
		return resolved;
	}

	/**
	 * Associates a context object with this ELContext. The ELContext maintains a collection of
	 * context objects relevant to the evaluation of an expression. These context objects are used
	 * by ELResolvers. This method is used to add a context object to that collection. By
	 * convention, the contextObject will be of the type specified by the key. However, this is not
	 * required and the key is used strictly as a unique identifier.
	 * 
	 * @param key
	 *            The key used by an {@link ELResolver} to identify this context object.
	 * @param contextObject
	 *            The context object to add to the collection.
	 * @throws NullPointerException
	 *             if key is null or contextObject is null.
	 */
	public void putContext(Class<?> key, Object contextObject) {
		context.put(key, contextObject);
	}

	/**
	 * Set the Locale for this instance. This method may be called by the party creating the
	 * instance, such as JavaServer Faces or JSP, to enable the EL implementation to provide
	 * localized messages to the user. If no Locale is set, the implementation must use the locale
	 * returned by Locale.getDefault( ).
	 * 
	 * @param locale
	 *            The Locale in which this instance is operating. Used primarily for message
	 *            localization.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Called to indicate that a ELResolver has successfully resolved a given (base, property) pair.
	 * The {@link CompositeELResolver} checks this property to determine whether it should consider
	 * or skip other component resolvers.
	 * 
	 * @param resolved
	 *            true if the property has been resolved, or false if not.
	 * @see CompositeELResolver
	 */
	public void setPropertyResolved(boolean resolved) {
		this.resolved = resolved;
	}
}
