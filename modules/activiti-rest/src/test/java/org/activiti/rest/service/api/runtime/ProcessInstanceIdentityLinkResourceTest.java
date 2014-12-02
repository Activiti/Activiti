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

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a identity links on a Process
 * instance resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceIdentityLinkResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting all identity links.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testGetIdentityLinks() throws Exception {

    // Test candidate user/groups links + manual added identityLink
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.addUserIdentityLink(processInstance.getId(), "john", "customType");
    runtimeService.addUserIdentityLink(processInstance.getId(), "paul", "candidate");

    // Execute the request
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, processInstance.getId())), HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertTrue(responseNode.isArray());
    assertEquals(2, responseNode.size());

    boolean johnFound = false;
    boolean paulFound = false;

    for (int i = 0; i < responseNode.size(); i++) {
      ObjectNode link = (ObjectNode) responseNode.get(i);
      assertNotNull(link);
      if (!link.get("user").isNull()) {
        if (link.get("user").textValue().equals("john")) {
          assertEquals("customType", link.get("type").textValue());
          assertTrue(link.get("group").isNull());
          assertTrue(link.get("url").textValue()
                  .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "john", "customType")));
          johnFound = true;
        } else {
          assertEquals("paul", link.get("user").textValue());
          assertEquals("candidate", link.get("type").textValue());
          assertTrue(link.get("group").isNull());
          assertTrue(link.get("url").textValue()
                  .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "paul", "candidate")));
          paulFound = true;
        }
      }
    }
    assertTrue(johnFound);
    assertTrue(paulFound);
  }

  /**
   * Test creating an identity link.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testCreateIdentityLink() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // Add user link
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    requestNode.put("type", "myType");

    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("myType", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").textValue()
            .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "kermit", "myType")));

    // Test with unexisting process
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, "unexistingprocess"));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));

    // Test with no user
    requestNode = objectMapper.createObjectNode();
    requestNode.put("type", "myType");
    
    httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, processInstance.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));

    // Test with group (which is not supported on processes)
    requestNode = objectMapper.createObjectNode();
    requestNode.put("type", "myType");
    requestNode.put("group", "sales");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));

    // Test with no type
    requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
  }

  /**
   * Test getting a single identity link for a process instance.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testGetSingleIdentityLink() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "myType");
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "kermit", "myType")), HttpStatus.SC_OK);
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("myType", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").textValue().endsWith(
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "kermit", "myType")));

    // Test with unexisting process
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, 
            RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test deleting a single identity link for a process instance.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testDeleteSingleIdentityLink() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "myType");
    
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, 
            processInstance.getId(), "kermit", "myType")), HttpStatus.SC_NO_CONTENT));
    
    // Test with unexisting process identity link
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, 
            processInstance.getId(), "kermit", "myType")), HttpStatus.SC_NOT_FOUND));
    
    // Test with unexisting process
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, 
            "unexistingprocess", RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType")), HttpStatus.SC_NOT_FOUND));
  }
}
