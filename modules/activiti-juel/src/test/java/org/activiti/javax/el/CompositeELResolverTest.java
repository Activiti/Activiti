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
package org.activiti.javax.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.activiti.javax.el.ArrayELResolver;
import org.activiti.javax.el.BeanELResolver;
import org.activiti.javax.el.CompositeELResolver;
import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ListELResolver;
import org.activiti.javax.el.MapELResolver;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.TestContext;

public class CompositeELResolverTest extends TestCase {
	ELContext context = new TestContext();

	Map<Integer,Integer> sampleMap() {
		Map<Integer,Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < 3; i++) {
			map.put(i, i+1);
		}
		return map;
	}

	public void testGetCommonPropertyType() {
		CompositeELResolver resolver = new CompositeELResolver();
		assertNull(resolver.getCommonPropertyType(context, null));
		assertNull(resolver.getCommonPropertyType(context, "foo"));
		
		resolver.add(new ArrayELResolver());
		assertNull(resolver.getCommonPropertyType(context, null));
		assertNull(resolver.getCommonPropertyType(context, "foo"));
		assertSame(Integer.class, resolver.getCommonPropertyType(context, new int[0]));

		resolver.add(new ListELResolver());
		assertNull(resolver.getCommonPropertyType(context, null));
		assertNull(resolver.getCommonPropertyType(context, "foo"));
		assertSame(Integer.class, resolver.getCommonPropertyType(context, new ArrayList<String>()));

		resolver.add(new MapELResolver());
		assertNull(resolver.getCommonPropertyType(context, null));
		assertNull(resolver.getCommonPropertyType(context, "foo"));
		assertSame(Object.class, resolver.getCommonPropertyType(context, new HashMap<String,String>()));
	}

	private int count(Iterator<FeatureDescriptor> iterator) {
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}
	
	public void testGetFeatureDescriptors() {
		CompositeELResolver resolver = new CompositeELResolver();
		assertFalse(resolver.getFeatureDescriptors(context, null).hasNext());
		assertFalse(resolver.getFeatureDescriptors(context, "foo").hasNext());
		assertFalse(resolver.getFeatureDescriptors(context, sampleMap()).hasNext());
		
		resolver.add(new MapELResolver());
		assertEquals(3, count(resolver.getFeatureDescriptors(context, sampleMap()))); // 0, 1, 2

		resolver.add(new ArrayELResolver());
		resolver.add(new BeanELResolver());
		resolver.add(new ListELResolver());
		assertEquals(5, count(resolver.getFeatureDescriptors(context, sampleMap()))); // 0, 1, 2, class, empty
	}

	public void testGetType() {
		CompositeELResolver resolver = new CompositeELResolver();
		assertNull(resolver.getType(context, null, "foo"));
		assertNull(resolver.getType(context, "foo", "class"));

		resolver.add(new MapELResolver());
		assertNull(resolver.getType(context, null, "foo"));
		assertNull(resolver.getType(context, "foo", "class"));
		assertEquals(Object.class, resolver.getType(context, sampleMap(), "foo"));
		assertEquals(Object.class, resolver.getType(context, sampleMap(), 0));		

		resolver.add(new BeanELResolver());
		assertNull(resolver.getType(context, null, "foo"));
		assertEquals(Class.class, resolver.getType(context, "foo", "class"));
		assertEquals(Object.class, resolver.getType(context, sampleMap(), "foo"));		
		assertEquals(Object.class, resolver.getType(context, sampleMap(), 0));		
	}

	public void testGetValue() {
		CompositeELResolver resolver = new CompositeELResolver();
		assertNull(resolver.getValue(context, null, "foo"));
		assertNull(resolver.getValue(context, "foo", "class"));

		resolver.add(new MapELResolver());
		assertNull(resolver.getValue(context, null, "foo"));
		assertNull(resolver.getValue(context, "foo", "class"));
		assertNull(resolver.getValue(context, sampleMap(), "foo"));
		assertEquals(1, resolver.getValue(context, sampleMap(), 0));

		resolver.add(new BeanELResolver());
		assertNull(resolver.getValue(context, null, "foo"));
		assertEquals(String.class, resolver.getValue(context, "foo", "class"));
		assertNull(resolver.getValue(context, sampleMap(), "foo"));
		assertEquals(1, resolver.getValue(context, sampleMap(), 0));
	}

	public void testIsReadOnly() {
		CompositeELResolver resolver = new CompositeELResolver();
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertFalse(resolver.isReadOnly(context, "foo", "class"));

		resolver.add(new MapELResolver());
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertFalse(resolver.isReadOnly(context, "foo", "class"));
		assertFalse(resolver.isReadOnly(context, sampleMap(), "foo"));
		assertFalse(resolver.isReadOnly(context, sampleMap(), 0));

		resolver.add(new BeanELResolver());
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertTrue(resolver.isReadOnly(context, "foo", "class"));
		assertFalse(resolver.isReadOnly(context, sampleMap(), "foo"));
		assertFalse(resolver.isReadOnly(context, sampleMap(), 0));
	}

	public void testSetValue() {
		CompositeELResolver resolver = new CompositeELResolver();
		resolver.setValue(context, null, "foo", "bar");
		resolver.setValue(context, "foo", "class", Integer.class);

		resolver.add(new MapELResolver());
		resolver.setValue(context, null, "foo", "bar");
		resolver.setValue(context, "foo", "class", Integer.class);
		Map<?,?> map = sampleMap();
		resolver.setValue(context, map, "foo", "bar");
		assertEquals("bar", map.get("foo"));
		resolver.setValue(context, map, 0, 999);
		assertEquals(999, map.get(0));

		resolver.add(new BeanELResolver());
		resolver.setValue(context, null, "foo", "bar");
		try {
			resolver.setValue(context, "foo", "class", Integer.class);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
		map = sampleMap();
		resolver.setValue(context, map, "foo", "bar");
		assertEquals("bar", map.get("foo"));
		resolver.setValue(context, map, 0, 999);
		assertEquals(999, map.get(0));
	}

}
