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
package org.activiti.dmn.engine.impl.audit;

import java.util.Map;

import org.activiti.dmn.api.DecisionExecutionAuditContainer;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DecisionTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yvo Swillens
 */
public class DecisionExecutionAuditUtil {

    private static final Logger logger = LoggerFactory.getLogger(DecisionExecutionAuditUtil.class);

    public static DecisionExecutionAuditContainer initializeRuleExecutionAudit(Decision decision, Map<String, Object> inputVariables) {

        if (decision == null || decision.getId() == null) {

            logger.error("decision does not contain key");
            throw new IllegalArgumentException("decision does not contain key");
        }
        
        DecisionTable decisionTable = (DecisionTable) decision.getExpression();
        
        if (decisionTable.getHitPolicy() == null) {
            logger.error("decision table does not contain a hit policy");
            throw new IllegalArgumentException("decision table does not contain a hit policy");
        }

        String decisionKey  = decision.getId();
        String decisionName  = decision.getName();

        return new DecisionExecutionAuditContainer(decisionKey, decisionName, decisionTable.getHitPolicy(), inputVariables);
    }

}
