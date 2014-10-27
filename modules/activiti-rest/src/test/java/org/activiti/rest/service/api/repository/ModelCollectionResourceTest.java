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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class ModelCollectionResourceTest extends BaseSpringRestTestCase {

  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetModels() throws Exception {
    // Create 2 models
    Model model1 = null;
    Model model2 = null;
    
    try {
      model1 = repositoryService.newModel();
      model1.setCategory("Model category");
      model1.setKey("Model key");
      model1.setMetaInfo("Model metainfo");
      model1.setName("Model name");
      model1.setVersion(2);
      model1.setDeploymentId(deploymentId);
      repositoryService.saveModel(model1);
      
      model2 = repositoryService.newModel();
      model2.setCategory("Another category");
      model2.setKey("Another key");
      model2.setMetaInfo("Another metainfo");
      model2.setName("Another name");
      model2.setVersion(3);
      repositoryService.saveModel(model2);
      
      // Try filter-less, should return all models
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION);
      assertResultsPresentInDataResponse(url, model1.getId(), model2.getId());
      
      // Filter based on id
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?id=" + model1.getId();
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on category
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?category=" + encode("Another category");
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on category like
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?categoryLike=" + encode("Mode%");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on category not equals
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?categoryNotEquals=" + encode("Another category");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on name
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?name=" + encode("Another name");
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on name like
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?nameLike=" + encode("%del name");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on key
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?key=" + encode("Model key");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on version
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?version=3";
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on deploymentId
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?deploymentId=" + deploymentId;
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on deployed=true
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?deployed=true";
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on deployed=false
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?deployed=false";
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on latestVersion
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?key=" + encode("Model key") + "&latestVersion=true";
      // Make sure both models have same key
      model2 = repositoryService.createModelQuery().modelId(model2.getId()).singleResult();
      model2.setKey("Model key");
      repositoryService.saveModel(model2);
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter without tenant ID, before tenant update
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, model1.getId(), model2.getId());

      // Set tenant ID
      model1 = repositoryService.getModel(model1.getId());
      model1.setTenantId("myTenant");
      repositoryService.saveModel(model1);
      
      // Filter without tenant ID, after tenant update
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on tenantId
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?tenantId=myTenant";
      assertResultsPresentInDataResponse(url, model1.getId());
      
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?tenantId=anotherTenant";
      assertResultsPresentInDataResponse(url);
      
      // Filter based on tenantId like
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?tenantIdLike=" + encode("%enant");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?tenantIdLike=anotherTenant";
      assertResultsPresentInDataResponse(url);
      
      
      
    } finally {
      if(model1 != null) {
        try {
          repositoryService.deleteModel(model1.getId());
        } catch(Throwable ignore) { } 
      }
      if(model2 != null) {
        try {
          repositoryService.deleteModel(model2.getId());
        } catch(Throwable ignore) { }
      }
    }
  }
  
  @Deployment(resources={"org/activiti/rest/service/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testCreateModel() throws Exception {
    Model model = null;
    try {
      
      Calendar createTime = Calendar.getInstance();
      createTime.set(Calendar.MILLISECOND, 0);
      processEngineConfiguration.getClock().setCurrentTime(createTime.getTime());
      
      // Create create request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Model name");
      requestNode.put("category", "Model category");
      requestNode.put("key", "Model key");
      requestNode.put("metaInfo", "Model metainfo");
      requestNode.put("deploymentId", deploymentId);
      requestNode.put("version", 2);
      requestNode.put("tenantId", "myTenant");
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").textValue());
      assertEquals("Model key", responseNode.get("key").textValue());
      assertEquals("Model category", responseNode.get("category").textValue());
      assertEquals(2, responseNode.get("version").intValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").textValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").textValue());
      assertEquals("myTenant", responseNode.get("tenantId").textValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").textValue()).getTime());
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").textValue()).getTime());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, responseNode.get("id").textValue())));
      assertTrue(responseNode.get("deploymentUrl").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
      model = repositoryService.createModelQuery().modelId(responseNode.get("id").textValue()).singleResult();
      assertNotNull(model);
      assertEquals("Model category", model.getCategory());
      assertEquals("Model name", model.getName());
      assertEquals("Model key", model.getKey());
      assertEquals(deploymentId, model.getDeploymentId());
      assertEquals("Model metainfo", model.getMetaInfo());
      assertEquals("myTenant", model.getTenantId());
      assertEquals(2, model.getVersion().intValue());
      
    } finally {
      if(model != null) {
        try {
          repositoryService.deleteModel(model.getId());
        } catch(Throwable ignore) { } 
      }
    }
  }
}
