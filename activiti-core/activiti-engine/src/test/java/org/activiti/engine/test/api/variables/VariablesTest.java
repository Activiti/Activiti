package org.activiti.engine.test.api.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Testing various constructs with variables. Created to test the changes done in https://jira.codehaus.org/browse/ACT-1900.
 * 

 */
public class VariablesTest extends PluggableActivitiTestCase {

  protected String processInstanceId;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/variables/VariablesTest.bpmn20.xml").deploy();

    // Creating 50 vars in total
    Map<String, Object> vars = generateVariables();
    processInstanceId = runtimeService.startProcessInstanceByKey("variablesTest", vars).getId();
  }

  private Map<String, Object> generateVariables() {
    Map<String, Object> vars = new HashMap<String, Object>();

    // 10 Strings
    for (int i = 0; i < 10; i++) {
      vars.put("stringVar" + i, "stringVarValue-" + i);
    }

    // 10 integers
    for (int i = 0; i < 10; i++) {
      vars.put("intVar" + i, i * 100);
    }

    // 10 dates
    for (int i = 0; i < 10; i++) {
      vars.put("dateVar" + i, new Date());
    }
    
    // 10 joda local dates
    for (int i = 0; i < 10; i++) {
      vars.put("localdateVar" + i, new LocalDate());
    }
    
    // 10 joda local dates
    for (int i = 0; i < 10; i++) {
      vars.put("datetimeVar" + i, new DateTime());
    }

    // 10 booleans
    for (int i = 0; i < 10; i++) {
      vars.put("booleanValue" + i, (i % 2 == 0));
    }

    // 10 Serializables
    for (int i = 0; i < 10; i++) {
      vars.put("serializableValue" + i, new TestSerializableVariable(i));
    }
    return vars;
  }

  @Override
  protected void tearDown() throws Exception {

    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    super.tearDown();
  }

  public void testGetVariables() {

    // Regular getVariables after process instance start
    Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
    assertEquals(70, vars.size());
    int nrOfStrings = 0, nrOfInts = 0, nrOfDates = 0, nrOfLocalDates = 0, nrOfDateTimes = 0, nrOfBooleans = 0, nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(10, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertFalse(processInstanceId.equals(executionId));

    vars = runtimeService.getVariables(processInstanceId);
    assertEquals(70, vars.size());
    nrOfStrings = 0;
    nrOfInts = 0;
    nrOfDates = 0;
    nrOfLocalDates = 0;
    nrOfDateTimes = 0;
    nrOfBooleans = 0;
    nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(10, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);
  }

  public void testGetVariablesLocal() {

    // Regular getVariables after process instance start
    Map<String, Object> vars = runtimeService.getVariablesLocal(processInstanceId);
    assertEquals(70, vars.size());
    int nrOfStrings = 0, nrOfInts = 0, nrOfDates = 0, nrOfLocalDates = 0, nrOfDateTimes = 0, nrOfBooleans = 0, nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(10, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertFalse(processInstanceId.equals(executionId));

    // On the local scope level, the vars shouldn't be visible
    vars = runtimeService.getVariablesLocal(executionId);
    assertEquals(0, vars.size());
  }

  public void testGetVariable() {

    // This actually does a specific select. Before, this was not the case
    // (all variables were fetched)
    // See the logging to verify this

    String value = (String) runtimeService.getVariable(processInstanceId, "stringVar3");
    assertEquals("stringVarValue-3", value);
  }

  public void testGetVariablesLocal2() {

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertFalse(processInstanceId.equals(executionId));

    runtimeService.setVariableLocal(executionId, "stringVar1", "hello");
    runtimeService.setVariableLocal(executionId, "stringVar2", "world");
    runtimeService.setVariableLocal(executionId, "myVar", "test123");

    Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
    assertEquals(70, vars.size());
    int nrOfStrings = 0;
    int nrOfInts = 0;
    int nrOfDates = 0;
    int nrOfLocalDates = 0;
    int nrOfDateTimes = 0;
    int nrOfBooleans = 0;
    int nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(10, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);

    assertEquals("stringVarValue-1", vars.get("stringVar1"));
    assertEquals("stringVarValue-2", vars.get("stringVar2"));
    assertNull(vars.get("myVar"));

    // Execution local

    vars = runtimeService.getVariables(executionId);

    nrOfStrings = 0;
    nrOfInts = 0;
    nrOfDates = 0;
    nrOfLocalDates = 0;
    nrOfDateTimes = 0;
    nrOfBooleans = 0;
    nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(11, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);

    assertEquals("hello", vars.get("stringVar1"));
    assertEquals("world", vars.get("stringVar2"));
    assertEquals("test123", vars.get("myVar"));

  }

  public void testGetVariablesWithCollectionThroughRuntimeService() {

    Map<String, Object> vars = runtimeService.getVariables(processInstanceId, Arrays.asList("intVar1", "intVar3", "intVar5", "intVar9"));
    assertEquals(4, vars.size());
    assertEquals(100, vars.get("intVar1"));
    assertEquals(300, vars.get("intVar3"));
    assertEquals(500, vars.get("intVar5"));
    assertEquals(900, vars.get("intVar9"));

    assertEquals(4, runtimeService.getVariablesLocal(processInstanceId, Arrays.asList("intVar1", "intVar3", "intVar5", "intVar9")).size());

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertFalse(processInstanceId.equals(executionId));

    assertEquals(0, runtimeService.getVariablesLocal(executionId, Arrays.asList("intVar1", "intVar3", "intVar5", "intVar9")).size());

  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableAllVariableFetchingDefault() {

    // Testing it the default way, all using getVariable("someVar");

    Map<String, Object> vars = generateVariables();
    vars.put("testVar", "hello");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    vars = runtimeService.getVariables(processInstanceId);
    assertEquals(71, vars.size());

    String varValue = (String) runtimeService.getVariable(processInstanceId, "testVar");
    assertEquals("HELLO world", varValue);
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableAllVariableFetchingDisabled() {

    Map<String, Object> vars = generateVariables();
    vars.put("testVar", "hello");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    String varValue = (String) runtimeService.getVariable(processInstanceId, "testVar");
    assertEquals("HELLO world!", varValue);
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableInDelegateMixed() {

    Map<String, Object> vars = generateVariables();
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    assertEquals("test 1 2 3", (String) runtimeService.getVariable(processInstanceId, "testVar"));
    assertEquals("Hiya", (String) runtimeService.getVariable(processInstanceId, "testVar2"));
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableInDelegateMixed2() {

    Map<String, Object> vars = generateVariables();
    vars.put("testVar", "1");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    assertEquals("1234", (String) runtimeService.getVariable(processInstanceId, "testVar"));
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableInDelegateMixed3() {

    Map<String, Object> vars = generateVariables();
    vars.put("testVar1", "one");
    vars.put("testVar2", "two");
    vars.put("testVar3", "three");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers
                                                                                                   // service
                                                                                                   // task
                                                                                                   // invocation

    assertEquals("one-CHANGED", (String) runtimeService.getVariable(processInstanceId, "testVar1"));
    assertEquals("two-CHANGED", (String) runtimeService.getVariable(processInstanceId, "testVar2"));
    assertNull(runtimeService.getVariable(processInstanceId, "testVar3"));
  }

  public void testTaskGetVariables() {

    Task task = taskService.createTaskQuery().taskName("Task 1").singleResult();
    Map<String, Object> vars = taskService.getVariables(task.getId());
    assertEquals(70, vars.size());
    int nrOfStrings = 0, nrOfInts = 0, nrOfDates = 0, nrOfLocalDates = 0, nrOfDateTimes = 0, nrOfBooleans = 0, nrOfSerializable = 0;
    for (String variableName : vars.keySet()) {
      Object variableValue = vars.get(variableName);
      if (variableValue instanceof String) {
        nrOfStrings++;
      } else if (variableValue instanceof Integer) {
        nrOfInts++;
      } else if (variableValue instanceof Boolean) {
        nrOfBooleans++;
      } else if (variableValue instanceof Date) {
        nrOfDates++;
      } else if (variableValue instanceof LocalDate) {
        nrOfLocalDates++;
      } else if (variableValue instanceof DateTime) {
        nrOfDateTimes++;
      } else if (variableValue instanceof TestSerializableVariable) {
        nrOfSerializable++;
      }
    }

    assertEquals(10, nrOfStrings);
    assertEquals(10, nrOfBooleans);
    assertEquals(10, nrOfDates);
    assertEquals(10, nrOfLocalDates);
    assertEquals(10, nrOfDateTimes);
    assertEquals(10, nrOfInts);
    assertEquals(10, nrOfSerializable);

    // Get variables local
    assertEquals(0, taskService.getVariablesLocal(task.getId()).size());

    // Get collection of variables
    assertEquals(2, taskService.getVariables(task.getId(), Arrays.asList("intVar2", "intVar5")).size());
    assertEquals(0, taskService.getVariablesLocal(task.getId(), Arrays.asList("intVar2", "intVar5")).size());

    // Get Variable
    assertEquals("stringVarValue-3", taskService.getVariable(task.getId(), "stringVar3"));
    assertNull(taskService.getVariable(task.getId(), "stringVarDoesNotExist"));
    assertNull(taskService.getVariableLocal(task.getId(), "stringVar3"));

    // Set local variable
    taskService.setVariableLocal(task.getId(), "localTaskVar", "localTaskVarValue");
    assertEquals(71, taskService.getVariables(task.getId()).size());
    assertEquals(1, taskService.getVariablesLocal(task.getId()).size());
    assertEquals(2, taskService.getVariables(task.getId(), Arrays.asList("intVar2", "intVar5")).size());
    assertEquals(0, taskService.getVariablesLocal(task.getId(), Arrays.asList("intVar2", "intVar5")).size());
    assertEquals("localTaskVarValue", taskService.getVariable(task.getId(), "localTaskVar"));
    assertEquals("localTaskVarValue", taskService.getVariableLocal(task.getId(), "localTaskVar"));

    // Override process variable
    Collection<String> varNames = new ArrayList<String>();
    varNames.add("stringVar1");
    assertEquals("stringVarValue-1", taskService.getVariable(task.getId(), "stringVar1"));
    assertEquals("stringVarValue-1", taskService.getVariable(task.getId(), "stringVar1"));
    assertEquals("stringVarValue-1", taskService.getVariables(task.getId(), varNames).get("stringVar1"));
    taskService.setVariableLocal(task.getId(), "stringVar1", "Override");
    assertEquals(71, taskService.getVariables(task.getId()).size());
    assertEquals("Override", taskService.getVariable(task.getId(), "stringVar1"));
    assertEquals("Override", taskService.getVariables(task.getId(), varNames).get("stringVar1"));
  }
  
  public void testLocalDateVariable() {

    Calendar todayCal = new GregorianCalendar();
    int todayYear = todayCal.get(Calendar.YEAR);
    int todayMonth = todayCal.get(Calendar.MONTH);
    int todayDate = todayCal.get(Calendar.DAY_OF_MONTH);
    
    // Regular getVariables after process instance start
    LocalDate date1 = (LocalDate) runtimeService.getVariable(processInstanceId, "localdateVar1");
    assertEquals(todayYear, date1.getYear());
    assertEquals(todayMonth + 1, date1.getMonthOfYear());
    assertEquals(todayDate, date1.getDayOfMonth());
    
    date1 = new LocalDate(2010, 11, 10);
    runtimeService.setVariable(processInstanceId, "localdateVar1", date1);
    date1 = (LocalDate) runtimeService.getVariable(processInstanceId, "localdateVar1");
    assertEquals(2010, date1.getYear());
    assertEquals(11, date1.getMonthOfYear());
    assertEquals(10, date1.getDayOfMonth());
    
    LocalDate queryDate = new LocalDate(2010, 11, 9);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("localdateVar1", queryDate).singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstanceId, processInstance.getId());
    
    queryDate = new LocalDate(2010, 11, 10);
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("localdateVar1", queryDate).singleResult();
    assertNull(processInstance);
    
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("localdateVar1", queryDate).singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstanceId, processInstance.getId());
  }
  
  public void testLocalDateTimeVariable() {

    Calendar todayCal = new GregorianCalendar();
    int todayYear = todayCal.get(Calendar.YEAR);
    int todayMonth = todayCal.get(Calendar.MONTH);
    int todayDate = todayCal.get(Calendar.DAY_OF_MONTH);
    
    // Regular getVariables after process instance start
    DateTime date1 = (DateTime) runtimeService.getVariable(processInstanceId, "datetimeVar1");
    assertEquals(todayYear, date1.getYear());
    assertEquals(todayMonth + 1, date1.getMonthOfYear());
    assertEquals(todayDate, date1.getDayOfMonth());
    
    date1 = new DateTime(2010, 11, 10, 10, 15);
    runtimeService.setVariable(processInstanceId, "datetimeVar1", date1);
    date1 = (DateTime) runtimeService.getVariable(processInstanceId, "datetimeVar1");
    assertEquals(2010, date1.getYear());
    assertEquals(11, date1.getMonthOfYear());
    assertEquals(10, date1.getDayOfMonth());
    assertEquals(10, date1.getHourOfDay());
    assertEquals(15, date1.getMinuteOfHour());
    
    DateTime queryDate = new DateTime(2010, 11, 10, 9, 15);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("datetimeVar1", queryDate).singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstanceId, processInstance.getId());
    
    queryDate = new DateTime(2010, 11, 10, 10, 15);
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("datetimeVar1", queryDate).singleResult();
    assertNull(processInstance);
    
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("datetimeVar1", queryDate).singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstanceId, processInstance.getId());
  }

  // Class to test variable serialization
  public static class TestSerializableVariable implements Serializable {

    private static final long serialVersionUID = 1L;
    private int number;

    public TestSerializableVariable(int number) {
      this.number = number;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

  }

  // Test delegates
  public static class TestJavaDelegate1 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar");
      execution.setVariable("testVar", var.toUpperCase());
    }
  }

  public static class TestJavaDelegate2 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar");
      execution.setVariable("testVar", var + " world");
    }
  }

  public static class TestJavaDelegate3 implements JavaDelegate {
    public void execute(DelegateExecution execution) {

    }
  }

  // ////////////////////////////////////////

  public static class TestJavaDelegate4 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", var.toUpperCase());
    }
  }

  public static class TestJavaDelegate5 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", var + " world");
    }
  }

  public static class TestJavaDelegate6 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", var + "!");
    }
  }

  // ////////////////////////////////////////

  public static class TestJavaDelegate7 implements JavaDelegate {
    public void execute(DelegateExecution execution) {

      // Setting variable through 'default' way of setting variable
      execution.setVariable("testVar", "test");

    }
  }

  public static class TestJavaDelegate8 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String var = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", var + " 1 2 3");
    }
  }

  public static class TestJavaDelegate9 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      execution.setVariable("testVar2", "Hiya");
    }
  }

  // ////////////////////////////////////////

  public static class TestJavaDelegate10 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String testVar = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", testVar + "2");
    }
  }

  public static class TestJavaDelegate11 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String testVar = (String) execution.getVariable("testVar", false);
      execution.setVariable("testVar", testVar + "3");
    }
  }

  public static class TestJavaDelegate12 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String testVar = (String) execution.getVariable("testVar");
      execution.setVariable("testVar", testVar + "4");
    }
  }

  // ////////////////////////////////////////

  public static class TestJavaDelegate13 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      Map<String, Object> vars = execution.getVariables(Arrays.asList("testVar1", "testVar2", "testVar3"), false);

      String testVar1 = (String) vars.get("testVar1");
      String testVar2 = (String) vars.get("testVar2");
      String testVar3 = (String) vars.get("testVar3");

      execution.setVariable("testVar1", testVar1 + "-CHANGED", false);
      execution.setVariable("testVar2", testVar2 + "-CHANGED", false);
      execution.setVariable("testVar3", testVar3 + "-CHANGED", false);

      execution.setVariableLocal("localVar", "localValue", false);
    }
  }

  public static class TestJavaDelegate14 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      String value = (String) execution.getVariable("testVar2");
      String localVarValue = (String) execution.getVariableLocal("localValue");
      execution.setVariableLocal("testVar2", value + localVarValue);
    }
  }

  public static class TestJavaDelegate15 implements JavaDelegate {
    public void execute(DelegateExecution execution) {
      execution.removeVariable("testVar3");
    }
  }

}
