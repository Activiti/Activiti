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
package org.activiti.rest.dmn.service.api.decision;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yvo Swillens
 */
@RestController
public class DecisionExecutorResource extends BaseDecisionExecutorResource {

  @RequestMapping(value = "/rules/decision-executor", method = RequestMethod.POST, produces = "application/json")
  public ExecuteDecisionResponse executeDecision(@RequestBody ExecuteDecisionRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {

    if (request.getDecisionKey() == null) {
      throw new ActivitiDmnIllegalArgumentException("Decision key is required.");
    }

    Map<String, Object> inputVariables = null;
    if (request.getInputVariables() != null) {
      inputVariables = new HashMap<String, Object>();
      for (Map.Entry<String, Object> variable : request.getInputVariables().entrySet()) {
        if (variable.getKey() == null) {
          throw new ActivitiDmnIllegalArgumentException("Variable name is required.");
        }
        inputVariables.put(variable.getKey(), variable.getValue());
      }
    }

    try {
      RuleEngineExecutionResult executionResult = executeDecisionByKeyAndTenantId(request.getDecisionKey(), request.getTenantId(), request.getInputVariables());

      response.setStatus(HttpStatus.CREATED.value());

      return dmnRestResponseFactory.createExecuteDecisionResponse(executionResult);

      // TODO: add audit trail info
    } catch (ActivitiDmnObjectNotFoundException aonfe) {
      throw new ActivitiDmnIllegalArgumentException(aonfe.getMessage(), aonfe);
    }
  }
}
