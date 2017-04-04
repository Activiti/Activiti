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
public class DecisionTableModelResourceTest extends BaseSpringDmnRestTestCase {

  @DmnDeploymentAnnotation(resources = { "org/activiti/rest/dmn/service/api/repository/simple.dmn" })
  public void testGetDecisionTableModel() throws Exception {

    DmnDecisionTable decisionTable = dmnRepositoryService.createDecisionTableQuery().singleResult();

    HttpGet httpGet = new HttpGet(SERVER_URL_PREFIX + DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DECISION_TABLE_MODEL, decisionTable.getId()));
    CloseableHttpResponse response = executeRequest(httpGet, HttpStatus.SC_OK);

    // Check "OK" status
    JsonNode resultNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(resultNode);
    JsonNode firstDecision = resultNode.get("decisions").get(0);
    assertNotNull(firstDecision);
    
    JsonNode decisionTableNode = firstDecision.get("expression");
    assertEquals("decisionTable", decisionTableNode.get("id").textValue());
  }
}
