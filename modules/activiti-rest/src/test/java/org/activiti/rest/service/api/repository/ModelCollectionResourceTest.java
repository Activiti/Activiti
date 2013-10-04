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


/**
 * @author Frederik Heremans
 */
public class ModelCollectionResourceTest extends BaseRestTestCase {

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
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?category=Another category";
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on category like
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?categoryLike=" + encode("Mode%");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on category not equals
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?categoryNotEquals=Another category";
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on name
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?name=Another name";
      assertResultsPresentInDataResponse(url, model2.getId());
      
      // Filter based on name like
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?nameLike=" + encode("%del name");
      assertResultsPresentInDataResponse(url, model1.getId());
      
      // Filter based on key
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?key=Model key";
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
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL_COLLECTION) + "?key=Model key&latestVersion=true";
      // Make sure both models have same key
      model2 = repositoryService.createModelQuery().modelId(model2.getId()).singleResult();
      model2.setKey("Model key");
      repositoryService.saveModel(model2);
      assertResultsPresentInDataResponse(url, model2.getId());
      
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
      ClockUtil.setCurrentTime(createTime.getTime());
      
      // Create create request
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Model name");
      requestNode.put("category", "Model category");
      requestNode.put("key", "Model key");
      requestNode.put("metaInfo", "Model metainfo");
      requestNode.put("deploymentId", deploymentId);
      requestNode.put("version", 2);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_MODEL_COLLECTION));
      Representation response = client.post(requestNode);
      
      // Check "CREATED" status
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("Model name", responseNode.get("name").getTextValue());
      assertEquals("Model key", responseNode.get("key").getTextValue());
      assertEquals("Model category", responseNode.get("category").getTextValue());
      assertEquals(2, responseNode.get("version").getIntValue());
      assertEquals("Model metainfo", responseNode.get("metaInfo").getTextValue());
      assertEquals(deploymentId, responseNode.get("deploymentId").getTextValue());
      
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("createTime").getTextValue()).getTime());
      assertEquals(createTime.getTime().getTime(), getDateFromISOString(responseNode.get("lastUpdateTime").getTextValue()).getTime());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_MODEL, responseNode.get("id").getTextValue())));
      assertTrue(responseNode.get("deploymentUrl").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_DEPLOYMENT, deploymentId)));
      
      model = repositoryService.createModelQuery().modelId(responseNode.get("id").getTextValue()).singleResult();
      assertNotNull(model);
      assertEquals("Model category", model.getCategory());
      assertEquals("Model name", model.getName());
      assertEquals("Model key", model.getKey());
      assertEquals(deploymentId, model.getDeploymentId());
      assertEquals("Model metainfo", model.getMetaInfo());
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
