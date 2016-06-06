package org.activiti.test.scripting.secure;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joram Barrez
 */
public class SecureScriptingTest extends SecureScriptingBaseTest {

  @Test
  public void testClassWhiteListing() {
    deployProcessDefinition("test-secure-script-class-white-listing.bpmn20.xml");

    try {
      runtimeService.startProcessInstanceByKey("secureScripting");
      Assert.fail(); // Expecting exception
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e.getMessage().contains("Cannot call property getRuntime in object"));
    }
  }

  @Test
  public void testInfiniteLoop() {
    deployProcessDefinition("test-secure-script-infinite-loop.bpmn20.xml");

    enableSysoutsInScript();
    addWhiteListedClass("java.lang.Thread"); // For the thread.sleep

    try {
      runtimeService.startProcessInstanceByKey("secureScripting");
      Assert.fail(); // Expecting exception
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.assertTrue(t.getMessage().contains("Maximum variableScope time of 3000 ms exceeded"));
    }
  }

  @Test
  public void testMaximumStackDepth() {
    deployProcessDefinition("test-secure-script-max-stack-depth.bpmn20.xml");

    try {
      runtimeService.startProcessInstanceByKey("secureScripting");
      Assert.fail(); // Expecting exception
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.assertTrue(t.getMessage().contains("Exceeded maximum stack depth"));
    }
  }

  @Test
  public void testMaxMemoryUsage() {
    deployProcessDefinition("test-secure-script-max-memory-usage.bpmn20.xml");

    try {
      runtimeService.startProcessInstanceByKey("secureScripting");
      Assert.fail(); // Expecting exception
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.assertTrue(t.getMessage().contains("Memory limit of 3145728 bytes reached"));
    }
  }

  @Test
  public void testUseExecutionAndVariables() {
    deployProcessDefinition("test-secure-script-use-variableScope-and-vars.bpmn20.xml");

    addWhiteListedClass("java.lang.Integer");
    addWhiteListedClass("org.activiti.engine.impl.persistence.entity.ExecutionEntity");

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("a", 123);
    vars.put("b", 456);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("useExecutionAndVars", vars);

    Object c = runtimeService.getVariable(processInstance.getId(), "c");
    Assert.assertTrue(c instanceof Number);
    Number cNumber = (Number) c;
    Assert.assertEquals(579, cNumber.intValue());
  }

  @Test
  public void testExecutionListener() {
    deployProcessDefinition("test-secure-script-execution-listener.bpmn20.xml");
    try {
      runtimeService.startProcessInstanceByKey("secureScripting");
      Assert.fail(); // Expecting exception
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e.getMessage().contains("Cannot call property getRuntime in object"));
    }
    Assert.assertEquals(0, taskService.createTaskQuery().count());
  }
  
  @Test
  public void testTaskListener() {
    deployProcessDefinition("test-secure-script-task-listener.bpmn20.xml");
    runtimeService.startProcessInstanceByKey("secureScripting");
    
    Task task = taskService.createTaskQuery().singleResult();
    Assert.assertNotNull(task);
    
    // Completing the task should fail cause the script is not secure
    try {
      taskService.complete(task.getId());
      Assert.fail(); // Expecting exception
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    task = taskService.createTaskQuery().singleResult();
    Assert.assertNotNull(task);
  }

}
