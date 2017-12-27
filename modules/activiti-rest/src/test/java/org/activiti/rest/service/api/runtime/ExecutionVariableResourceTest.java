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

package org.activiti.rest.service.api.runtime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a single execution variable.
 * @author Frederik Heremans
 */
public class ExecutionVariableResourceTest extends BaseSpringRestTestCase {

    /**
     * Test getting an execution variable. GET
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testGetExecutionVariable() throws Exception {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
        runtimeService.setVariable(processInstance.getId(), "variable", "processValue");

        Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertNotNull(childExecution);
        runtimeService.setVariableLocal(childExecution.getId(), "variable", "childValue");

        // Get local scope variable
        CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "variable")),
                                                        HttpStatus.SC_OK);
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("local", responseNode.get("scope").asText());
        assertEquals("childValue", responseNode.get("value").asText());
        assertEquals("variable", responseNode.get("name").asText());
        assertEquals("string", responseNode.get("type").asText());

        // Get global scope variable
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "variable") + "?scope=global"),
                                  HttpStatus.SC_OK);
        responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("global", responseNode.get("scope").asText());
        assertEquals("processValue", responseNode.get("value").asText());
        assertEquals("variable", responseNode.get("name").asText());
        assertEquals("string", responseNode.get("type").asText());

        // Illegal scope
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, processInstance.getId(), "variable") + "?scope=illegal"),
                                  HttpStatus.SC_BAD_REQUEST);
        closeResponse(response);

        // Unexisting process
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, "unexisting", "variable")), HttpStatus.SC_NOT_FOUND);
        closeResponse(response);

        // Unexisting variable
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, processInstance.getId(), "unexistingVariable")),
                                  HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test getting execution variable data.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testGetExecutionVariableData() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
        runtimeService.setVariableLocal(processInstance.getId(), "var", "This is a binary piece of text".getBytes());

        Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertNotNull(childExecution);
        runtimeService.setVariableLocal(childExecution.getId(), "var", "This is a binary piece of text in the child execution".getBytes());

        CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "var")),
                                                        HttpStatus.SC_OK);
        String actualResponseBytesAsText = IOUtils.toString(response.getEntity().getContent());
        closeResponse(response);
        assertEquals("This is a binary piece of text in the child execution", actualResponseBytesAsText);
        assertEquals("application/octet-stream", response.getEntity().getContentType().getValue());

        // Test global scope
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "var") + "?scope=global"),
                                  HttpStatus.SC_OK);
        actualResponseBytesAsText = IOUtils.toString(response.getEntity().getContent());
        closeResponse(response);
        assertEquals("This is a binary piece of text", actualResponseBytesAsText);
        assertEquals("application/octet-stream", response.getEntity().getContentType().getValue());
    }

    /**
     * Test getting an execution variable data.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testGetExecutionVariableDataSerializable() throws Exception {

        TestSerializableVariable originalSerializable = new TestSerializableVariable();
        originalSerializable.setSomeField("This is some field");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
        runtimeService.setVariableLocal(processInstance.getId(), "var", originalSerializable);

        CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "var")),
                                                        HttpStatus.SC_OK);

        // Read the serializable from the stream
        ObjectInputStream stream = new ObjectInputStream(response.getEntity().getContent());
        Object readSerializable = stream.readObject();
        closeResponse(response);
        assertNotNull(readSerializable);
        assertTrue(readSerializable instanceof TestSerializableVariable);
        assertEquals("This is some field", ((TestSerializableVariable) readSerializable).getSomeField());
        assertEquals("application/x-java-serialized-object", response.getEntity().getContentType().getValue());
    }

    /**
     * Test getting an execution variable, for illegal vars.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testGetExecutionDataForIllegalVariables() throws Exception {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne");
        runtimeService.setVariableLocal(processInstance.getId(), "localTaskVariable", "this is a plain string variable");

        // Try getting data for non-binary variable
        CloseableHttpResponse response = executeRequest(
                new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "localTaskVariable")), HttpStatus.SC_NOT_FOUND);
        closeResponse(response);

        // Try getting data for unexisting property
        response = executeRequest(new HttpGet(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, processInstance.getId(), "unexistingVariable")),
                                  HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test deleting a single execution variable, including "not found" check.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testDeleteExecutionVariable() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", Collections.singletonMap("myVariable", (Object) "processValue"));

        Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertNotNull(childExecution);
        runtimeService.setVariableLocal(childExecution.getId(), "myVariable", "childValue");

        // Delete variable local
        HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVariable"));
        CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
        closeResponse(response);

        assertFalse(runtimeService.hasVariableLocal(childExecution.getId(), "myVariable"));
        // Global variable should remain unaffected
        assertTrue(runtimeService.hasVariable(childExecution.getId(), "myVariable"));

        // Delete variable global
        httpDelete = new HttpDelete(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVariable") + "?scope=global");
        response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
        closeResponse(response);

        assertFalse(runtimeService.hasVariableLocal(childExecution.getId(), "myVariable"));
        assertFalse(runtimeService.hasVariable(childExecution.getId(), "myVariable"));

        // Run the same delete again, variable is not there so 404 should be returned
        response = executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test updating a single execution variable, including "not found" check.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testUpdateExecutionVariable() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", Collections.singletonMap("overlappingVariable", (Object) "processValue"));
        runtimeService.setVariableLocal(processInstance.getId(), "myVar", "processValue");

        Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertNotNull(childExecution);
        runtimeService.setVariableLocal(childExecution.getId(), "myVar", "childValue");

        // Update variable local
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("name", "myVar");
        requestNode.put("value", "updatedValue");
        requestNode.put("type", "string");

        HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVar"));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("updatedValue", responseNode.get("value").asText());
        assertEquals("local", responseNode.get("scope").asText());

        // Global value should be unaffected
        assertEquals("processValue", runtimeService.getVariable(processInstance.getId(), "myVar"));
        assertEquals("updatedValue", runtimeService.getVariableLocal(childExecution.getId(), "myVar"));

        // Update variable global
        requestNode = objectMapper.createObjectNode();
        requestNode.put("name", "myVar");
        requestNode.put("value", "updatedValueGlobal");
        requestNode.put("type", "string");
        requestNode.put("scope", "global");

        httpPut = new HttpPut(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "myVar"));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        response = executeRequest(httpPut, HttpStatus.SC_OK);
        responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("updatedValueGlobal", responseNode.get("value").asText());
        assertEquals("global", responseNode.get("scope").asText());

        // Local value should be unaffected
        assertEquals("updatedValueGlobal", runtimeService.getVariable(processInstance.getId(), "myVar"));
        assertEquals("updatedValue", runtimeService.getVariableLocal(childExecution.getId(), "myVar"));

        requestNode.put("name", "unexistingVariable");

        httpPut.setEntity(new StringEntity(requestNode.toString()));
        response = executeRequest(httpPut, HttpStatus.SC_BAD_REQUEST);
        closeResponse(response);

        httpPut = new HttpPut(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "unexistingVariable"));
        httpPut.setEntity(new StringEntity(requestNode.toString()));
        response = executeRequest(httpPut, HttpStatus.SC_NOT_FOUND);
        closeResponse(response);
    }

    /**
     * Test updating a single execution variable using a binary stream.
     */
    @Deployment(resources = {"org/activiti/rest/service/api/runtime/ExecutionResourceTest.process-with-subprocess.bpmn20.xml"})
    public void testUpdateBinaryExecutionVariable() throws Exception {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processOne", Collections.singletonMap("overlappingVariable", (Object) "processValue"));
        runtimeService.setVariableLocal(processInstance.getId(), "binaryVariable", "Initial binary value".getBytes());

        Execution childExecution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertNotNull(childExecution);
        runtimeService.setVariableLocal(childExecution.getId(), "binaryVariable", "Initial binary value child".getBytes());

        InputStream binaryContent = new ByteArrayInputStream("This is binary content".getBytes());

        // Add name and type
        Map<String, String> additionalFields = new HashMap<String, String>();
        additionalFields.put("name", "binaryVariable");
        additionalFields.put("type", "binary");

        HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "binaryVariable"));
        httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
        CloseableHttpResponse response = executeBinaryRequest(httpPut, HttpStatus.SC_OK);

        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("binaryVariable", responseNode.get("name").asText());
        assertTrue(responseNode.get("value").isNull());
        assertEquals("local", responseNode.get("scope").asText());
        assertEquals("binary", responseNode.get("type").asText());
        assertNotNull(responseNode.get("valueUrl").isNull());
        assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "binaryVariable")));

        // Check actual value of variable in engine
        Object variableValue = runtimeService.getVariableLocal(childExecution.getId(), "binaryVariable");
        assertNotNull(variableValue);
        assertTrue(variableValue instanceof byte[]);
        assertEquals("This is binary content", new String((byte[]) variableValue));

        // Update variable in global scope
        additionalFields.put("scope", "global");
        binaryContent = new ByteArrayInputStream("This is binary content global".getBytes());

        httpPut = new HttpPut(SERVER_URL_PREFIX + RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE, childExecution.getId(), "binaryVariable"));
        httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("value", "application/octet-stream", binaryContent, additionalFields));
        response = executeBinaryRequest(httpPut, HttpStatus.SC_OK);
        responseNode = objectMapper.readTree(response.getEntity().getContent());
        closeResponse(response);
        assertNotNull(responseNode);
        assertEquals("binaryVariable", responseNode.get("name").asText());
        assertTrue(responseNode.get("value").isNull());
        assertEquals("global", responseNode.get("scope").asText());
        assertEquals("binary", responseNode.get("type").asText());
        assertNotNull(responseNode.get("valueUrl").isNull());
        assertTrue(responseNode.get("valueUrl").asText().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, childExecution.getId(), "binaryVariable")));

        // Check actual global value of variable in engine
        variableValue = runtimeService.getVariableLocal(processInstance.getId(), "binaryVariable");
        assertNotNull(variableValue);
        assertTrue(variableValue instanceof byte[]);
        assertEquals("This is binary content global", new String((byte[]) variableValue));

        // local value should remain unchainged
        variableValue = runtimeService.getVariableLocal(childExecution.getId(), "binaryVariable");
        assertNotNull(variableValue);
        assertTrue(variableValue instanceof byte[]);
        assertEquals("This is binary content", new String((byte[]) variableValue));
    }
}
