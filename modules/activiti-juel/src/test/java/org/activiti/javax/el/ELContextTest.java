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

import java.util.Locale;

import junit.framework.TestCase;


import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.TestContext;

public class ELContextTest extends TestCase {

	public void testContext() {
		ELContext context = new TestContext();
		assertNull(context.getContext(Integer.class));
		context.putContext(Integer.class, "foo");
		assertEquals("foo", context.getContext(Integer.class));
	}

	public void testLocale() {
		ELContext context = new TestContext();
		assertNull(context.getLocale());
		context.setLocale(Locale.ENGLISH);
		assertEquals(Locale.ENGLISH, context.getLocale());
	}

	public void testPropertyResolved() {
		ELContext context = new TestContext();
		assertFalse(context.isPropertyResolved());
		context.setPropertyResolved(true);
		assertTrue(context.isPropertyResolved());
	}
}
