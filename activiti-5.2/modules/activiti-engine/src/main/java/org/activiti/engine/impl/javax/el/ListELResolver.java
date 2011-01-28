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
import java.util.Iterator;
import java.util.List;

/**
 * Defines property resolution behavior on instances of java.util.List. This resolver handles base
 * objects of type java.util.List. It accepts any object as a property and coerces that object into
 * an integer index into the list. The resulting value is the value in the list at that index. This
 * resolver can be constructed in read-only mode, which means that isReadOnly will always return
 * true and {@link #setValue(ELContext, Object, Object, Object)} will always throw
 * PropertyNotWritableException. ELResolvers are combined together using {@link CompositeELResolver}
 * s, to define rich semantics for evaluating an expression. See the javadocs for {@link ELResolver}
 * for details.
 */
public class ListELResolver extends ELResolver {
	private final boolean readOnly;

	/**
	 * Creates a new read/write ListELResolver.
	 */
	public ListELResolver() {
		this(false);
	}

	/**
	 * Creates a new ListELResolver whose read-only status is determined by the given parameter.
	 * 
	 * @param readOnly
	 *            true if this resolver cannot modify lists; false otherwise.
	 */
	public ListELResolver(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * If the base object is a list, returns the most general type that this resolver accepts for
	 * the property argument. Otherwise, returns null. Assuming the base is a List, this method will
	 * always return Integer.class. This is because Lists accept integers as their index.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @return null if base is not a List; otherwise Integer.class.
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return isResolvable(base) ? Integer.class : null;
	}

	/**
	 * Always returns null, since there is no reason to iterate through set set of all integers.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @return null.
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	/**
	 * If the base object is a list, returns the most general acceptable type for a value in this
	 * list. If the base is a List, the propertyResolved property of the ELContext object must be
	 * set to true by this resolver, before returning. If this property is not true after this
	 * method is called, the caller should ignore the return value. Assuming the base is a List,
	 * this method will always return Object.class. This is because Lists accept any object as an
	 * element.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @param property
	 *            The index of the element in the list to return the acceptable type for. Will be
	 *            coerced into an integer, but otherwise ignored by this resolver.
	 * @return If the propertyResolved property of ELContext was set to true, then the most general
	 *         acceptable type; otherwise undefined.
	 * @throws PropertyNotFoundException
	 *             if the given index is out of bounds for this list.
	 * @throws IllegalArgumentException
	 *             if the property could not be coerced into an integer.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		Class<?> result = null;
		if (isResolvable(base)) {
			toIndex((List<?>) base, property);
			result = Object.class;
			context.setPropertyResolved(true);
		}
		return result;
	}

	/**
	 * If the base object is a list, returns the value at the given index. The index is specified by
	 * the property argument, and coerced into an integer. If the coercion could not be performed,
	 * an IllegalArgumentException is thrown. If the index is out of bounds, null is returned. If
	 * the base is a List, the propertyResolved property of the ELContext object must be set to true
	 * by this resolver, before returning. If this property is not true after this method is called,
	 * the caller should ignore the return value.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @param property
	 *            The index of the element in the list to return the acceptable type for. Will be
	 *            coerced into an integer, but otherwise ignored by this resolver.
	 * @return If the propertyResolved property of ELContext was set to true, then the value at the
	 *         given index or null if the index was out of bounds. Otherwise, undefined.
	 * @throws PropertyNotFoundException
	 *             if the given index is out of bounds for this list.
	 * @throws IllegalArgumentException
	 *             if the property could not be coerced into an integer.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		Object result = null;
		if (isResolvable(base)) {
			int index = toIndex(null, property);
			List<?> list = (List<?>) base;
			result = index < 0 || index >= list.size() ? null : list.get(index);
			context.setPropertyResolved(true);
		}
		return result;
	}

	/**
	 * If the base object is a list, returns whether a call to
	 * {@link #setValue(ELContext, Object, Object, Object)} will always fail. If the base is a List,
	 * the propertyResolved property of the ELContext object must be set to true by this resolver,
	 * before returning. If this property is not true after this method is called, the caller should
	 * ignore the return value. If this resolver was constructed in read-only mode, this method will
	 * always return true. If a List was created using java.util.Collections.unmodifiableList(List),
	 * this method must return true. Unfortunately, there is no Collections API method to detect
	 * this. However, an implementation can create a prototype unmodifiable List and query its
	 * runtime type to see if it matches the runtime type of the base object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @param property
	 *            The index of the element in the list to return the acceptable type for. Will be
	 *            coerced into an integer, but otherwise ignored by this resolver.
	 * @return If the propertyResolved property of ELContext was set to true, then true if calling
	 *         the setValue method will always fail or false if it is possible that such a call may
	 *         succeed; otherwise undefined.
	 * @throws PropertyNotFoundException
	 *             if the given index is out of bounds for this list.
	 * @throws IllegalArgumentException
	 *             if the property could not be coerced into an integer.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (isResolvable(base)) {
			toIndex((List<?>) base, property);
			context.setPropertyResolved(true);
		}
		return readOnly;
	}

	/**
	 * If the base object is a list, attempts to set the value at the given index with the given
	 * value. The index is specified by the property argument, and coerced into an integer. If the
	 * coercion could not be performed, an IllegalArgumentException is thrown. If the index is out
	 * of bounds, a PropertyNotFoundException is thrown. If the base is a List, the propertyResolved
	 * property of the ELContext object must be set to true by this resolver, before returning. If
	 * this property is not true after this method is called, the caller can safely assume no value
	 * was set. If this resolver was constructed in read-only mode, this method will always throw
	 * PropertyNotWritableException. If a List was created using
	 * java.util.Collections.unmodifiableList(List), this method must throw
	 * PropertyNotWritableException. Unfortunately, there is no Collections API method to detect
	 * this. However, an implementation can create a prototype unmodifiable List and query its
	 * runtime type to see if it matches the runtime type of the base object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The list to analyze. Only bases of type List are handled by this resolver.
	 * @param property
	 *            The index of the element in the list to return the acceptable type for. Will be
	 *            coerced into an integer, but otherwise ignored by this resolver.
	 * @param value
	 *            The value to be set at the given index.
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being added to this list.
	 * @throws PropertyNotFoundException
	 *             if the given index is out of bounds for this list.
	 * @throws PropertyNotWritableException
	 *             if this resolver was constructed in read-only mode, or if the set operation is
	 *             not supported by the underlying list.
	 * @throws IllegalArgumentException
	 *             if the property could not be coerced into an integer.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or variable resolution.
	 *             The thrown exception must be included as the cause property of this exception, if
	 *             available.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (isResolvable(base)) {
			if (readOnly) {
				throw new PropertyNotWritableException("resolver is read-only");
			}
			List list = (List) base;
			int index = toIndex(list, property);
			try {
				list.set(index, value);
			} catch (UnsupportedOperationException e) {
				throw new PropertyNotWritableException(e);
			} catch (ArrayStoreException e) {
				throw new IllegalArgumentException(e);
			}
			context.setPropertyResolved(true);
		}
	}

	/**
	 * Test whether the given base should be resolved by this ELResolver.
	 * 
	 * @param base
	 *            The bean to analyze.
	 * @param property
	 *            The name of the property to analyze. Will be coerced to a String.
	 * @return base instanceof List
	 */
	private static final boolean isResolvable(Object base) {
		return base instanceof List<?>;
	}

	/**
	 * Convert the given property to an index in (list) base.
	 * 
	 * @param base
	 *            The bean to analyze.
	 * @param property
	 *            The name of the property to analyze. Will be coerced to a String.
	 * @return The index of property in base.
	 * @throws IllegalArgumentException
	 *             if base property cannot be coerced to an integer.
	 * @throws PropertyNotFoundException
	 *             if base is not null and the computed index is out of bounds for base.
	 */
	private static final int toIndex(List<?> base, Object property) {
		int index = 0;
		if (property instanceof Number) {
			index = ((Number) property).intValue();
		} else if (property instanceof String) {
			try {
				index = Integer.valueOf((String) property);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Cannot parse list index: " + property);
			}
		} else if (property instanceof Character) {
			index = ((Character) property).charValue();
		} else if (property instanceof Boolean) {
			index = ((Boolean) property).booleanValue() ? 1 : 0;
		} else {
			throw new IllegalArgumentException("Cannot coerce property to list index: " + property);
		}
		if (base != null && (index < 0 || index >= base.size())) {
			throw new PropertyNotFoundException("List index out of bounds: " + index);
		}
		return index;
	}
}
