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
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ELResolver;
import org.activiti.javax.el.PropertyNotWritableException;
import org.activiti.javax.el.ResourceBundleELResolver;
import org.activiti.javax.el.TestContext;

public class ResourceBundleELResolverTest extends TestCase {
	ELContext context = new TestContext();

	ResourceBundle sampleBundle() {
		return new ListResourceBundle() {
			@Override
			protected Object[][] getContents() {
				return new Object[][]{
						{"0", 1},
						{"1", 2},
						{"2", 3},
				};
			}
		};
	}
	
	public void testGetCommonPropertyType() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base is bundle --> String.class
		assertSame(String.class, resolver.getCommonPropertyType(context, bundle));

		// base is scalar --> null
		assertNull(resolver.getCommonPropertyType(context, scalar));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));
	}

	public void testGetFeatureDescriptors() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base is scalar or null --> null
		assertNull(resolver.getFeatureDescriptors(context, scalar));
		assertNull(resolver.getFeatureDescriptors(context, null));

		// base == null --> null
		assertNull(resolver.getCommonPropertyType(context, null));

		// base is bean --> features...
		Iterator<FeatureDescriptor> iterator = resolver.getFeatureDescriptors(context, bundle);
		List<String> names = new ArrayList<String>();
		while (iterator.hasNext()) {
			FeatureDescriptor feature = iterator.next();
			names.add(feature.getName());
			assertSame(String.class, feature.getValue(ELResolver.TYPE));
			assertSame(Boolean.TRUE, feature.getValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME));
		}
		assertTrue(names.contains("0"));
		assertTrue(names.contains("1"));
		assertTrue(names.contains("2"));
		assertEquals(3, names.size());		
	}

	public void testGetType() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bundle, any property --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, bundle, 1));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, bundle, null));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertNull(resolver.getType(context, bundle, "foo"));
		assertTrue(context.isPropertyResolved());
	}

	public void testGetValue() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bundle, property == 1 --> 2
		context.setPropertyResolved(false);
		assertEquals(2, resolver.getValue(context, bundle, 1));
		assertTrue(context.isPropertyResolved());

		// base is bundle, any non-null property which is not a key in the bundle --> '???' + property + '???'
		context.setPropertyResolved(false);
		assertEquals("???foo???", resolver.getValue(context, bundle, "foo"));
		assertTrue(context.isPropertyResolved());

		// base is bundle, property == null --> null
		context.setPropertyResolved(false);
		assertNull(resolver.getValue(context, bundle, null));
		assertTrue(context.isPropertyResolved());
	}

	public void testIsReadOnly() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base is null --> false
		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, null, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is scalar --> false
		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, scalar, "foo"));
		assertFalse(context.isPropertyResolved());

		// base is bundle, any property --> true
		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, bundle, 1));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, bundle, "foo"));
		assertTrue(context.isPropertyResolved());

		context.setPropertyResolved(false);
		assertTrue(resolver.isReadOnly(context, bundle, null));
		assertTrue(context.isPropertyResolved());
	}

	public void testSetValue() {
		Integer scalar = 0;
		ResourceBundle bundle = sampleBundle();
		ResourceBundleELResolver resolver = new ResourceBundleELResolver();

		// base == null --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, null, "foo", -1);
		assertFalse(context.isPropertyResolved());

		// base is scalar --> unresolved
		context.setPropertyResolved(false);
		resolver.setValue(context, scalar, "foo", -1);
		assertFalse(context.isPropertyResolved());

		try {
			resolver.setValue(context, bundle, "1", 999);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
		try {
			resolver.setValue(context, bundle, "1", null);
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
		try {
			resolver.setValue(context, bundle, "1", "foo");
			fail();
		} catch (PropertyNotWritableException e) {
			// fine
		}
	}
}
