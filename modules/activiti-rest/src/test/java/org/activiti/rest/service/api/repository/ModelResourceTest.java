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

package org.activiti.rest.service.api.repository;

import java.util.Calendar;

import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.Model;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class ModelResourceTest extends BaseRestTestCase {

  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetModel() throws Exception {
    
    Model model = null;
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(now.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      model.setDeploymentId(deploymentId);
      repositoryService.saveModel(model);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL, model.getId()));
      Representation response = client.get();
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").getTextValue());
      assertEquals("Model key", responseNode.get("key").getTextValue());
      assertEquals("Model category", responseNode.get("category").getTextValue());
      assertEquals(2, responseNode.get("version").getIntValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").getTextValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").getTextValue());
      assertEquals(model.getId(), responseNode.get("id").getTextValue());
      
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").getTextValue()).getTime());
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").getTextValue()).getTime());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testGetUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    try {
      client.get();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  public void testDeleteModel() throws Exception {
    Model model = null;
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(now.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      repositoryService.saveModel(model);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL, model.getId()));
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0, response.getSize());
      
      // Check if the model is really gone
      assertNull(repositoryService.createModelQuery().modelId(model.getId()).singleResult());
      
      model = null;
    } finally
    {
      if(model != null) {
        try {
          repositoryService.deleteModel(model.getId());
        } catch(Throwable ignore) {
          // Ignore, model might not be created
        }
      }
    }
  }
  public void testDeleteUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    try {
      client.delete();
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testUpdateModel() throws Exception {
    
    Model model = null;
    try {
      Calendar createTime = Calendar.getInstance();
      createTime.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(createTime.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      repositoryService.saveModel(model);
      
      
      Calendar updateTime = Calendar.getInstance();
      updateTime.set(Calendar.MILLISECOND, 0);
      updateTime.add(Calendar.HOUR, 1);
      ClockUtil.setCurrentTime(updateTime.getTime());
      
      // Create update request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Updated name");
      requestNode.put("category", "Updated category");
      requestNode.put("key", "Updated key");
      requestNode.put("metaInfo", "Updated metainfo");
      requestNode.put("deploymentId", deploymentId);
      requestNode.put("version", 3);
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL, model.getId()));
      Representation response = client.put(requestNode);
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("Updated name", responseNode.get("name").getTextValue());
      assertEquals("Updated key", responseNode.get("key").getTextValue());
      assertEquals("Updated category", responseNode.get("category").getTextValue());
      assertEquals(3, responseNode.get("version").getIntValue());
      assertEquals("Updated metainfo", responseNode.get("metaInfo").getTextValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").getTextValue());
      assertEquals(model.getId(), responseNode.get("id").getTextValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").getTextValue()).getTime());
      assertEquals(updateTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").getTextValue()).getTime());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testUpdateModelOverrideWithNull() throws Exception {
    Model model = null;
    try {
      Calendar createTime = Calendar.getInstance();
      createTime.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(createTime.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      repositoryService.saveModel(model);
      
      
      Calendar updateTime = Calendar.getInstance();
      updateTime.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(updateTime.getTime());
      
      // Create update request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", (String) null);
      requestNode.put("category", (String) null);
      requestNode.put("key", (String) null);
      requestNode.put("metaInfo", (String) null);
      requestNode.put("deploymentId", (String) null);
      requestNode.put("version", (String) null);
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL, model.getId()));
      Representation response = client.put(requestNode);
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertNull(responseNode.get("name").getTextValue());
      assertNull(responseNode.get("key").getTextValue());
      assertNull(responseNode.get("category").getTextValue());
      assertNull(responseNode.get("version").getTextValue());
      assertNull(responseNode.get("metaInfo").getTextValue());
      assertNull(responseNode.get("deploymentId").getTextValue());
      assertEquals(model.getId(), responseNode.get("id").getTextValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").getTextValue()).getTime());
      assertEquals(updateTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").getTextValue()).getTime());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testUpdateModelNoFields() throws Exception {
    
    Model model = null;
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      ClockUtil.setCurrentTime(now.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      model.setDeploymentId(deploymentId);
      repositoryService.saveModel(model);
      
      // Use empty request-node, nothing should be changed after update
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL, model.getId()));
      Representation response = client.put(requestNode);
      
      // Check "OK" status
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").getTextValue());
      assertEquals("Model key", responseNode.get("key").getTextValue());
      assertEquals("Model category", responseNode.get("category").getTextValue());
      assertEquals(2, responseNode.get("version").getIntValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").getTextValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").getTextValue());
      assertEquals(model.getId(), responseNode.get("id").getTextValue());
      
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").getTextValue()).getTime());
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").getTextValue()).getTime());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
    } finally
    {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testUpdateUnexistingModel() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    try {
      client.put(objectMapper.createObjectNode());
      fail("404 expected, but was: " + client.getResponse().getStatus());
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, client.getResponse().getStatus());
      assertEquals("Could not find a model with id 'unexisting'.", client.getResponse().getStatus().getDescription());
    }
  }
}
