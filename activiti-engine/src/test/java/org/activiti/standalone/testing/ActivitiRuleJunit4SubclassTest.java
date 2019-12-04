package org.activiti.standalone.testing;

/**
 * This test is explicitly empty.
 * 
 * The purpose of this test is to make sure that ActivitiRule works with tests methods that are defined in a parent class.
 * 
 * When running the test suite over this class, JUnit will execute the test from {@link ActivitiRuleJunit4Test}. This means that the code in {@link org.activiti.engine.impl.test.TestHelper} needs to
 * be able to find the test method even when it's not declared on the test class itself.
 * 
 * Specifically, {@link org.activiti.engine.impl.test.TestHelper} needs to call getMethod() rather than getDeclaredMethod() since the method is declared in a parent of the actual test class.
 */
public class ActivitiRuleJunit4SubclassTest extends ActivitiRuleJunit4Test {

}
