package org.activiti.core.el.juel.test;

public class TestClass {
	private static class TestInterfaceImpl implements TestInterface {
		public int fourtyTwo() {
			return 42;
		}
	}
	
	private TestInterface anonymousTestInterface = new TestInterface() {
		public int fourtyTwo() {
			return 42;
		}
	};

	public TestInterface getNestedTestInterface() {
		return new TestInterfaceImpl();
	}

	public TestInterface getAnonymousTestInterface() {
		return anonymousTestInterface;
	}
}
