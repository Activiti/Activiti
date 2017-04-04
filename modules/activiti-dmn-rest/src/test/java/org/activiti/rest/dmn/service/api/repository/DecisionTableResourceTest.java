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

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.engine.test.DmnDeploymentAnnotation;
import org.activiti.rest.dmn.service.api.BaseSpringDmnRestTestCase;
import org.activiti.rest.dmn.service.api.DmnRestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 */
public class DecisionTableResourceTest extends BaseSpringDmnRestTestCase {

  @DmnDeploymentAnnotation(resources = { "org/activiti/rest/dmn/service/api/repository/simple.dmn" })
  public void testGetDecisionTable() throws Exception {

    DmnDecisionTable decisionTable = dmnRepositoryService.createDecisionTableQuery().singleResult();

    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DECISION_TABLE, decisionTable.getId()));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertEquals(decisionTable.getId(), responseNode.get("id").textValue());
    assertEquals(decisionTable.getKey(), responseNode.get("key").textValue());
    assertEquals(decisionTable.getCategory(), responseNode.get("category").textValue());
    assertEquals(decisionTable.getVersion(), responseNode.get("version").intValue());
    assertEquals(decisionTable.getDescription(), responseNode.get("description").textValue());
    assertEquals(decisionTable.getName(), responseNode.get("name").textValue());

    // Check URL's
    assertEquals(httpGet.getURI().toString(), responseNode.get("url").asText());
    assertEquals(decisionTable.getDeploymentId(), responseNode.get("deploymentId").textValue());
  }

  @DmnDeploymentAnnotation(resources = { "org/activiti/rest/dmn/service/api/repository/simple.dmn" })
  public void testGetUnexistingDecisionTable() throws Exception {
    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DECISION_TABLE, "unexisting"));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }

}
