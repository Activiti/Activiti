package org.activiti.engine.test.api.variables;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.ArrayList;
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
    assertThat(vars).hasSize(70);
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

    assertThat(nrOfStrings).isEqualTo(10);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertThat(processInstanceId.equals(executionId)).isFalse();

    vars = runtimeService.getVariables(processInstanceId);
    assertThat(vars).hasSize(70);
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

    assertThat(nrOfStrings).isEqualTo(10);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);
  }

  public void testGetVariablesLocal() {

    // Regular getVariables after process instance start
    Map<String, Object> vars = runtimeService.getVariablesLocal(processInstanceId);
    assertThat(vars).hasSize(70);
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

    assertThat(nrOfStrings).isEqualTo(10);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertThat(processInstanceId.equals(executionId)).isFalse();

    // On the local scope level, the vars shouldn't be visible
    vars = runtimeService.getVariablesLocal(executionId);
    assertThat(vars).hasSize(0);
  }

  public void testGetVariable() {

    // This actually does a specific select. Before, this was not the case
    // (all variables were fetched)
    // See the logging to verify this

    String value = (String) runtimeService.getVariable(processInstanceId, "stringVar3");
    assertThat(value).isEqualTo("stringVarValue-3");
  }

  public void testGetVariablesLocal2() {

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertThat(processInstanceId.equals(executionId)).isFalse();

    runtimeService.setVariableLocal(executionId, "stringVar1", "hello");
    runtimeService.setVariableLocal(executionId, "stringVar2", "world");
    runtimeService.setVariableLocal(executionId, "myVar", "test123");

    Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
    assertThat(vars).hasSize(70);
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

    assertThat(nrOfStrings).isEqualTo(10);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);

    assertThat(vars.get("stringVar1")).isEqualTo("stringVarValue-1");
    assertThat(vars.get("stringVar2")).isEqualTo("stringVarValue-2");
    assertThat(vars.get("myVar")).isNull();

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

    assertThat(nrOfStrings).isEqualTo(11);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);

    assertThat(vars.get("stringVar1")).isEqualTo("hello");
    assertThat(vars.get("stringVar2")).isEqualTo("world");
    assertThat(vars.get("myVar")).isEqualTo("test123");

  }

  public void testGetVariablesWithCollectionThroughRuntimeService() {

    Map<String, Object> vars = runtimeService.getVariables(processInstanceId, asList("intVar1", "intVar3", "intVar5", "intVar9"));
    assertThat(vars).hasSize(4);
    assertThat(vars.get("intVar1")).isEqualTo(100);
    assertThat(vars.get("intVar3")).isEqualTo(300);
    assertThat(vars.get("intVar5")).isEqualTo(500);
    assertThat(vars.get("intVar9")).isEqualTo(900);

    assertThat(runtimeService.getVariablesLocal(processInstanceId, asList("intVar1", "intVar3", "intVar5", "intVar9"))).hasSize(4);

    // Trying the same after moving the process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskName("Task 3").singleResult();
    String executionId = task.getExecutionId();
    assertThat(processInstanceId.equals(executionId)).isFalse();

    assertThat(runtimeService.getVariablesLocal(executionId, asList("intVar1", "intVar3", "intVar5", "intVar9"))).hasSize(0);

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
    assertThat(vars).hasSize(71);

    String varValue = (String) runtimeService.getVariable(processInstanceId, "testVar");
    assertThat(varValue).isEqualTo("HELLO world");
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableAllVariableFetchingDisabled() {

    Map<String, Object> vars = generateVariables();
    vars.put("testVar", "hello");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    String varValue = (String) runtimeService.getVariable(processInstanceId, "testVar");
    assertThat(varValue).isEqualTo("HELLO world!");
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableInDelegateMixed() {

    Map<String, Object> vars = generateVariables();
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    assertThat((String) runtimeService.getVariable(processInstanceId, "testVar")).isEqualTo("test 1 2 3");
    assertThat((String) runtimeService.getVariable(processInstanceId, "testVar2")).isEqualTo("Hiya");
  }

  @org.activiti.engine.test.Deployment
  public void testGetVariableInDelegateMixed2() {

    Map<String, Object> vars = generateVariables();
    vars.put("testVar", "1");
    String processInstanceId = runtimeService.startProcessInstanceByKey("variablesFetchingTestProcess", vars).getId();

    taskService.complete(taskService.createTaskQuery().taskName("Task A").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Task B").singleResult().getId()); // Triggers service task invocation

    assertThat((String) runtimeService.getVariable(processInstanceId, "testVar")).isEqualTo("1234");
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

    assertThat((String) runtimeService.getVariable(processInstanceId, "testVar1")).isEqualTo("one-CHANGED");
    assertThat((String) runtimeService.getVariable(processInstanceId, "testVar2")).isEqualTo("two-CHANGED");
    assertThat(runtimeService.getVariable(processInstanceId, "testVar3")).isNull();
  }

  public void testTaskGetVariables() {

    Task task = taskService.createTaskQuery().taskName("Task 1").singleResult();
    Map<String, Object> vars = taskService.getVariables(task.getId());
    assertThat(vars).hasSize(70);
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

    assertThat(nrOfStrings).isEqualTo(10);
    assertThat(nrOfBooleans).isEqualTo(10);
    assertThat(nrOfDates).isEqualTo(10);
    assertThat(nrOfLocalDates).isEqualTo(10);
    assertThat(nrOfDateTimes).isEqualTo(10);
    assertThat(nrOfInts).isEqualTo(10);
    assertThat(nrOfSerializable).isEqualTo(10);

    // Get variables local
    assertThat(taskService.getVariablesLocal(task.getId())).hasSize(0);

    // Get collection of variables
    assertThat(taskService.getVariables(task.getId(), asList("intVar2", "intVar5"))).hasSize(2);
    assertThat(taskService.getVariablesLocal(task.getId(), asList("intVar2", "intVar5"))).hasSize(0);

    // Get Variable
    assertThat(taskService.getVariable(task.getId(), "stringVar3")).isEqualTo("stringVarValue-3");
    assertThat(taskService.getVariable(task.getId(), "stringVarDoesNotExist")).isNull();
    assertThat(taskService.getVariableLocal(task.getId(), "stringVar3")).isNull();

    // Set local variable
    taskService.setVariableLocal(task.getId(), "localTaskVar", "localTaskVarValue");
    assertThat(taskService.getVariables(task.getId())).hasSize(71);
    assertThat(taskService.getVariablesLocal(task.getId())).hasSize(1);
    assertThat(taskService.getVariables(task.getId(), asList("intVar2", "intVar5"))).hasSize(2);
    assertThat(taskService.getVariablesLocal(task.getId(), asList("intVar2", "intVar5"))).hasSize(0);
    assertThat(taskService.getVariable(task.getId(), "localTaskVar")).isEqualTo("localTaskVarValue");
    assertThat(taskService.getVariableLocal(task.getId(), "localTaskVar")).isEqualTo("localTaskVarValue");

    // Override process variable
    Collection<String> varNames = new ArrayList<String>();
    varNames.add("stringVar1");
    assertThat(taskService.getVariable(task.getId(), "stringVar1")).isEqualTo("stringVarValue-1");
    assertThat(taskService.getVariable(task.getId(), "stringVar1")).isEqualTo("stringVarValue-1");
    assertThat(taskService.getVariables(task.getId(), varNames).get("stringVar1")).isEqualTo("stringVarValue-1");
    taskService.setVariableLocal(task.getId(), "stringVar1", "Override");
    assertThat(taskService.getVariables(task.getId())).hasSize(71);
    assertThat(taskService.getVariable(task.getId(), "stringVar1")).isEqualTo("Override");
    assertThat(taskService.getVariables(task.getId(), varNames).get("stringVar1")).isEqualTo("Override");
  }

  public void testLocalDateVariable() {

    Calendar todayCal = new GregorianCalendar();
    int todayYear = todayCal.get(Calendar.YEAR);
    int todayMonth = todayCal.get(Calendar.MONTH);
    int todayDate = todayCal.get(Calendar.DAY_OF_MONTH);

    // Regular getVariables after process instance start
    LocalDate date1 = (LocalDate) runtimeService.getVariable(processInstanceId, "localdateVar1");
    assertThat(date1.getYear()).isEqualTo(todayYear);
    assertThat(date1.getMonthOfYear()).isEqualTo(todayMonth + 1);
    assertThat(date1.getDayOfMonth()).isEqualTo(todayDate);

    date1 = new LocalDate(2010, 11, 10);
    runtimeService.setVariable(processInstanceId, "localdateVar1", date1);
    date1 = (LocalDate) runtimeService.getVariable(processInstanceId, "localdateVar1");
    assertThat(date1.getYear()).isEqualTo(2010);
    assertThat(date1.getMonthOfYear()).isEqualTo(11);
    assertThat(date1.getDayOfMonth()).isEqualTo(10);

    LocalDate queryDate = new LocalDate(2010, 11, 9);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("localdateVar1", queryDate).singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);

    queryDate = new LocalDate(2010, 11, 10);
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("localdateVar1", queryDate).singleResult();
    assertThat(processInstance).isNull();

    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("localdateVar1", queryDate).singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
  }

  public void testLocalDateTimeVariable() {

    Calendar todayCal = new GregorianCalendar();
    int todayYear = todayCal.get(Calendar.YEAR);
    int todayMonth = todayCal.get(Calendar.MONTH);
    int todayDate = todayCal.get(Calendar.DAY_OF_MONTH);

    // Regular getVariables after process instance start
    DateTime date1 = (DateTime) runtimeService.getVariable(processInstanceId, "datetimeVar1");
    assertThat(date1.getYear()).isEqualTo(todayYear);
    assertThat(date1.getMonthOfYear()).isEqualTo(todayMonth + 1);
    assertThat(date1.getDayOfMonth()).isEqualTo(todayDate);

    date1 = new DateTime(2010, 11, 10, 10, 15);
    runtimeService.setVariable(processInstanceId, "datetimeVar1", date1);
    date1 = (DateTime) runtimeService.getVariable(processInstanceId, "datetimeVar1");
    assertThat(date1.getYear()).isEqualTo(2010);
    assertThat(date1.getMonthOfYear()).isEqualTo(11);
    assertThat(date1.getDayOfMonth()).isEqualTo(10);
    assertThat(date1.getHourOfDay()).isEqualTo(10);
    assertThat(date1.getMinuteOfHour()).isEqualTo(15);

    DateTime queryDate = new DateTime(2010, 11, 10, 9, 15);
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("datetimeVar1", queryDate).singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);

    queryDate = new DateTime(2010, 11, 10, 10, 15);
    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("datetimeVar1", queryDate).singleResult();
    assertThat(processInstance).isNull();

    processInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("datetimeVar1", queryDate).singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getId()).isEqualTo(processInstanceId);
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
      Map<String, Object> vars = execution.getVariables(asList("testVar1", "testVar2", "testVar3"), false);

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
