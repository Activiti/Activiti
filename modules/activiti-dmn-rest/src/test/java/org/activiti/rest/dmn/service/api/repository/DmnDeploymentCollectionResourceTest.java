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
package org.activiti.rest.dmn.service.api.repository;

import java.util.Calendar;
import java.util.List;

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.rest.dmn.service.api.BaseSpringDmnRestTestCase;
import org.activiti.rest.dmn.service.api.DmnRestUrls;
import org.activiti.rest.dmn.service.api.HttpMultipartHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 */
public class DmnDeploymentCollectionResourceTest extends BaseSpringDmnRestTestCase {

  /**
   * Test deploying single DMN file
   */
  public void testPostNewDeploymentDMNFile() throws Exception {

    try {
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION));

      httpPost.setEntity(HttpMultipartHelper.getMultiPartEntity("simple.dmn", "application/xml", this.getClass().getClassLoader().getResourceAsStream("org/activiti/rest/dmn/service/api/repository/simple.dmn"), null));
      CloseableHttpResponse response = executeBinaryRequest(httpPost, HttpStatus.SC_CREATED);

      // Check deployment
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);

      String deploymentId = responseNode.get("id").textValue();
      String name = responseNode.get("name").textValue();
      String category = responseNode.get("category").textValue();
      String deployTime = responseNode.get("deploymentTime").textValue();
      String url = responseNode.get("url").textValue();
      String tenantId = responseNode.get("tenantId").textValue();

      assertEquals("", tenantId);

      assertNotNull(deploymentId);
      assertEquals(1L, dmnRepositoryService.createDeploymentQuery().deploymentId(deploymentId).count());

      assertNotNull(name);
      assertEquals("simple.dmn", name);

      assertNotNull(url);
      assertTrue(url.endsWith(DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, deploymentId)));

      // No deployment-category should have been set
      assertNull(category);
      assertNotNull(deployTime);

      // Check if process is actually deployed in the deployment
      List<String> resources = dmnRepositoryService.getDeploymentResourceNames(deploymentId);
      assertEquals(1L, resources.size());
      assertEquals("simple.dmn", resources.get(0));
      assertEquals(1L, dmnRepositoryService.createDeploymentQuery().deploymentId(deploymentId).count());
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<DmnDeployment> deployments = dmnRepositoryService.createDeploymentQuery().list();
      for (DmnDeployment deployment : deployments) {
        dmnRepositoryService.deleteDeployment(deployment.getId());
      }
    }
  }

  /**
   * Test getting deployments. GET dmn-repository/deployments
   */
  public void testGetDeployments() throws Exception {

    try {
      // Alter time to ensure different deployTimes
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.DAY_OF_MONTH, -1);
      dmnEngineConfiguration.getClock().setCurrentTime(yesterday.getTime());

      DmnDeployment firstDeployment = dmnRepositoryService.createDeployment().name("Deployment 1").category("DEF").addClasspathResource("org/activiti/rest/dmn/service/api/repository/simple.dmn")
          .deploy();

      dmnEngineConfiguration.getClock().setCurrentTime(Calendar.getInstance().getTime());
      DmnDeployment secondDeployment = dmnRepositoryService.createDeployment().name("Deployment 2").category("ABC").addClasspathResource("org/activiti/rest/dmn/service/api/repository/simple.dmn")
          .tenantId("myTenant").deploy();

      String baseUrl = DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION);
      assertResultsPresentInDataResponse(baseUrl, firstDeployment.getId(), secondDeployment.getId());

      // Check name filtering
      String url = baseUrl + "?name=" + encode("Deployment 1");
      assertResultsPresentInDataResponse(url, firstDeployment.getId());

      // Check name-like filtering
      url = baseUrl + "?nameLike=" + encode("%ment 2");
      assertResultsPresentInDataResponse(url, secondDeployment.getId());

      // Check category filtering
      url = baseUrl + "?category=DEF";
      assertResultsPresentInDataResponse(url, firstDeployment.getId());

      // Check category-not-equals filtering
      url = baseUrl + "?categoryNotEquals=DEF";
      assertResultsPresentInDataResponse(url, secondDeployment.getId());

      // Check tenantId filtering
      url = baseUrl + "?tenantId=myTenant";
      assertResultsPresentInDataResponse(url, secondDeployment.getId());

      // Check tenantId filtering
      url = baseUrl + "?tenantId=unexistingTenant";
      assertResultsPresentInDataResponse(url);

      // Check tenantId like filtering
      url = baseUrl + "?tenantIdLike=" + encode("%enant");
      assertResultsPresentInDataResponse(url, secondDeployment.getId());

      // Check without tenantId filtering
      url = baseUrl + "?withoutTenantId=true";
      assertResultsPresentInDataResponse(url, firstDeployment.getId());

      // Check ordering by name
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=name&order=asc"),
          HttpStatus.SC_OK);
      JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
      closeResponse(response);
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").textValue());

      // Check ordering by deploy time
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=deployTime&order=asc"), HttpStatus.SC_OK);
      dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
      closeResponse(response);
      assertEquals(2L, dataNode.size());
      assertEquals(firstDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(secondDeployment.getId(), dataNode.get(1).get("id").textValue());

      // Check ordering by tenantId
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=tenantId&order=desc"), HttpStatus.SC_OK);
      dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
      closeResponse(response);
      assertEquals(2L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(firstDeployment.getId(), dataNode.get(1).get("id").textValue());

      // Check paging
      response = executeRequest(new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT_COLLECTION) + "?sort=deployTime&order=asc&start=1&size=1"),
          HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      dataNode = responseNode.get("data");
      assertEquals(1L, dataNode.size());
      assertEquals(secondDeployment.getId(), dataNode.get(0).get("id").textValue());
      assertEquals(2L, responseNode.get("total").longValue());
      assertEquals(1L, responseNode.get("start").longValue());
      assertEquals(1L, responseNode.get("size").longValue());

    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<DmnDeployment> deployments = dmnRepositoryService.createDeploymentQuery().list();
      for (DmnDeployment deployment : deployments) {
        dmnRepositoryService.deleteDeployment(deployment.getId());
      }
    }
  }
}
