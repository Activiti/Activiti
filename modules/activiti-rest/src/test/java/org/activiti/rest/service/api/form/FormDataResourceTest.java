/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;


/**
 * Test for all REST-operations related to a Form data resource.
 * 
 * @author Tijs Rademakers
 */
public class FormDataResourceTest extends BaseRestTestCase {
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  
  @Deployment
  public void testGetFormData() throws Exception {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("SpeakerName", "John Doe");
    Address address = new Address();
    variableMap.put("address", address);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variableMap);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_FORM_DATA) + "?taskId=" + task.getId());
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    // Check resulting task
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertEquals(7, responseNode.get("formProperties").size());
    Map<String, JsonNode> mappedProperties = new HashMap<String, JsonNode>();
    for (JsonNode propNode : responseNode.get("formProperties")) {
      mappedProperties.put(propNode.get("id").asText(), propNode);
    }
    JsonNode propNode = mappedProperties.get("room");
    assertNotNull(propNode);
    assertEquals("room", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertTrue(propNode.get("type").isNull());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("duration");
    assertNotNull(propNode);
    assertEquals("duration", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertEquals("long", propNode.get("type").asText());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("speaker");
    assertNotNull(propNode);
    assertEquals("speaker", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertTrue(propNode.get("type").isNull());
    assertEquals("John Doe", propNode.get("value").asText());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean() == false);
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("street");
    assertNotNull(propNode);
    assertEquals("street", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertTrue(propNode.get("type").isNull());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean());
    
    propNode = mappedProperties.get("start");
    assertNotNull(propNode);
    assertEquals("start", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertEquals("date", propNode.get("type").asText());
    assertTrue(propNode.get("value").isNull());
    assertEquals("dd-MMM-yyyy", propNode.get("datePattern").asText());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("end");
    assertNotNull(propNode);
    assertEquals("end", propNode.get("id").asText());
    assertEquals("End", propNode.get("name").asText());
    assertEquals("date", propNode.get("type").asText());
    assertTrue(propNode.get("value").isNull());
    assertEquals("dd/MM/yyyy", propNode.get("datePattern").asText());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("direction");
    assertNotNull(propNode);
    assertEquals("direction", propNode.get("id").asText());
    assertTrue(propNode.get("name").isNull());
    assertEquals("enum", propNode.get("type").asText());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("datePattern").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    JsonNode enumValues = propNode.get("enumValues");
    assertEquals(4, enumValues.size());
    Map<String, String> mappedEnums = new HashMap<String, String>();
    for (JsonNode enumNode : enumValues) {
      mappedEnums.put(enumNode.get("id").asText(), enumNode.get("name").asText());
    }
    assertEquals("Go Left", mappedEnums.get("left"));
    assertEquals("Go Right", mappedEnums.get("right"));
    assertEquals("Go Up", mappedEnums.get("up"));
    assertEquals("Go Down", mappedEnums.get("down"));
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_FORM_DATA) + "?processDefinitionId=" + processInstance.getProcessDefinitionId());
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    // Check resulting task
    responseNode = objectMapper.readTree(response.getStream());
    assertEquals(2, responseNode.get("formProperties").size());
    mappedProperties.clear();
    for (JsonNode propertyNode : responseNode.get("formProperties")) {
      mappedProperties.put(propertyNode.get("id").asText(), propertyNode);
    }
    
    propNode = mappedProperties.get("number");
    assertNotNull(propNode);
    assertEquals("number", propNode.get("id").asText());
    assertEquals("Number", propNode.get("name").asText());
    assertEquals("long", propNode.get("type").asText());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    propNode = mappedProperties.get("description");
    assertNotNull(propNode);
    assertEquals("description", propNode.get("id").asText());
    assertEquals("Description", propNode.get("name").asText());
    assertTrue(propNode.get("type").isNull());
    assertTrue(propNode.get("value").isNull());
    assertTrue(propNode.get("readable").asBoolean());
    assertTrue(propNode.get("writable").asBoolean());
    assertTrue(propNode.get("required").asBoolean() == false);
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_FORM_DATA) + "?processDefinitionId=123");
    try {
      response = client.get();
      fail();
    } catch(Exception e) {
      // expected
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
    }
    
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_FORM_DATA) + "?processDefinitionId2=123");
    try {
      response = client.get();
      fail();
    } catch(Exception e) {
      // expected
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, client.getResponse().getStatus());
    }
  }
  
  @Deployment
  public void testSubmitFormData() throws Exception {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("SpeakerName", "John Doe");
    Address address = new Address();
    variableMap.put("address", address);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variableMap);
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_FORM_DATA));
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("taskId", task.getId());
    ArrayNode propertyArray = objectMapper.createArrayNode();
    requestNode.put("properties", propertyArray);
    ObjectNode propNode = objectMapper.createObjectNode();
    propNode.put("id", "room");
    propNode.put("value", 123l);
    propertyArray.add(propNode);
    try {
      client.post(requestNode);
    } catch(Exception e) {
      // expected
      assertEquals(Status.SERVER_ERROR_INTERNAL, client.getResponse().getStatus());
    }
    
    propNode = objectMapper.createObjectNode();
    propNode.put("id", "street");
    propNode.put("value", "test");
    propertyArray.add(propNode);
    client.release();
    client.post(requestNode);
    
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    assertNull(task);
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    assertNull(processInstance);
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
    Map<String, HistoricVariableInstance> historyMap = new HashMap<String, HistoricVariableInstance>();
    for (HistoricVariableInstance historicVariableInstance : variables) {
      historyMap.put(historicVariableInstance.getVariableName(), historicVariableInstance);
    }
    
    assertEquals("123", historyMap.get("room").getValue());
    assertEquals(processInstanceId, historyMap.get("room").getProcessInstanceId());
    
    processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variableMap);
    processInstanceId = processInstance.getId();
    task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    
    requestNode.put("taskId", task.getId());
    propNode = objectMapper.createObjectNode();
    propNode.put("id", "direction");
    propNode.put("value", "nowhere");
    propertyArray.add(propNode);
    try {
      client.release();
      client.post(requestNode);
    } catch(Exception e) {
      // expected
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, client.getResponse().getStatus());
    }
    
    propNode.put("value", "up");
    client.release();
    client.post(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    assertNull(task);
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    assertNull(processInstance);
    variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
    historyMap.clear();
    for (HistoricVariableInstance historicVariableInstance : variables) {
      historyMap.put(historicVariableInstance.getVariableName(), historicVariableInstance);
    }
    
    assertEquals("123", historyMap.get("room").getValue());
    assertEquals(processInstanceId, historyMap.get("room").getProcessInstanceId());
    assertEquals("up", historyMap.get("direction").getValue());
    
    requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionId", processDefinitionId);
    propertyArray = objectMapper.createArrayNode();
    requestNode.put("properties", propertyArray);
    propNode = objectMapper.createObjectNode();
    propNode.put("id", "number");
    propNode.put("value", 123);
    propertyArray.add(propNode);
    client.release();
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode.get("id").asText());
    assertEquals(processDefinitionId, responseNode.get("processDefinitionId").asText());
    task = taskService.createTaskQuery().processInstanceId(responseNode.get("id").asText()).singleResult();
    assertNotNull(task);
  }
}
