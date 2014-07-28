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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to a identity links on a Process
 * instance resource.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceIdentityLinkResourceTest extends BaseRestTestCase {

  /**
   * Test getting all identity links.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testGetIdentityLinks() throws Exception {

    // Test candidate user/groups links + manual added identityLink
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.addUserIdentityLink(processInstance.getId(), "john", "customType");
    runtimeService.addUserIdentityLink(processInstance.getId(), "paul", "candidate");

    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION,
            processInstance.getId()));
    // Execute the request
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION,
            processInstance.getId()));

    // Add user link
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    requestNode.put("type", "myType");

    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("myType", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").textValue()
            .endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "kermit", "myType")));

    // Test with unexisting process
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, "unexistingprocess"));
    try {
      client.post(null);
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a process instance with id 'unexistingprocess'.", expected.getStatus().getDescription());
    }

    // Test with no user
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINKS_COLLECTION, processInstance.getId()));
    requestNode = objectMapper.createObjectNode();
    requestNode.put("type", "myType");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("The user is required.", expected.getStatus().getDescription());
    }

    // Test with group (which is not supported on processes)
    requestNode = objectMapper.createObjectNode();
    requestNode.put("type", "myType");
    requestNode.put("group", "sales");
    try {
      client.release();
      client.post(requestNode);
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Only user identity links are supported on a process instance.", expected.getStatus().getDescription());
    }

    // Test with no type
    requestNode = objectMapper.createObjectNode();
    requestNode.put("user", "kermit");
    try {
      client.release();
      client.post(requestNode);
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("The identity link type is required.", expected.getStatus().getDescription());
    }
  }

  /**
   * Test getting a single identity link for a process instance.
   */
  @Deployment(resources = { "org/activiti/rest/service/api/runtime/ProcessInstanceIdentityLinkResourceTest.process.bpmn20.xml" })
  public void testGetSingleIdentityLink() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "myType");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK,
            processInstance.getId(), "kermit", "myType"));

    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());

    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("kermit", responseNode.get("user").textValue());
    assertEquals("myType", responseNode.get("type").textValue());
    assertTrue(responseNode.get("group").isNull());
    assertTrue(responseNode.get("url").textValue().endsWith(
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, processInstance.getId(), "kermit", "myType")));

    // Test with unexisting process
    client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK, "unexistingprocess",
            RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS, "kermit", "myType"));
    try {
      client.get();
      fail("Exception expected");
    } catch (ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a process instance with id 'unexistingprocess'.", expected.getStatus().getDescription());
    }
  }
}
