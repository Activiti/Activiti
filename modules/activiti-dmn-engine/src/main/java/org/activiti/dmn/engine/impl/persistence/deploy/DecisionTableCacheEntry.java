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
package org.activiti.dmn.engine.impl.persistence.deploy;

import java.io.Serializable;

import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DmnDefinition;

/**
 * @author Tijs Rademakers
 */
public class DecisionTableCacheEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DecisionTableEntity decisionTableEntity;
    protected DmnDefinition dmnDefinition;
    protected Decision decision;

    public DecisionTableCacheEntry(DecisionTableEntity decisionTableEntity, DmnDefinition dmnDefinition, Decision decision) {
        this.decisionTableEntity = decisionTableEntity;
        this.dmnDefinition = dmnDefinition;
        this.decision = decision;
    }

    public DecisionTableEntity getDecisionTableEntity() {
        return decisionTableEntity;
    }

    public void setDecisionTableEntity(DecisionTableEntity decisionTable) {
        this.decisionTableEntity = decisionTableEntity;
    }

    public DmnDefinition getDmnDefinition() {
        return dmnDefinition;
    }

    public void setDmnDefinition(DmnDefinition dmnDefinition) {
        this.dmnDefinition = dmnDefinition;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }
}
