/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import org.activiti.engine.impl.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Defines property resolution behavior on JsonNodes.
 *
 * @see CompositeELResolver
 * @see ELResolver
 */
public class JsonNodeELResolver extends ELResolver {

	private final boolean readOnly;

	/**
	 * Creates a new read/write BeanELResolver.
	 */
	public JsonNodeELResolver() {
		this(false);
	}

	/**
	 * Creates a new BeanELResolver whose read-only status is determined by the
	 * given parameter.
	 */
	public JsonNodeELResolver(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * If the base object is not null, returns the most general type that this
	 * resolver accepts for the property argument. Otherwise, returns null.
	 * Assuming the base is not null, this method will always return
	 * Object.class. This is because any object is accepted as a key and is
	 * coerced into a string.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The bean to analyze.
	 * @return null if base is null; otherwise Object.class.
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return isResolvable(base) ? Object.class : null;
	}

	/**
	 * If the base object is not null, returns an Iterator containing the set of
	 * JavaBeans properties available on the given object. Otherwise, returns
	 * null. The Iterator returned must contain zero or more instances of
	 * java.beans.FeatureDescriptor. Each info object contains information about
	 * a property in the bean, as obtained by calling the
	 * BeanInfo.getPropertyDescriptors method. The FeatureDescriptor is
	 * initialized using the same fields as are present in the
	 * PropertyDescriptor, with the additional required named attributes "type"
	 * and "resolvableAtDesignTime" set as follows:
	 * <ul>
	 * <li>{@link ELResolver#TYPE} - The runtime type of the property, from
	 * PropertyDescriptor.getPropertyType().</li>
	 * <li>{@link ELResolver#RESOLVABLE_AT_DESIGN_TIME} - true.</li>
	 * </ul>
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The bean to analyze.
	 * @return An Iterator containing zero or more FeatureDescriptor objects,
	 *         each representing a property on this bean, or null if the base
	 *         object is null.
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		if (isResolvable(base)) {
			JsonNode node = (JsonNode) base;
			final Iterator<String> keys = node.fieldNames();
			return new Iterator<FeatureDescriptor>() {
				@Override
				public boolean hasNext() {
					return keys.hasNext();
				}

				@Override
				public FeatureDescriptor next() {
					Object key = keys.next();
					FeatureDescriptor feature = new FeatureDescriptor();
					feature.setDisplayName(key == null ? "null" : key.toString());
					feature.setName(feature.getDisplayName());
					feature.setShortDescription("");
					feature.setExpert(true);
					feature.setHidden(false);
					feature.setPreferred(true);
					feature.setValue(TYPE, key == null ? "null" : key.getClass());
					feature.setValue(RESOLVABLE_AT_DESIGN_TIME, true);
					return feature;

				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("cannot remove");
				}
			};
		}
		return null;
	}

	/**
	 * If the base object is a map, returns the most general acceptable type for
	 * a value in this map. If the base is a Map, the propertyResolved property
	 * of the ELContext object must be set to true by this resolver, before
	 * returning. If this property is not true after this method is called, the
	 * caller should ignore the return value. Assuming the base is a Map, this
	 * method will always return Object.class. This is because Maps accept any
	 * object as the value for a given key.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then the most general acceptable type; otherwise undefined.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		Class<?> result = null;
		if (isResolvable(base)) {
			result = Object.class;
			context.setPropertyResolved(true);
		}
		return result;
	}

	/**
	 * If the base object is a map, returns the value associated with the given
	 * key, as specified by the property argument. If the key was not found,
	 * null is returned. If the base is a Map, the propertyResolved property of
	 * the ELContext object must be set to true by this resolver, before
	 * returning. If this property is not true after this method is called, the
	 * caller should ignore the return value. Just as in
	 * java.util.Map.get(Object), just because null is returned doesn't mean
	 * there is no mapping for the key; it's also possible that the Map
	 * explicitly maps the key to null.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then the value associated with the given key or null if the key
	 *         was not found. Otherwise, undefined.
	 * @throws ClassCastException
	 *             if the key is of an inappropriate type for this map
	 *             (optionally thrown by the underlying Map).
	 * @throws NullPointerException
	 *             if context is null, or if the key is null and this map does
	 *             not permit null keys (the latter is optionally thrown by the
	 *             underlying Map).
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		Object result = null;
		if (isResolvable(base)) {
			JsonNode resultNode = ((JsonNode) base).get(property.toString());
			if (resultNode != null && resultNode.isValueNode()) {
				if (resultNode.isBoolean()) {
					result = resultNode.asBoolean();
				} else if (resultNode.isLong()) {
					result = resultNode.asLong();
				} else if (resultNode.isBigDecimal() || resultNode.isDouble()) {
					result = resultNode.asDouble();
				} else if (resultNode.isTextual()) {
					result = resultNode.asText();
				} else {
					result = resultNode.toString();
				}

			} else {
				if (resultNode.isArray()) {
					result = Context.getProcessEngineConfiguration().getObjectMapper().convertValue(resultNode,
							List.class);
				} else {
					result = resultNode;
				}
			}
			context.setPropertyResolved(true);
		}
		return result;
	}

	/**
	 * If the base object is a map, returns whether a call to
	 * {@link #setValue(ELContext, Object, Object, Object)} will always fail. If
	 * the base is a Map, the propertyResolved property of the ELContext object
	 * must be set to true by this resolver, before returning. If this property
	 * is not true after this method is called, the caller should ignore the
	 * return value. If this resolver was constructed in read-only mode, this
	 * method will always return true. If a Map was created using
	 * java.util.Collections.unmodifiableMap(Map), this method must return true.
	 * Unfortunately, there is no Collections API method to detect this.
	 * However, an implementation can create a prototype unmodifiable Map and
	 * query its runtime type to see if it matches the runtime type of the base
	 * object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then true if calling the setValue method will always fail or
	 *         false if it is possible that such a call may succeed; otherwise
	 *         undefined.
	 * @throws NullPointerException
	 *             if context is null.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (isResolvable(base)) {
			context.setPropertyResolved(true);
		}
		return readOnly;
	}

	/**
	 * If the base object is a map, attempts to set the value associated with
	 * the given key, as specified by the property argument. If the base is a
	 * Map, the propertyResolved property of the ELContext object must be set to
	 * true by this resolver, before returning. If this property is not true
	 * after this method is called, the caller can safely assume no value was
	 * set. If this resolver was constructed in read-only mode, this method will
	 * always throw PropertyNotWritableException. If a Map was created using
	 * java.util.Collections.unmodifiableMap(Map), this method must throw
	 * PropertyNotWritableException. Unfortunately, there is no Collections API
	 * method to detect this. However, an implementation can create a prototype
	 * unmodifiable Map and query its runtime type to see if it matches the
	 * runtime type of the base object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @param value
	 *            The value to be associated with the specified key.
	 * @throws ClassCastException
	 *             if the class of the specified key or value prevents it from
	 *             being stored in this map.
	 * @throws NullPointerException
	 *             if context is null, or if this map does not permit null keys
	 *             or values, and the specified key or value is null.
	 * @throws IllegalArgumentException
	 *             if some aspect of this key or value prevents it from being
	 *             stored in this map.
	 * @throws PropertyNotWritableException
	 *             if this resolver was constructed in read-only mode, or if the
	 *             put operation is not supported by the underlying map.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (base instanceof ObjectNode) {
			if (readOnly) {
				throw new PropertyNotWritableException("resolver is read-only");
			}
			ObjectNode node = (ObjectNode) base;
			if (value instanceof BigDecimal) {
				node.put(property.toString(), (BigDecimal) value);

			} else if (value instanceof Boolean) {
				node.put(property.toString(), (Boolean) value);

			} else if (value instanceof Long) {
				node.put(property.toString(), (Long) value);

			} else if (value instanceof Double) {
				node.put(property.toString(), (Double) value);

			} else if (value != null) {
				node.put(property.toString(), value.toString());

			} else {
				node.putNull(property.toString());
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
	 *            The name of the property to analyze. Will be coerced to a
	 *            String.
	 * @return base != null
	 */
	private final boolean isResolvable(Object base) {
		return base instanceof JsonNode;
	}
}
