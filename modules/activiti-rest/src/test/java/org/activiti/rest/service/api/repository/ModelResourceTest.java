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

import org.activiti.engine.repository.Model;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class ModelResourceTest extends BaseSpringRestTestCase {

  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetModel() throws Exception {
    
    Model model = null;
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(now.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      model.setDeploymentId(deploymentId);
      model.setTenantId("myTenant");
      repositoryService.saveModel(model);
      
      repositoryService.addModelEditorSource(model.getId(), "This is the editor source".getBytes());
      repositoryService.addModelEditorSourceExtra(model.getId(), "This is the extra editor source".getBytes());
      
      HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId()));
      CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").textValue());
      assertEquals("Model key", responseNode.get("key").textValue());
      assertEquals("Model category", responseNode.get("category").textValue());
      assertEquals(2, responseNode.get("version").intValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").textValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").textValue());
      assertEquals(model.getId(), responseNode.get("id").textValue());
      assertEquals("myTenant", responseNode.get("tenantId").textValue());
      
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").textValue()).getTime());
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").textValue()).getTime());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
      assertTrue(responseNode.get("sourceUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE, model.getId())));
      assertTrue(responseNode.get("sourceExtraUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId())));
      
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
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    closeResponse(executeRequest(httpGet, HttpStatus.SC_NOT_FOUND));
  }
  
  public void testDeleteModel() throws Exception {
    Model model = null;
    try {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(now.getTime());
      
      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setVersion(2);
      repositoryService.saveModel(model);
      
      HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId()));
      closeResponse(executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT));
      
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
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    closeResponse(executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND));
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testUpdateModel() throws Exception {
    
    Model model = null;
    try {
      Calendar createTime = Calendar.getInstance();
      createTime.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(createTime.getTime());
      
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
      processEngineConfiguration.getClock().setCurrentTime(updateTime.getTime());
      
      // Create update request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Updated name");
      requestNode.put("category", "Updated category");
      requestNode.put("key", "Updated key");
      requestNode.put("metaInfo", "Updated metainfo");
      requestNode.put("deploymentId", deploymentId);
      requestNode.put("version", 3);
      requestNode.put("tenantId", "myTenant");
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("Updated name", responseNode.get("name").textValue());
      assertEquals("Updated key", responseNode.get("key").textValue());
      assertEquals("Updated category", responseNode.get("category").textValue());
      assertEquals(3, responseNode.get("version").intValue());
      assertEquals("Updated metainfo", responseNode.get("metaInfo").textValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").textValue());
      assertEquals(model.getId(), responseNode.get("id").textValue());
      assertEquals("myTenant", responseNode.get("tenantId").textValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").textValue()).getTime());
      assertEquals(updateTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").textValue()).getTime());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
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
      processEngineConfiguration.getClock().setCurrentTime(createTime.getTime());

      model = repositoryService.newModel();
      model.setCategory("Model category");
      model.setKey("Model key");
      model.setMetaInfo("Model metainfo");
      model.setName("Model name");
      model.setTenantId("myTenant");
      model.setVersion(2);
      repositoryService.saveModel(model);
      
      Calendar updateTime = Calendar.getInstance();
      updateTime.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(updateTime.getTime());
      
      // Create update request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", (String) null);
      requestNode.put("category", (String) null);
      requestNode.put("key", (String) null);
      requestNode.put("metaInfo", (String) null);
      requestNode.put("deploymentId", (String) null);
      requestNode.put("version", (String) null);
      requestNode.put("tenantId", (String) null);
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertNull(responseNode.get("name").textValue());
      assertNull(responseNode.get("key").textValue());
      assertNull(responseNode.get("category").textValue());
      assertNull(responseNode.get("version").textValue());
      assertNull(responseNode.get("metaInfo").textValue());
      assertNull(responseNode.get("deploymentId").textValue());
      assertNull(responseNode.get("tenantId").textValue());
      assertEquals(model.getId(), responseNode.get("id").textValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").textValue()).getTime());
      assertEquals(updateTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").textValue()).getTime());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      
      model = repositoryService.getModel(model.getId());
      assertNull(model.getName());
      assertNull(model.getKey());
      assertNull(model.getCategory());
      assertNull(model.getMetaInfo());
      assertNull(model.getDeploymentId());
      assertEquals("", model.getTenantId());
      
    } finally {
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
      processEngineConfiguration.getClock().setCurrentTime(now.getTime());
      
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId()));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").textValue());
      assertEquals("Model key", responseNode.get("key").textValue());
      assertEquals("Model category", responseNode.get("category").textValue());
      assertEquals(2, responseNode.get("version").intValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").textValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").textValue());
      assertEquals(model.getId(), responseNode.get("id").textValue());
      
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").textValue()).getTime());
      assertEquals(now.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").textValue()).getTime());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, model.getId())));
      assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
    } finally {
      try {
        repositoryService.deleteModel(model.getId());
      } catch(Throwable ignore) {
        // Ignore, model might not be created
      }
    }
  }
  
  public void testUpdateUnexistingModel() throws Exception {
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, "unexisting"));
    httpPut.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
  }
}
