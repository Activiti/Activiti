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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.activiti.javax.el.BeanELResolver;
import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ELException;
import org.activiti.javax.el.ELResolver;
import org.activiti.javax.el.ExpressionFactory;
import org.activiti.javax.el.MethodNotFoundException;
import org.activiti.javax.el.PropertyNotFoundException;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.TestContext;

public class BeanELResolverTest extends TestCase {
	public static class TestBean {
		int readOnly = 123;
		int readWrite = 456;
		int writeOnly = 789;
		public int getReadOnly() {
			return readOnly;
		}
		protected void setReadOnly(int readOnly) {
			this.readOnly = readOnly;
		}
		public int getReadWrite() {
			return readWrite;
		}
		public void setReadWrite(int readWrite) {
			this.readWrite = readWrite;
		}
		int getWriteOnly() {
			return writeOnly;
		}
		public void setWriteOnly(int writeOnly) {
			this.writeOnly = writeOnly;
		}
		public int add(int n, int... rest) {
			for (int x : rest) {
				n += x;
			}
			return n;
		}
		public String cat(String... strings) {
			StringBuilder b = new StringBuilder();
			for (String s : strings) {
				b.append(s);
			}
			return b.toString();
		}
		int secret() {
			return 42;
		}
	}

	ELContext context = new TestContext();

	public void testGetCommonPropertyType() {
		BeanELResolver resolver = new BeanELResolver();

		// base is bean --> Object.class
		assertSame(Object.class, resolver.getCommonPropertyType(context, new TestBean()));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));
	}

	public void testGetFeatureDescriptors() {
		BeanELResolver resolver = new BeanELResolver();

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));

		// base is bean --> features...
		Iterator<FeatureDescriptor> iterator = resolver.getFeatureDescriptors(context, new TestBean());
		List<String> names = new ArrayList<String>();
		while (iterator.hasNext()) {
			FeatureDescriptor feature = iterator.next();
			names.add(feature.getName());
			Class<?> type = "class".equals(feature.getName()) ? Class.class : int.class;
			assertSame(type, feature.getValue(ELResolver.TYPE));
			assertSame(Boolean.TRUE, feature.getValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME));
		}
		assertTrue(names.contains("class"));
		assertTrue(names.contains("readOnly"));
		assertTrue(names.contains("readWrite"));
		assertTrue(names.contains("writeOnly"));
		assertEquals(4, names.size());		
	}

	public void testGetType() {
		BeanELResolver resolver = new BeanELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bean, property == "readWrite" --> int.class
		context.setPropertyResolved(false);
		assertSame(int.class, resolver.getType(context, new TestBean(), "readWrite"));
		assertTrue(context.isPropertyResolved());

		// base is bean, property == null --> exception
		try {
			resolver.getType(context, new TestBean(), null);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}

		// base is bean, property != null, but doesn't exist --> exception
		try {
			resolver.getType(context, new TestBean(), "doesntExist");
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testGetValue() {
		Properties properties = new Properties();
		properties.setProperty(ExpressionFactory.class.getName(), TestFactory.class.getName());
		BeanELResolver resolver = new BeanELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bean, property == "readWrite" --> 123
		context.setPropertyResolved(false);
		assertEquals(456, resolver.getValue(context, new TestBean(), "readWrite"));
		assertTrue(context.isPropertyResolved());

		// base is bean, property == "writeOnly" --> exception
		try {
			resolver.getValue(context, new TestBean(), "writeOnly");
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}

		// base is bean, property != null, but doesn't exist --> exception
		try {
			resolver.getValue(context, new TestBean(), "doesntExist");
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testIsReadOnly() {
		BeanELResolver resolver = new BeanELResolver();
		BeanELResolver resolverReadOnly = new BeanELResolver(true);

		// base == null --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bean, property == "readOnly" --> true
		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, new TestBean(), "readOnly"));
		assertTrue(context.isPropertyResolved());

		// base is bean, property == "readWrite" --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, new TestBean(), "readWrite"));
		assertTrue(context.isPropertyResolved());

		// base is bean, property == "writeOnly" --> false
		context.setPropertyResolved(false);
		assertFalse(resolver.isReadOnly(context, new TestBean(), "writeOnly"));
		assertTrue(context.isPropertyResolved());

		// base is bean, property == 1 --> true (use read-only resolver)
		context.setPropertyResolved(false);
		assertTrue(resolverReadOnly.isReadOnly(context, new TestBean(), "readWrite"));
		assertTrue(context.isPropertyResolved());

		// is bean, property != null, but doesn't exist --> exception
		try {
			resolver.isReadOnly(context, new TestBean(), "doesntExist");
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}
	}

	public void testSetValue() {
		BeanELResolver resolver = new BeanELResolver();
		BeanELResolver resolverReadOnly = new BeanELResolver(true);

		// base == null --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, null, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is bean, property == "readWrite" --> ok
		context.setPropertyResolved(false);
		TestBean bean = new TestBean();
		resolver.setValue(context, bean, "readWrite", 999);
		assertEquals(999, bean.getReadWrite());
		assertTrue(context.isPropertyResolved());

		// base is bean, property == "readOnly" --> exception
		try {
			resolver.setValue(context, new TestBean(), "readOnly", 1);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}

		// base is bean, property != null, but doesn't exist --> exception
		try {
			resolver.setValue(context, new TestBean(), "doesntExist", 1);
			fail();
		} catch (PropertyNotFoundException e) {
			// fine
		}

		// base is bean, property == "readWrite", invalid value --> exception
		try {
			resolver.setValue(context, new TestBean(), "readWrite", "invalid");
			fail();
		} catch (ELException e) {
			// fine, according to the spec...
		} catch (IllegalArgumentException e) {
			// violates the spec, but we'll accept this...
		}

		// read-only resolver
		try {
			resolverReadOnly.setValue(context, bean, "readWrite", 999);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
	}

	public void testInvoke() {
		BeanELResolver resolver = new BeanELResolver();
		
		assertEquals(1, resolver.invoke(context, new TestBean(), "add", null, new Integer[]{1}));
		assertEquals(6, resolver.invoke(context, new TestBean(), "add", null, new Integer[]{1, 2, 3}));
		assertEquals(6, resolver.invoke(context, new TestBean(), "add", null, new String[]{"1", "2", "3"}));
		assertEquals(6, resolver.invoke(context, new TestBean(), "add", null, new Object[]{1, new int[]{2, 3}}));
		assertEquals(6, resolver.invoke(context, new TestBean(), "add", null, new Object[]{1, new Double[]{2.0, 3.0}}));

		assertEquals("", resolver.invoke(context, new TestBean(), "cat", null, new Object[0]));
		assertEquals("", resolver.invoke(context, new TestBean(), "cat", null, null));
		assertEquals("123", resolver.invoke(context, new TestBean(), "cat", null, new Object[]{123}));
		assertEquals("123", resolver.invoke(context, new TestBean(), "cat", null, new Integer[]{1, 2, 3}));
		assertEquals("123", resolver.invoke(context, new TestBean(), "cat", null, new Object[]{new String[]{"1", "2", "3"}}));
	
		TestBean bean = new TestBean();
		bean.setReadWrite(1);
		assertNull(resolver.invoke(context, bean, "setReadWrite", null, new Object[]{null}));
		assertEquals(0, bean.getReadWrite());
		assertNull(resolver.invoke(context, bean, "setReadWrite", null, new Object[]{5}));
		assertEquals(5, bean.getReadWrite());
		try {
			resolver.invoke(context, new TestBean(), "secret", null, null);
			fail();
		} catch (MethodNotFoundException e) {
			// fine
		}
	}
}
