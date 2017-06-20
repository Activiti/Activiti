/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.sort;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**

 */
@Component
public class ProcessDefinitionSortApplier extends BaseSortApplier<ProcessDefinitionQuery> {

    protected void applyDefaultSort(ProcessDefinitionQuery query) {
        query.orderByProcessDefinitionId().asc();
    }

    protected void applyOrder(ProcessDefinitionQuery query, Sort.Order order) {
        if (order.getProperty().equals(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID.getName())) {
            query.orderByProcessDefinitionId();
        } else if (order.getProperty().equals(ProcessDefinitionQueryProperty.DEPLOYMENT_ID.getName())) {
            query.orderByDeploymentId();
        } else {
            throw new ActivitiIllegalArgumentException("The property '" + order.getProperty() + "' cannot be used to sort the result.");
        }
    }

}
