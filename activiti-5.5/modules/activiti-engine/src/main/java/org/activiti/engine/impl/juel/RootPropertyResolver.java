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

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotWritableException;

/**
 * Simple root property resolver implementation. This resolver handles root properties (i.e.
 * <code>base == null &amp;&amp; property instanceof String</code>), which are stored in a map. The
 * properties can be accessed via the {@link #getProperty(String)},
 * {@link #setProperty(String, Object)}, {@link #isProperty(String)} and {@link #properties()}
 * methods.
 * 
 * @author Christoph Beck
 */
public class RootPropertyResolver extends ELResolver {
	private final Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
	private final boolean readOnly;

	/**
	 * Create a read/write root property resolver
	 */
	public RootPropertyResolver() {
		this(false);
	}

	/**
	 * Create a root property resolver
	 * 
	 * @param readOnly
	 */
	public RootPropertyResolver(boolean readOnly) {
		this.readOnly = readOnly;
	}

	private boolean isResolvable(Object base) {
		return base == null;
	}

	private boolean resolve(ELContext context, Object base, Object property) {
		context.setPropertyResolved(isResolvable(base) && property instanceof String);
		return context.isPropertyResolved();
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return isResolvable(context) ? String.class : null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return resolve(context, base, property) ? Object.class : null;
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (resolve(context, base, property)) {
			if (!isProperty((String) property)) {
				throw new PropertyNotFoundException("Cannot find property " + property);
			}
			return getProperty((String) property);
		}
		return null;
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return resolve(context, base, property) ? readOnly : false;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value)
			throws PropertyNotWritableException {
		if (resolve(context, base, property)) {
			if (readOnly) {
				throw new PropertyNotWritableException("Resolver is read only!");
			}
			setProperty((String) property, value);
		}
	}

	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		if (resolve(context, base, method)) {
			throw new NullPointerException("Cannot invoke method " + method + " on null");
		}
		return null;
	}

	/**
	 * Get property value
	 * 
	 * @param property
	 *            property name
	 * @return value associated with the given property
	 */
	public Object getProperty(String property) {
		return map.get(property);
	}

	/**
	 * Set property value
	 * 
	 * @param property
	 *            property name
	 * @param value
	 *            property value
	 */
	public void setProperty(String property, Object value) {
		map.put(property, value);
	}

	/**
	 * Test property
	 * 
	 * @param property
	 *            property name
	 * @return <code>true</code> if the given property is associated with a value
	 */
	public boolean isProperty(String property) {
		return map.containsKey(property);
	}

	/**
	 * Get properties
	 * 
	 * @return all property names (in no particular order)
	 */
	public Iterable<String> properties() {
		return map.keySet();
	}
}
