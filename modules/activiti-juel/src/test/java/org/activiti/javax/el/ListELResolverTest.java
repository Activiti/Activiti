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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ListELResolver;
import org.activiti.javax.el.PropertyNotFoundException;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.TestContext;

public class ListELResolverTest extends TestCase {
	ELContext context = new TestContext();

	public void testGetCommonPropertyType() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();

		// base is array --> int.class
		assertSame(Integer.class, resolver.getCommonPropertyType(context, list));

		// base is scalar --> null
		assertNull(resolver.getCommonPropertyType(context, scalar));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));
	}

	public void testGetFeatureDescriptors() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();

		// any --> null
		assertNull(resolver.getFeatureDescriptors(context, scalar));
		assertNull(resolver.getFeatureDescriptors(context, list));
		assertNull(resolver.getFeatureDescriptors(context, null));
	}

	public void testGetType() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is array, property == 1 --> Object.class
		context.setPropertyResolved(false);
		assertSame(Object.class, resolver.getType(context, list, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.getType(context, list, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getType(context, list, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getType(context, list, -1);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.getType(context, list, list.size());
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testGetValue() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is array, property == 1 --> 2
		context.setPropertyResolved(false);
		assertEquals(2, resolver.getValue(context, list, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.getValue(context, list, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getValue(context, list, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		assertNull(resolver.getValue(context, list, -1));
		assertNull(resolver.getValue(context, list, list.size()));
	}

	public void testIsReadOnly() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();
		ListELResolver resolverReadOnly = new ListELResolver(true);

		// base is null --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is array, property == 1 --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, list, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, property == 1 --> true (use read-only resolver)
		context.setPropertyResolved(false);
		assertTrue(resolverReadOnly.isReadOnly(context, list, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.isReadOnly(context, list, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, list, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, list, -1);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, list, list.size());
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testSetValue() {
		Integer scalar = 0;
		List<Integer> list = Arrays.asList(1, 2, 3);
		ListELResolver resolver = new ListELResolver();
		ListELResolver resolverReadOnly = new ListELResolver(true);

		// base == null --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, null, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is scalar --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, scalar, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is array, property == 1 --> ok
		context.setPropertyResolved(false);
		resolver.setValue(context, list, 1, 999);
		assertEquals(999, list.get(1).intValue());
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.setValue(context, list, null, 999);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.setValue(context, list, "foo", 999);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.setValue(context, list, -1, 999);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.setValue(context, list, list.size(), 999);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}

		// base is array, property == 1, value == null --> ok
		context.setPropertyResolved(false);
		resolver.setValue(context, list, 1, null);
		assertNull(list.get(1));
		assertTrue(context.isPropertyResolved());

		// base is array, property == 1, bad value --> exception
		try {
			resolver.setValue(context, list, 1, "foo");
			fail();
		} catch (ClassCastException e) {
			// fine, according to the spec...
		} catch (IllegalArgumentException e) {
			// violates the spec, but we'll accept this...
		}
		
		// read-only resolver
		try {
			resolverReadOnly.setValue(context, list, 1, 999);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
	}
}
