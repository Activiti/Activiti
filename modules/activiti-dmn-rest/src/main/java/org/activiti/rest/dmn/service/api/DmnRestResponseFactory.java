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
package org.activiti.rest.dmn.service.api;

import java.util.ArrayList;
import java.util.List;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.rest.dmn.service.api.decision.ExecuteDecisionResponse;
import org.activiti.rest.dmn.service.api.repository.DecisionTableResponse;
import org.activiti.rest.dmn.service.api.repository.DmnDeploymentResponse;

/**
 *
 * Default implementation of a {@link DmnRestResponseFactory}.
 *
 * @author Yvo Swillens
 */
public class DmnRestResponseFactory {

  public DecisionTableResponse createDecisionTableResponse(DmnDecisionTable decisionTable) {
    return createDecisionTableResponse(decisionTable, createUrlBuilder());
  }

  public DecisionTableResponse createDecisionTableResponse(DmnDecisionTable decisionTable, DmnRestUrlBuilder urlBuilder) {
    DecisionTableResponse response = new DecisionTableResponse(decisionTable);
    response.setUrl(urlBuilder.buildUrl(DmnRestUrls.URL_DECISION_TABLE, decisionTable.getId()));

    return response;
  }

  public List<DecisionTableResponse> createDecisionTableResponseList(List<DmnDecisionTable> decisionTables) {
    DmnRestUrlBuilder urlBuilder = createUrlBuilder();
    List<DecisionTableResponse> responseList = new ArrayList<>();
    for (DmnDecisionTable instance : decisionTables) {
      responseList.add(createDecisionTableResponse(instance, urlBuilder));
    }
    return responseList;
  }

  public List<DmnDeploymentResponse> createDmnDeploymentResponseList(List<DmnDeployment> deployments) {
    DmnRestUrlBuilder urlBuilder = createUrlBuilder();
    List<DmnDeploymentResponse> responseList = new ArrayList<>();
    for (DmnDeployment instance : deployments) {
      responseList.add(createDmnDeploymentResponse(instance, urlBuilder));
    }
    return responseList;
  }

  public DmnDeploymentResponse createDmnDeploymentResponse(DmnDeployment deployment) {
    return createDmnDeploymentResponse(deployment, createUrlBuilder());
  }

  public DmnDeploymentResponse createDmnDeploymentResponse(DmnDeployment deployment, DmnRestUrlBuilder urlBuilder) {
    return new DmnDeploymentResponse(deployment, urlBuilder.buildUrl(DmnRestUrls.URL_DEPLOYMENT, deployment.getId()));
  }

  public ExecuteDecisionResponse createExecuteDecisionResponse(RuleEngineExecutionResult executionResult) {
    return createExecuteDecisionResponse(executionResult, createUrlBuilder());
  }

  public ExecuteDecisionResponse createExecuteDecisionResponse(RuleEngineExecutionResult executionResult, DmnRestUrlBuilder urlBuilder) {
    ExecuteDecisionResponse response = new ExecuteDecisionResponse(executionResult);
    response.setUrl(urlBuilder.buildUrl(DmnRestUrls.URL_DECISION_EXECUTOR));

    return response;
  }

  protected DmnRestUrlBuilder createUrlBuilder() {
    return DmnRestUrlBuilder.fromCurrentRequest();
  }
}
