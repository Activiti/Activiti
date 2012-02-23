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

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains an ordered composite list of child ELResolvers. Though only a single ELResolver is
 * associated with an ELContext, there are usually multiple resolvers considered for any given
 * variable or property resolution. ELResolvers are combined together using a CompositeELResolver,
 * to define rich semantics for evaluating an expression. For the
 * {@link #getValue(ELContext, Object, Object)}, {@link #getType(ELContext, Object, Object)},
 * {@link #setValue(ELContext, Object, Object, Object)} and
 * {@link #isReadOnly(ELContext, Object, Object)} methods, an ELResolver is not responsible for
 * resolving all possible (base, property) pairs. In fact, most resolvers will only handle a base of
 * a single type. To indicate that a resolver has successfully resolved a particular (base,
 * property) pair, it must set the propertyResolved property of the ELContext to true. If it could
 * not handle the given pair, it must leave this property alone. The caller must ignore the return
 * value of the method if propertyResolved is false. The CompositeELResolver initializes the
 * ELContext.propertyResolved flag to false, and uses it as a stop condition for iterating through
 * its component resolvers. The ELContext.propertyResolved flag is not used for the design-time
 * methods {@link #getFeatureDescriptors(ELContext, Object)} and
 * {@link #getCommonPropertyType(ELContext, Object)}. Instead, results are collected and combined
 * from all child ELResolvers for these methods.
 */
public class CompositeELResolver extends ELResolver {
	private final List<ELResolver> resolvers = new ArrayList<ELResolver>();

	/**
	 * Adds the given resolver to the list of component resolvers. Resolvers are consulted in the
	 * order in which they are added.
	 * 
	 * @param elResolver
	 *            The component resolver to add.
	 * @throws NullPointerException
	 *             If the provided resolver is null.
	 */
	public void add(ELResolver elResolver) {
		if (elResolver == null) {
			throw new NullPointerException("resolver must not be null");
		}
		resolvers.add(elResolver);
	}

	/**
	 * Returns the most general type that this resolver accepts for the property argument, given a
	 * base object. One use for this method is to assist tools in auto-completion. The result is
	 * obtained by querying all component resolvers. The Class returned is the most specific class
	 * that is a common superclass of all the classes returned by each component resolver's
	 * getCommonPropertyType method. If null is returned by a resolver, it is skipped.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @return null if this ELResolver does not know how to handle the given base object; otherwise
	 *         Object.class if any type of property is accepted; otherwise the most general property
	 *         type accepted for the given base.
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		Class<?> result = null;
		for (ELResolver resolver : resolvers) {
			Class<?> type = resolver.getCommonPropertyType(context, base);
			if (type != null) {
				if (result == null || type.isAssignableFrom(result)) {
					result = type;
				} else if (!result.isAssignableFrom(type)) {
					result = Object.class;
				}
			}
		}
		return result;
	}

	/**
	 * Returns information about the set of variables or properties that can be resolved for the
	 * given base object. One use for this method is to assist tools in auto-completion. The results
	 * are collected from all component resolvers. The propertyResolved property of the ELContext is
	 * not relevant to this method. The results of all ELResolvers are concatenated. The Iterator
	 * returned is an iterator over the collection of FeatureDescriptor objects returned by the
	 * iterators returned by each component resolver's getFeatureDescriptors method. If null is
	 * returned by a resolver, it is skipped.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @return An Iterator containing zero or more (possibly infinitely more) FeatureDescriptor
	 *         objects, or null if this resolver does not handle the given base object or that the
	 *         results are too complex to represent with this method
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base) {
		return new Iterator<FeatureDescriptor>() {
			Iterator<FeatureDescriptor> empty = Collections.<FeatureDescriptor> emptyList().iterator();
			Iterator<ELResolver> resolvers = CompositeELResolver.this.resolvers.iterator();
			Iterator<FeatureDescriptor> features = empty;

			Iterator<FeatureDescriptor> features() {
				while (!features.hasNext() && resolvers.hasNext()) {
					features = resolvers.next().getFeatureDescriptors(context, base);
					if (features == null) {
						features = empty;
					}
				}
				return features;
			}

			public boolean hasNext() {
				return features().hasNext();
			}

			public FeatureDescriptor next() {
				return features().next();
			}

			public void remove() {
				features().remove();
			}
		};
	}

	/**
	 * For a given base and property, attempts to identify the most general type that is acceptable
	 * for an object to be passed as the value parameter in a future call to the
	 * {@link #setValue(ELContext, Object, Object, Object)} method. The result is obtained by
	 * querying all component resolvers. If this resolver handles the given (base, property) pair,
	 * the propertyResolved property of the ELContext object must be set to true by the resolver,
	 * before returning. If this property is not true after this method is called, the caller should
	 * ignore the return value. First, propertyResolved is set to false on the provided ELContext.
	 * Next, for each component resolver in this composite:
	 * <ol>
	 * <li>The getType() method is called, passing in the provided context, base and property.</li>
	 * <li>If the ELContext's propertyResolved flag is false then iteration continues.</li>
	 * <li>Otherwise, iteration stops and no more component resolvers are considered. The value
	 * returned by getType() is returned by this method.</li>
	 * </ol>
	 * If none of the component resolvers were able to perform this operation, the value null is
	 * returned and the propertyResolved flag remains set to false. Any exception thrown by
	 * component resolvers during the iteration is propagated to the caller of this method.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @param property
	 *            The property or variable to return the acceptable type for.
	 * @return If the propertyResolved property of ELContext was set to true, then the most general
	 *         acceptable type; otherwise undefined.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if base is not null and the specified property does not exist or is not readable.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		context.setPropertyResolved(false);
		for (ELResolver resolver : resolvers) {
			Class<?> type = resolver.getType(context, base, property);
			if (context.isPropertyResolved()) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Attempts to resolve the given property object on the given base object by querying all
	 * component resolvers. If this resolver handles the given (base, property) pair, the
	 * propertyResolved property of the ELContext object must be set to true by the resolver, before
	 * returning. If this property is not true after this method is called, the caller should ignore
	 * the return value. First, propertyResolved is set to false on the provided ELContext. Next,
	 * for each component resolver in this composite:
	 * <ol>
	 * <li>The getValue() method is called, passing in the provided context, base and property.</li>
	 * <li>If the ELContext's propertyResolved flag is false then iteration continues.</li>
	 * <li>Otherwise, iteration stops and no more component resolvers are considered. The value
	 * returned by getValue() is returned by this method.</li>
	 * </ol>
	 * If none of the component resolvers were able to perform this operation, the value null is
	 * returned and the propertyResolved flag remains set to false. Any exception thrown by
	 * component resolvers during the iteration is propagated to the caller of this method.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @param property
	 *            The property or variable to return the acceptable type for.
	 * @return If the propertyResolved property of ELContext was set to true, then the result of the
	 *         variable or property resolution; otherwise undefined.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if base is not null and the specified property does not exist or is not readable.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		context.setPropertyResolved(false);
		for (ELResolver resolver : resolvers) {
			Object value = resolver.getValue(context, base, property);
			if (context.isPropertyResolved()) {
				return value;
			}
		}
		return null;
	}

	/**
	 * For a given base and property, attempts to determine whether a call to
	 * {@link #setValue(ELContext, Object, Object, Object)} will always fail. The result is obtained
	 * by querying all component resolvers. If this resolver handles the given (base, property)
	 * pair, the propertyResolved property of the ELContext object must be set to true by the
	 * resolver, before returning. If this property is not true after this method is called, the
	 * caller should ignore the return value. First, propertyResolved is set to false on the
	 * provided ELContext. Next, for each component resolver in this composite:
	 * <ol>
	 * <li>The isReadOnly() method is called, passing in the provided context, base and property.</li>
	 * <li>If the ELContext's propertyResolved flag is false then iteration continues.</li>
	 * <li>Otherwise, iteration stops and no more component resolvers are considered. The value
	 * returned by isReadOnly() is returned by this method.</li>
	 * </ol>
	 * If none of the component resolvers were able to perform this operation, the value false is
	 * returned and the propertyResolved flag remains set to false. Any exception thrown by
	 * component resolvers during the iteration is propagated to the caller of this method.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @param property
	 *            The property or variable to return the acceptable type for.
	 * @return If the propertyResolved property of ELContext was set to true, then true if the
	 *         property is read-only or false if not; otherwise undefined.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if base is not null and the specified property does not exist or is not readable.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		context.setPropertyResolved(false);
		for (ELResolver resolver : resolvers) {
			boolean readOnly = resolver.isReadOnly(context, base, property);
			if (context.isPropertyResolved()) {
				return readOnly;
			}
		}
		return false;
	}

	/**
	 * Attempts to set the value of the given property object on the given base object. All
	 * component resolvers are asked to attempt to set the value. If this resolver handles the given
	 * (base, property) pair, the propertyResolved property of the ELContext object must be set to
	 * true by the resolver, before returning. If this property is not true after this method is
	 * called, the caller can safely assume no value has been set. First, propertyResolved is set to
	 * false on the provided ELContext. Next, for each component resolver in this composite:
	 * <ol>
	 * <li>The setValue() method is called, passing in the provided context, base, property and
	 * value.</li>
	 * <li>If the ELContext's propertyResolved flag is false then iteration continues.</li>
	 * <li>Otherwise, iteration stops and no more component resolvers are considered.</li>
	 * </ol>
	 * If none of the component resolvers were able to perform this operation, the propertyResolved
	 * flag remains set to false. Any exception thrown by component resolvers during the iteration
	 * is propagated to the caller of this method.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The base object to return the most general property type for, or null to enumerate
	 *            the set of top-level variables that this resolver can evaluate.
	 * @param property
	 *            The property or variable to return the acceptable type for.
	 * @param value
	 *            The value to set the property or variable to.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws PropertyNotFoundException
	 *             if base is not null and the specified property does not exist or is not readable.
	 * @throws PropertyNotWritableException
	 *             if the given (base, property) pair is handled by this ELResolver but the
	 *             specified variable or property is not writable.
	 * @throws ELException
	 *             if an exception was thrown while attempting to set the property or variable. The
	 *             thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		context.setPropertyResolved(false);
		for (ELResolver resolver : resolvers) {
			resolver.setValue(context, base, property, value);
			if (context.isPropertyResolved()) {
				return;
			}
		}
	}

	/**
	 * Attempts to resolve and invoke the given <code>method</code> on the given <code>base</code>
	 * object by querying all component resolvers.
	 * 
	 * <p>
	 * If this resolver handles the given (base, method) pair, the <code>propertyResolved</code>
	 * property of the <code>ELContext</code> object must be set to <code>true</code> by the
	 * resolver, before returning. If this property is not <code>true</code> after this method is
	 * called, the caller should ignore the return value.
	 * </p>
	 * 
	 * <p>
	 * First, <code>propertyResolved</code> is set to <code>false</code> on the provided
	 * <code>ELContext</code>.
	 * </p>
	 * 
	 * <p>
	 * Next, for each component resolver in this composite:
	 * <ol>
	 * <li>The <code>invoke()</code> method is called, passing in the provided <code>context</code>,
	 * <code>base</code>, <code>method</code>, <code>paramTypes</code>, and <code>params</code>.</li>
	 * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code>
	 * then iteration continues.</li>
	 * <li>Otherwise, iteration stops and no more component resolvers are considered. The value
	 * returned by <code>getValue()</code> is returned by this method.</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * If none of the component resolvers were able to perform this operation, the value
	 * <code>null</code> is returned and the <code>propertyResolved</code> flag remains set to
	 * <code>false</code>
	 * </p>
	 * 
	 * <p>
	 * Any exception thrown by component resolvers during the iteration is propagated to the caller
	 * of this method.
	 * </p>
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The bean on which to invoke the method
	 * @param method
	 *            The simple name of the method to invoke. Will be coerced to a <code>String</code>.
	 *            If method is "&lt;init&gt;"or "&lt;clinit&gt;" a NoSuchMethodException is raised.
	 * @param paramTypes
	 *            An array of Class objects identifying the method's formal parameter types, in
	 *            declared order. Use an empty array if the method has no parameters. Can be
	 *            <code>null</code>, in which case the method's formal parameter types are assumed
	 *            to be unknown.
	 * @param params
	 *            The parameters to pass to the method, or <code>null</code> if no parameters.
	 * @return The result of the method invocation (<code>null</code> if the method has a
	 *         <code>void</code> return type).
	 * @since 2.2
	 */
	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		context.setPropertyResolved(false);
		for (ELResolver resolver : resolvers) {
			Object result = resolver.invoke(context, base, method, paramTypes, params);
			if (context.isPropertyResolved()) {
				return result;
			}
		}
		return null;
	}
}
