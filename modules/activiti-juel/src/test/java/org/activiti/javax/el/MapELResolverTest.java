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
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ELResolver;
import org.activiti.javax.el.MapELResolver;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.TestContext;

public class MapELResolverTest extends TestCase {
	ELContext context = new TestContext();

	Map<Integer,Integer> sampleMap() {
		Map<Integer,Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < 3; i++) {
			map.put(i, i+1);
		}
		return map;
	}
	
	public void testGetCommonPropertyType() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();

		// base is map --> Object.class
		assertSame(Object.class, resolver.getCommonPropertyType(context, map));

		// base is scalar --> null
		assertNull(resolver.getCommonPropertyType(context, scalar));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));
	}

	public void testGetFeatureDescriptors() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();

		// base is scalar or null --> null
		assertNull(resolver.getFeatureDescriptors(context, scalar));
		assertNull(resolver.getFeatureDescriptors(context, null));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));

		// base is map --> features...
		Iterator<FeatureDescriptor> iterator = resolver.getFeatureDescriptors(context, map);
		List<String> names = new ArrayList<String>();
		while (iterator.hasNext()) {
			FeatureDescriptor feature = iterator.next();
			names.add(feature.getName());
			assertSame(Integer.class, feature.getValue(ELResolver.TYPE));
			assertSame(Boolean.TRUE, feature.getValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME));
		}
		assertTrue(names.contains("0"));
		assertTrue(names.contains("1"));
		assertTrue(names.contains("2"));
		assertEquals(3, names.size());		
	}

	public void testGetType() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is map, any property --> Object.class
		context.setPropertyResolved(false);
		assertSame(Object.class, resolver.getType(context, map, 1));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertSame(Object.class, resolver.getType(context, map, null));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertSame(Object.class, resolver.getType(context, map, "foo"));
		assertTrue(context.isPropertyResolved());
	}

	public void testGetValue() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is map, property == 1 --> 2
		context.setPropertyResolved(false);
		assertEquals(2, resolver.getValue(context, map, 1));
		assertTrue(context.isPropertyResolved());

		// base is map, any property which is not a key in the map --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, map, "foo"));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, map, null));
		assertTrue(context.isPropertyResolved());
	}

	public void testIsReadOnly() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();
		MapELResolver resolverReadOnly = new MapELResolver(true);

		// base is null --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is map, property == 1 --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, map, 1));
		assertTrue(context.isPropertyResolved());

		// base is map, property == 1 --> true (use read-only resolver)
		context.setPropertyResolved(false);
		assertTrue(resolverReadOnly.isReadOnly(context, map, 1));
		assertTrue(context.isPropertyResolved());

		// base is map, any property which is not a key in the map --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, map, "foo"));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, map, null));
		assertTrue(context.isPropertyResolved());
	}

	public void testSetValue() {
		Integer scalar = 0;
		Map<Integer,Integer> map = sampleMap();
		MapELResolver resolver = new MapELResolver();
		MapELResolver resolverReadOnly = new MapELResolver(true);

		// base == null --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, null, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is scalar --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, scalar, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is map, property == 1 --> ok
		context.setPropertyResolved(false);
		resolver.setValue(context, map, 1, 999);
		assertEquals(999, map.get(1).intValue());
		assertTrue(context.isPropertyResolved());

		// base is map, any property which is not a key in the map --> false
		context.setPropertyResolved(false);
		resolver.setValue(context, map, 999, "foo");
		assertEquals(map.get(999), "foo");
		assertTrue(context.isPropertyResolved());

		// base is map, property == 1, value == null --> ok
		context.setPropertyResolved(false);
		resolver.setValue(context, map, 1, null);
		assertNull(map.get(1));
		assertTrue(context.isPropertyResolved());
		
		// read-only resolver
		try {
			resolverReadOnly.setValue(context, map, 1, 999);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
	}
}
