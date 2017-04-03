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

import java.util.List;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.api.DmnDeployment;
import org.activiti.rest.dmn.service.api.BaseSpringDmnRestTestCase;
import org.activiti.rest.dmn.service.api.DmnRestUrls;

/**
 * @author Yvo Swillens
 */
public class DecisionTableCollectionResourceTest extends BaseSpringDmnRestTestCase {

  /**
   * Test getting deployments. GET dmn-repository/deployments
   */
  public void testGetDecisionTables() throws Exception {

    try {
      DmnDeployment firstDeployment = dmnRepositoryService.createDeployment().name("Deployment 1").addClasspathResource("org/activiti/rest/dmn/service/api/repository/simple.dmn").category("cat one")
          .deploy();

      DmnDeployment secondDeployment = dmnRepositoryService.createDeployment().name("Deployment 2").addClasspathResource("org/activiti/rest/dmn/service/api/repository/simple.dmn").category("cat two")
          .addClasspathResource("org/activiti/rest/dmn/service/api/repository/simple-2.dmn").deploy();

      DmnDecisionTable firstDecision = dmnRepositoryService.createDecisionTableQuery().decisionTableKey("decision").deploymentId(firstDeployment.getId()).singleResult();

      DmnDecisionTable latestDecision = dmnRepositoryService.createDecisionTableQuery().decisionTableKey("decision").deploymentId(secondDeployment.getId()).singleResult();

      DmnDecisionTable decisionTwo = dmnRepositoryService.createDecisionTableQuery().decisionTableKey("decisionTwo").deploymentId(secondDeployment.getId()).singleResult();

      String baseUrl = DmnRestUrls.createRelativeResourceUrl(DmnRestUrls.URL_DECISION_TABLE_COLLETION);
      assertResultsPresentInDataResponse(baseUrl, firstDecision.getId(), decisionTwo.getId(), latestDecision.getId());

      // Verify

      // Test name filtering
      String url = baseUrl + "?name=" + encode("Full Decision Two");
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test nameLike filtering
      url = baseUrl + "?nameLike=" + encode("Full Decision Tw%");
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test key filtering
      url = baseUrl + "?key=decisionTwo";
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test returning multiple versions for the same key
      url = baseUrl + "?key=decision";
      assertResultsPresentInDataResponse(url, firstDecision.getId(), latestDecision.getId());

      // Test keyLike filtering
      url = baseUrl + "?keyLike=" + encode("%Two");
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test resourceName filtering
      url = baseUrl + "?resourceName=org/activiti/rest/dmn/service/api/repository/simple-2.dmn";
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test resourceNameLike filtering
      url = baseUrl + "?resourceNameLike=" + encode("%simple-2%");
      assertResultsPresentInDataResponse(url, decisionTwo.getId());

      // Test version filtering
      url = baseUrl + "?version=2";
      assertResultsPresentInDataResponse(url, latestDecision.getId());

      // Test latest filtering
      url = baseUrl + "?latest=true";
      assertResultsPresentInDataResponse(url, latestDecision.getId(), decisionTwo.getId());
      url = baseUrl + "?latest=false";
      assertResultsPresentInDataResponse(baseUrl, firstDecision.getId(), latestDecision.getId(), decisionTwo.getId());

      // Test deploymentId
      url = baseUrl + "?deploymentId=" + secondDeployment.getId();
      assertResultsPresentInDataResponse(url, latestDecision.getId(), decisionTwo.getId());
    } finally {
      // Always cleanup any created deployments, even if the test failed
      List<DmnDeployment> deployments = dmnRepositoryService.createDeploymentQuery().list();
      for (DmnDeployment deployment : deployments) {
        dmnRepositoryService.deleteDeployment(deployment.getId());
      }
    }
  }
}
