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

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.engine.test.DmnDeploymentAnnotation;
import org.activiti.rest.dmn.service.api.BaseSpringDmnRestTestCase;
import org.activiti.rest.dmn.service.api.DmnRestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 */
public class DmnDeploymentResourceTest extends BaseSpringDmnRestTestCase {

  /**
   * Test getting a single deployment. GET dmn-repository/deployments/{deploymentId}
   */
  @DmnDeploymentAnnotation(resources = { "org/activiti/rest/dmn/service/api/repository/simple.dmn" })
  public void testGetDeployment() throws Exception {
    DmnDeployment existingDeployment = dmnRepositoryService.createDeploymentQuery().singleResult();

    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, existingDeployment.getId()));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
    closeResponse(response);

    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());

    String deploymentId = responseNode.get("id").textValue();
    String name = responseNode.get("name").textValue();
    String category = responseNode.get("category").textValue();
    String deployTime = responseNode.get("deploymentTime").textValue();
    String url = responseNode.get("url").textValue();
    String tenantId = responseNode.get("tenantId").textValue();

    assertEquals("", tenantId);
    assertNotNull(deploymentId);
    assertEquals(existingDeployment.getId(), deploymentId);

    assertNotNull(name);
    assertEquals(existingDeployment.getName(), name);

    assertEquals(existingDeployment.getCategory(), category);

    assertNotNull(deployTime);

    assertNotNull(url);
    assertTrue(url.endsWith(DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, deploymentId)));
  }

  /**
   * Test getting an unexisting deployment. GET dmn-repository/deployments/{deploymentId}
   */
  public void testGetUnexistingDeployment() throws Exception {
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, "unexisting"));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }

  /**
   * Test deleting a single deployment. DELETE dmn-repository/deployments/{deploymentId}
   */
  @DmnDeploymentAnnotation(resources = { "org/activiti/rest/dmn/service/api/repository/simple.dmn" })
  public void testDeleteDeployment() throws Exception {
    dmnRepositoryService.createDeploymentQuery().singleResult();
    DmnDeployment existingDeployment = dmnRepositoryService.createDeploymentQuery().singleResult();
    assertNotNull(existingDeployment);

    // Delete the deployment
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, existingDeployment.getId()));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);

    existingDeployment = dmnRepositoryService.createDeploymentQuery().singleResult();
    assertNull(existingDeployment);
  }

  /**
   * Test deleting an unexisting deployment. DELETE dmn-repository/deployments/{deploymentId}
   */
  public void testDeleteUnexistingDeployment() throws Exception {
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DEPLOYMENT, "unexisting"));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }

}
