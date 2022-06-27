/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.examples.variables;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.runtime.DataObject;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class VariablesTest extends PluggableActivitiTestCase {

  @Deployment
  public void testBasicVariableOperations() {
    processEngineConfiguration.getVariableTypes().addType(CustomVariableType.instance);

    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes1 = "somebytes1".getBytes();
    byte[] bytes2 = "somebytes2".getBytes();

    // 2000 characters * 2 bytes = 4000 bytes
    StringBuilder long2000StringBuilder = new StringBuilder();
    for (int i = 0; i < 2000; i++) {
      long2000StringBuilder.append("z");
    }

    // 2001 characters * 2 bytes  = 4002 bytes
    StringBuilder long2001StringBuilder = new StringBuilder();

    for (int i = 0; i < 2000; i++) {
      long2001StringBuilder.append("a");
    }
    long2001StringBuilder.append("a");


    // 4002 characters
    StringBuilder long4001StringBuilder = new StringBuilder();

    for (int i = 0; i < 4000; i++) {
      long4001StringBuilder.append("a");
    }
    long4001StringBuilder.append("a");

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("longString2000chars", long2000StringBuilder.toString());
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes1);
    variables.put("customVar1", new CustomType(bytes2));
    variables.put("customVar2", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables.get("longVar")).isEqualTo(928374L);
    assertThat(variables.get("shortVar")).isEqualTo((short) 123);
    assertThat(variables.get("integerVar")).isEqualTo(1234);
    assertThat(variables.get("stringVar")).isEqualTo("coca-cola");
    assertThat(variables.get("longString2000chars")).isEqualTo(long2000StringBuilder.toString());
    assertThat(variables.get("dateVar")).isEqualTo(now);
    assertThat(variables.get("nullVar")).isEqualTo(null);
    assertThat(variables.get("serializableVar")).isEqualTo(serializable);
    assertThat((byte[]) variables.get("bytesVar")).isEqualTo(bytes1);
    assertThat(variables.get("customVar1")).isEqualTo(new CustomType(bytes2));
    assertThat(variables.get("customVar2")).isEqualTo(null);
    assertThat(variables).hasSize(11);

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "longString2000chars", null);
    runtimeService.setVariable(processInstance.getId(), "longString4000chars", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", null);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", null);
    runtimeService.setVariable(processInstance.getId(), "customVar1", null);
    runtimeService.setVariable(processInstance.getId(), "customVar2", null);

    variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables.get("longVar")).isEqualTo(null);
    assertThat(variables.get("shortVar")).isEqualTo(null);
    assertThat(variables.get("integerVar")).isEqualTo(null);
    assertThat(variables.get("stringVar")).isEqualTo(null);
    assertThat(variables.get("longString2000chars")).isEqualTo(null);
    assertThat(variables.get("longString4000chars")).isEqualTo(null);
    assertThat(variables.get("dateVar")).isEqualTo(null);
    assertThat(variables.get("nullVar")).isEqualTo(null);
    assertThat(variables.get("serializableVar")).isEqualTo(null);
    assertThat(variables.get("bytesVar")).isEqualTo(null);
    assertThat(variables.get("customVar1")).isEqualTo(null);
    assertThat(variables.get("customVar2")).isEqualTo(null);
    assertThat(variables).hasSize(12);

    // Update existing variable values again, and add a new variable
    runtimeService.setVariable(processInstance.getId(), "new var", "hi");
    runtimeService.setVariable(processInstance.getId(), "longVar", 9987L);
    runtimeService.setVariable(processInstance.getId(), "shortVar", (short) 456);
    runtimeService.setVariable(processInstance.getId(), "integerVar", 4567);
    runtimeService.setVariable(processInstance.getId(), "stringVar", "colgate");
    runtimeService.setVariable(processInstance.getId(), "longString2000chars", long2001StringBuilder.toString());
    runtimeService.setVariable(processInstance.getId(), "longString4000chars", long4001StringBuilder.toString());
    runtimeService.setVariable(processInstance.getId(), "dateVar", now);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", bytes1);
    runtimeService.setVariable(processInstance.getId(), "customVar1", new CustomType(bytes2));
    runtimeService.setVariable(processInstance.getId(), "customVar2", new CustomType(bytes1));

    variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables.get("new var")).isEqualTo("hi");
    assertThat(variables.get("longVar")).isEqualTo(9987L);
    assertThat(variables.get("shortVar")).isEqualTo((short) 456);
    assertThat(variables.get("integerVar")).isEqualTo(4567);
    assertThat(variables.get("stringVar")).isEqualTo("colgate");
    assertThat(variables.get("longString2000chars")).isEqualTo(long2001StringBuilder.toString());
    assertThat(variables.get("longString4000chars")).isEqualTo(long4001StringBuilder.toString());
    assertThat(variables.get("dateVar")).isEqualTo(now);
    assertThat(variables.get("nullVar")).isEqualTo(null);
    assertThat(variables.get("serializableVar")).isEqualTo(serializable);
    assertThat(Arrays.equals(bytes1, (byte[]) variables.get("bytesVar"))).isTrue();
    assertThat(variables.get("customVar1")).isEqualTo(new CustomType(bytes2));
    assertThat(variables.get("customVar2")).isEqualTo(new CustomType(bytes1));
    assertThat(variables).hasSize(13);

    Collection<String> varFilter = new ArrayList<String>(2);
    varFilter.add("stringVar");
    varFilter.add("integerVar");

    Map<String, Object> filteredVariables = runtimeService.getVariables(processInstance.getId(), varFilter);
    assertThat(filteredVariables).hasSize(2);
    assertThat(filteredVariables.containsKey("stringVar")).isTrue();
    assertThat(filteredVariables.containsKey("integerVar")).isTrue();

    // Try setting the value of the variable that was initially created with value 'null'
    runtimeService.setVariable(processInstance.getId(), "nullVar", "a value");
    Object newValue = runtimeService.getVariable(processInstance.getId(), "nullVar");
    assertThat(newValue).isNotNull();
    assertThat(newValue).isEqualTo("a value");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

  }

  @Deployment
  public void testLocalizeVariables() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "coca-cola");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("localizeVariables", variables);

    Map<String, VariableInstance> variableInstances = runtimeService.getVariableInstances(processInstance.getId());
    assertThat(variableInstances).hasSize(1);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("coca-cola");

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("stringVar");

    // getVariablesInstances via names
    variableInstances = runtimeService.getVariableInstances(processInstance.getId(), variableNames);
    assertThat(variableInstances).hasSize(1);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("coca-cola");

    // getVariableInstancesLocal via names
    variableInstances = runtimeService.getVariableInstancesLocal(processInstance.getId(), variableNames);
    assertThat(variableInstances).hasSize(1);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("coca-cola");

    // getVariableInstance
    VariableInstance variableInstance = runtimeService.getVariableInstance(processInstance.getId(), "stringVar");
    assertThat(variableInstance).isNotNull();
    assertThat(variableInstance.getName()).isEqualTo("stringVar");
    assertThat(variableInstance.getValue()).isEqualTo("coca-cola");

    // getVariableInstanceLocal
    variableInstance = runtimeService.getVariableInstanceLocal(processInstance.getId(), "stringVar");
    assertThat(variableInstance).isNotNull();
    assertThat(variableInstance.getName()).isEqualTo("stringVar");
    assertThat(variableInstance.getValue()).isEqualTo("coca-cola");

    // Verify TaskService behavior
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    variableInstances = taskService.getVariableInstances(task.getId());
    assertThat(variableInstances).hasSize(2);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(variableInstances.get("intVar").getName()).isEqualTo("intVar");
    assertThat(variableInstances.get("intVar").getValue()).isEqualTo(null);

    variableNames = new ArrayList<String>();
    variableNames.add("stringVar");

    // getVariablesInstances via names
    variableInstances = taskService.getVariableInstances(task.getId(), variableNames);
    assertThat(variableInstances).hasSize(1);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("coca-cola");

    taskService.setVariableLocal(task.getId(), "stringVar", "pepsi-cola");

    // getVariableInstancesLocal via names
    variableInstances = taskService.getVariableInstancesLocal(task.getId(), variableNames);
    assertThat(variableInstances).hasSize(1);
    assertThat(variableInstances.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(variableInstances.get("stringVar").getValue()).isEqualTo("pepsi-cola");

    // getVariableInstance
    variableInstance = taskService.getVariableInstance(task.getId(), "stringVar");
    assertThat(variableInstance).isNotNull();
    assertThat(variableInstance.getName()).isEqualTo("stringVar");
    assertThat(variableInstance.getValue()).isEqualTo("pepsi-cola");

    // getVariableInstanceLocal
    variableInstance = taskService.getVariableInstanceLocal(task.getId(), "stringVar");
    assertThat(variableInstance).isNotNull();
    assertThat(variableInstance.getName()).isEqualTo("stringVar");
    assertThat(variableInstance.getValue()).isEqualTo("pepsi-cola");
  }

  @Deployment
  public void testLocalizeDataObjects() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "coca-cola");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("localizeVariables", variables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
    dynamicBpmnService.changeLocalizationName("en-US", "stringVarId", "stringVar 'en-US' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-US", "stringVarId", "stringVar 'en-US' Description", infoNode);
    dynamicBpmnService.changeLocalizationName("en-AU", "stringVarId", "stringVar 'en-AU' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-AU", "stringVarId", "stringVar 'en-AU' Description", infoNode);
    dynamicBpmnService.changeLocalizationName("en", "stringVarId", "stringVar 'en' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "stringVarId", "stringVar 'en' Description", infoNode);

    dynamicBpmnService.changeLocalizationName("en-US", "intVarId", "intVar 'en-US' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-US", "intVarId", "intVar 'en-US' Description", infoNode);
    dynamicBpmnService.changeLocalizationName("en-AU", "intVarId", "intVar 'en-AU' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-AU", "intVarId", "intVar 'en-AU' Description", infoNode);
    dynamicBpmnService.changeLocalizationName("en", "intVarId", "intVar 'en' Name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "intVarId", "intVar 'en' Description", infoNode);

    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    Map<String, DataObject> dataObjects = runtimeService.getDataObjects(processInstance.getId(), "es", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'es' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'es' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObjects
    dataObjects = runtimeService.getDataObjects(processInstance.getId(), "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("stringVar");

    // getDataObjects via names
    dataObjects = runtimeService.getDataObjects(processInstance.getId(), variableNames, "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), variableNames, "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), variableNames, "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjects(processInstance.getId(), variableNames, "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObjectsLocal
    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), "ja-JA", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObjectsLocal via names
    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), variableNames, "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), variableNames, "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), variableNames, "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = runtimeService.getDataObjectsLocal(processInstance.getId(), variableNames, "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObject
    DataObject dataObject = runtimeService.getDataObject(processInstance.getId(), "stringVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObject(processInstance.getId(), "stringVar","en-US", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObject(processInstance.getId(), "stringVar", "en-AU", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObject(processInstance.getId(), "stringVar", "en-GB", true);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObject(processInstance.getId(), "stringVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObjectLocal
    dataObject = runtimeService.getDataObjectLocal(processInstance.getId(), "stringVar", "en-US", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObjectLocal(processInstance.getId(), "stringVar", "en-AU", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObject = runtimeService.getDataObjectLocal(processInstance.getId(), "stringVar", "en-GB", true);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");

    dataObject = runtimeService.getDataObjectLocal(processInstance.getId(), "stringVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    Execution subprocess = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subprocess1").singleResult();

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), "es", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'es' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'es' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObjects
    dataObjects = runtimeService.getDataObjects(subprocess.getId(), "en-US", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), "en-AU", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), "en-GB", true);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), "en-GB", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    // getDataObjects via names (from subprocess)

    variableNames.add("intVar");
    dataObjects = runtimeService.getDataObjects(subprocess.getId(), variableNames, "en-US", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), variableNames, "en-AU", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), variableNames, "en-GB", true);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjects(subprocess.getId(), variableNames, "en-GB", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    // getDataObjectsLocal
    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), "ja-JA", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    // getDataObjectsLocal via names
    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), variableNames, "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), variableNames, "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), variableNames, "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = runtimeService.getDataObjectsLocal(subprocess.getId(), variableNames, "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("intVar").getName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getValue()).isEqualTo(null);
    assertThat(dataObjects.get("intVar").getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObjects.get("intVar").getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    // getDataObject (in subprocess)
    dataObject = runtimeService.getDataObject(subprocess.getId(), "intVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObject(subprocess.getId(), "intVar","en-US", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObject(subprocess.getId(), "intVar", "en-AU", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObject(subprocess.getId(), "intVar", "en-GB", true);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObject(subprocess.getId(), "intVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    // getDataObjectLocal (in subprocess)
    dataObject = runtimeService.getDataObjectLocal(subprocess.getId(), "intVar", "en-US", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en-US' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en-US' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObjectLocal(subprocess.getId(), "intVar", "en-AU", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en-AU' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en-AU' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObjectLocal(subprocess.getId(), "intVar", "en-GB", true);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar 'en' Name");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'en' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    dataObject = runtimeService.getDataObjectLocal(subprocess.getId(), "intVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObject.getType()).isEqualTo("int");

    // Verify TaskService behavior
    dataObjects = taskService.getDataObjects(task.getId());
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    // getDataObjects
    dataObjects = taskService.getDataObjects(task.getId());
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = taskService.getDataObjects(task.getId(), "en-US", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = taskService.getDataObjects(task.getId(), "en-AU", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = taskService.getDataObjects(task.getId(), "en-GB", true);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    dataObjects = taskService.getDataObjects(task.getId(), "en-GB", false);
    assertThat(dataObjects).hasSize(2);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");
    assertThat(dataObject.getName()).isEqualTo("intVar");
    assertThat(dataObject.getValue()).isEqualTo(null);
    assertThat(dataObject.getLocalizedName()).isEqualTo("intVar");
    assertThat(dataObject.getDescription()).isEqualTo("intVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("intVar").getDataObjectDefinitionKey()).isEqualTo("intVarId");
    assertThat(dataObjects.get("intVar").getType()).isEqualTo("int");

    variableNames = new ArrayList<String>();
    variableNames.add("stringVar");

    // getDataObjects via names
    dataObjects = taskService.getDataObjects(task.getId(), variableNames, "en-US", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = taskService.getDataObjects(task.getId(), variableNames, "en-AU", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = taskService.getDataObjects(task.getId(), variableNames, "en-GB", true);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    dataObjects = taskService.getDataObjects(task.getId(), variableNames, "en-GB", false);
    assertThat(dataObjects).hasSize(1);
    assertThat(dataObjects.get("stringVar").getName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getValue()).isEqualTo("coca-cola");
    assertThat(dataObjects.get("stringVar").getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObjects.get("stringVar").getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObjects.get("stringVar").getType()).isEqualTo("string");

    // getDataObject
    dataObject = taskService.getDataObject(task.getId(), "stringVar");
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");

    dataObject = taskService.getDataObject(task.getId(), "stringVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");

    dataObject = taskService.getDataObject(task.getId(), "stringVar","en-US", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-US' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-US' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");

    dataObject = taskService.getDataObject(task.getId(), "stringVar", "en-AU", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en-AU' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en-AU' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");

    dataObject = taskService.getDataObject(task.getId(), "stringVar", "en-GB", true);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar 'en' Name");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'en' Description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");

    dataObject = taskService.getDataObject(task.getId(), "stringVar", "en-GB", false);
    assertThat(dataObject).isNotNull();
    assertThat(dataObject.getName()).isEqualTo("stringVar");
    assertThat(dataObject.getValue()).isEqualTo("coca-cola");
    assertThat(dataObject.getLocalizedName()).isEqualTo("stringVar");
    assertThat(dataObject.getDescription()).isEqualTo("stringVar 'default' description");
    assertThat(dataObject.getDataObjectDefinitionKey()).isEqualTo("stringVarId");
    assertThat(dataObject.getType()).isEqualTo("string");
  }

  // Test case for ACT-1839
  @Deployment(resources = { "org/activiti/examples/variables/VariablesTest.testChangeTypeSerializable.bpmn20.xml" })
  public void testChangeTypeSerializable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variable-type-change-test");
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Activiti is awesome!");
    SomeSerializable myVar = (SomeSerializable) runtimeService.getVariable(processInstance.getId(), "myVar");
    assertThat(myVar.getValue()).isEqualTo("someValue");
  }

  public String getVariableInstanceId(String executionId, String name) {
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().processInstanceId(executionId).variableName(name).singleResult();
    return variable.getId();
  }

  // test case for ACT-1082
  @Deployment(resources = { "org/activiti/examples/variables/VariablesTest.testBasicVariableOperations.bpmn20.xml" })
  public void testChangeVariableType() {

    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables.get("longVar")).isEqualTo(928374L);
    assertThat(variables.get("shortVar")).isEqualTo((short) 123);
    assertThat(variables.get("integerVar")).isEqualTo(1234);
    assertThat(variables.get("stringVar")).isEqualTo("coca-cola");
    assertThat(variables.get("dateVar")).isEqualTo(now);
    assertThat(variables.get("nullVar")).isEqualTo(null);
    assertThat(variables.get("serializableVar")).isEqualTo(serializable);
    assertThat(variables.get("bytesVar")).isEqualTo(bytes);
    assertThat(variables).hasSize(8);

    // check if the id of the variable is the same or not

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      String oldSerializableVarId = getVariableInstanceId(processInstance.getId(), "serializableVar");
      String oldLongVar = getVariableInstanceId(processInstance.getId(), "longVar");

      // Change type of serializableVar from serializable to Short
      Map<String, Object> newVariables = new HashMap<String, Object>();
      newVariables.put("serializableVar", (short) 222);
      runtimeService.setVariables(processInstance.getId(), newVariables);
      variables = runtimeService.getVariables(processInstance.getId());
      assertThat(variables.get("serializableVar")).isEqualTo((short) 222);

      String newSerializableVarId = getVariableInstanceId(processInstance.getId(), "serializableVar");

      assertThat(newSerializableVarId).isEqualTo(oldSerializableVarId);

      // Change type of a  longVar from Long to Short
      newVariables = new HashMap<String, Object>();
      newVariables.put("longVar", (short) 123);
      runtimeService.setVariables(processInstance.getId(), newVariables);
      variables = runtimeService.getVariables(processInstance.getId());
      assertThat(variables.get("longVar")).isEqualTo((short) 123);

      String newLongVar = getVariableInstanceId(processInstance.getId(), "longVar");
      assertThat(newLongVar).isEqualTo(oldLongVar);
    }
  }

  // test case for ACT-1428
  @Deployment
  public void testNullVariable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    taskService.complete(task.getId(), singletonMap("testProperty", "434"));
    String resultVar = (String) runtimeService.getVariable(processInstance.getId(), "testProperty");

    assertThat(resultVar).isEqualTo("434");

    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // If no variable is given, no variable should be set and script test should throw exception
    processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    String taskId = task.getId();
    assertThatExceptionOfType(ActivitiException.class)
      .as("Should throw exception as testProperty is not defined and used in Script task")
      .isThrownBy(() -> taskService.complete(taskId, emptyMap()));
    runtimeService.deleteProcessInstance(processInstance.getId(), "intentional exception in script task");

    // No we put null property, This should be put into the variable. We do not expect exceptions
    processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    try {
      taskService.complete(task.getId(), singletonMap("testProperty", null));
    } catch (Exception e) {
      fail("Should not throw exception as the testProperty is defined, although null");
    }
    resultVar = (String) runtimeService.getVariable(processInstance.getId(), "testProperty");

    assertThat(resultVar).isNull();

    runtimeService.deleteProcessInstance(processInstance.getId(), "intentional exception in script task");
  }

  /**
   * Test added to validate UUID variable type + querying (ACT-1665)
   */
  @Deployment
  public void testUUIDVariableAndQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processInstance).isNotNull();

    // Check UUID variable type query on task
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    UUID randomUUID = UUID.randomUUID();
    taskService.setVariableLocal(task.getId(), "conversationId", randomUUID);

    Task resultingTask = taskService.createTaskQuery().taskVariableValueEquals("conversationId", randomUUID).singleResult();
    assertThat(resultingTask).isNotNull();
    assertThat(resultingTask.getId()).isEqualTo(task.getId());

    randomUUID = UUID.randomUUID();

    // Check UUID variable type query on process
    runtimeService.setVariable(processInstance.getId(), "uuidVar", randomUUID);
    ProcessInstance result = runtimeService.createProcessInstanceQuery().variableValueEquals("uuidVar", randomUUID).singleResult();

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(processInstance.getId());
  }

}

class CustomType {
  private byte[] value;

  public CustomType(byte[] value) {
    if (value == null) {
      throw new NullPointerException();
    }
    this.value = value;
  }

  public byte[] getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(value);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;

    CustomType other = (CustomType) obj;
    if (!Arrays.equals(value, other.value))
      return false;
    return true;
  }

}

/**
 * A custom variable type for testing byte array value handling.
 *
 */
class CustomVariableType implements VariableType {
  public static final CustomVariableType instance = new CustomVariableType();

  @Override
  public String getTypeName() {
    return "CustomVariableType";
  }

  @Override
  public boolean isCachable() {
    return true;
  }

  @Override
  public boolean isAbleToStore(Object value) {
    return value == null || value instanceof CustomType;
  }

  @Override
  public void setValue(Object o, ValueFields valueFields) {
    // ensure calling setBytes multiple times no longer causes any problems
    valueFields.setBytes(new byte[] { 1, 2, 3 });
    valueFields.setBytes(null);
    valueFields.setBytes(new byte[] { 4, 5, 6 });

    byte[] value = (o == null ? null : ((CustomType) o).getValue());
    valueFields.setBytes(value);
  }

  @Override
  public Object getValue(ValueFields valueFields) {
    byte[] bytes = valueFields.getBytes();
    return bytes == null ? null : new CustomType(bytes);
  }

}
