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

import junit.framework.TestCase;

import org.activiti.javax.el.ArrayELResolver;
import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.PropertyNotFoundException;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.TestContext;

public class ArrayELResolverTest extends TestCase {
	ELContext context = new TestContext();

	public void testGetCommonPropertyType() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();

		// base is array --> int.class
		assertSame(Integer.class, resolver.getCommonPropertyType(context, array));

		// base is scalar --> null
		assertNull(resolver.getCommonPropertyType(context, scalar));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));
	}

	public void testGetFeatureDescriptors() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();

		// any --> null
		assertNull(resolver.getFeatureDescriptors(context, scalar));
		assertNull(resolver.getFeatureDescriptors(context, array));
		assertNull(resolver.getFeatureDescriptors(context, null));
	}

	public void testGetType() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is array, property == 1 --> int.class
		context.setPropertyResolved(false);
		assertSame(int.class, resolver.getType(context, array, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.getType(context, array, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getType(context, array, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getType(context, array, -1);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.getType(context, array, array.length);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testGetValue() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();

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
		assertEquals(2, resolver.getValue(context, array, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.getValue(context, array, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.getValue(context, array, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		assertNull(resolver.getValue(context, array, -1));
		assertNull(resolver.getValue(context, array, array.length));
	}

	public void testIsReadOnly() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();
		ArrayELResolver resolverReadOnly = new ArrayELResolver(true);

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
		assertFalse(resolver.isReadOnly(context, array, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, property == 1 --> true (use read-only resolver)
		context.setPropertyResolved(false);
		assertTrue(resolverReadOnly.isReadOnly(context, array, 1));
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.isReadOnly(context, array, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, array, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, array, -1);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.isReadOnly(context, array, array.length);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testSetValue() {
		int scalar = 0;
		int[] array = { 1, 2, 3 };
		ArrayELResolver resolver = new ArrayELResolver();
		ArrayELResolver resolverReadOnly = new ArrayELResolver(true);

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
		resolver.setValue(context, array, 1, 999);
		assertEquals(999, array[1]);
		assertTrue(context.isPropertyResolved());

		// base is array, bad property --> exception
		try {
			resolver.setValue(context, array, null, 999);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.setValue(context, array, "foo", 999);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.setValue(context, array, -1, 999);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
		try {
			resolver.setValue(context, array, array.length, 999);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}

		// base is array, property == 1, bad value --> exception
		try {
			resolver.setValue(context, array, 1, null);
			fail();
		} catch (IllegalArgumentException e) {
			// fine
		}
		try {
			resolver.setValue(context, array, 1, "foo");
			fail();
		} catch (ClassCastException e) {
			// fine, according to the spec...
		} catch (IllegalArgumentException e) {
			// violates the spec, but we'll accept this...
		}
		
		// read-only resolver
		try {
			resolverReadOnly.setValue(context, array, 1, 999);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
	}
}
