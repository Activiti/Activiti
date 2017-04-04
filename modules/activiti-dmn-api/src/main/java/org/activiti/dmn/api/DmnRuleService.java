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
package org.activiti.dmn.api;


import java.util.Map;

/**
 * Service for executing DMN decisions (decision tables)
 * 
 * @author Tijs Rademakers
 * @author Yvo Swillens
 */
public interface DmnRuleService {

/**
  * Execute a decision identified by it's key.
  *
  * @param decisionKey
  *            the decision key, cannot be null
  * @param input
  *            map with input variables
  * @return the {@link RuleEngineExecutionResult} for this execution
  */    
  RuleEngineExecutionResult executeDecisionByKey(String decisionKey, Map<String, Object> input);
  
/**
  * Execute a decision identified by it's key and tenant id.
  *
  * @param decisionKey
  *            the decision key, cannot be null
  * @param input
  *            map with input variables
  * @param tenantId
  *            the tenant id
  * @return the {@link RuleEngineExecutionResult} for this execution
  */
  RuleEngineExecutionResult executeDecisionByKeyAndTenantId(String decisionKey, Map<String, Object> input, String tenantId);
  
/**
  * Execute a decision identified by it's key and parent deployment id.
  *
  * @param decisionKey
  *            the decision key, cannot be null
  * @param parentDeploymentId
  *            the parent deployment id
  * @param input
  *            map with input variables
  * @return the {@link RuleEngineExecutionResult} for this execution
  */
  RuleEngineExecutionResult executeDecisionByKeyAndParentDeploymentId(String decisionKey, String parentDeploymentId, Map<String, Object> input);
  
/**
  * Execute a decision identified by it's key and parent deployment id.
  *
  * @param decisionKey
  *            the decision key, cannot be null
  * @param parentDeploymentId
  *            the parent deployment id
  * @param input
  *            map with input variables
  * @param tenantId
  *            the tenant id
  * @return the {@link RuleEngineExecutionResult} for this execution
  */
  RuleEngineExecutionResult executeDecisionByKeyParentDeploymentIdAndTenantId(String decisionKey, String parentDeploymentId, Map<String, Object> input, String tenantId);
}
