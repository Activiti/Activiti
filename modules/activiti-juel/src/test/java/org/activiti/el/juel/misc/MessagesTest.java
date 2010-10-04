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
package org.activiti.el.juel.misc;

import org.activiti.el.juel.misc.LocalMessages;

import junit.framework.TestCase;

public class MessagesTest extends TestCase {

	/*
	 * Test method for 'de.odysseus.el.lang.Messages.get(String)'
	 */
	public void testGetString() {
		assertTrue(LocalMessages.get("foo").matches(".*foo"));
	}

	/*
	 * Test method for 'de.odysseus.el.lang.Messages.get(String, Object)'
	 */
	public void testGetStringObject() {
		assertTrue(LocalMessages.get("foo", "bar").matches(".*foo\\(bar\\)"));
	}

	/*
	 * Test method for 'de.odysseus.el.lang.Messages.get(String, Object, Object)'
	 */
	public void testGetStringObjectObject() {
		assertTrue(LocalMessages.get("foo", "bar", "baz").matches(".*foo\\(bar,\\s*baz\\)"));
	}
}
