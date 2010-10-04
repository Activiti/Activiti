package org.activiti.javax.el;

import org.activiti.javax.el.ValueReference;

import junit.framework.TestCase;

public class ValueReferenceTest extends TestCase {

	public void testGetBase() {
		assertEquals("foo", new ValueReference("foo", "bar").getBase());
	}

	public void testGetProperty() {
		assertEquals("bar", new ValueReference("foo", "bar").getProperty());
	}

}
