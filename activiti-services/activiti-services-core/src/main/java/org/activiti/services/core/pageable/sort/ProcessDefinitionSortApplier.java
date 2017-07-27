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

package org.activiti.services.core.pageable.sort;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ProcessDefinitionSortApplier extends BaseSortApplier<ProcessDefinitionQuery> {

    private Map<String, QueryProperty> orderByProperties = new HashMap<>();

    public ProcessDefinitionSortApplier() {
        orderByProperties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
        orderByProperties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
        orderByProperties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
    }

    @Override
    protected void applyDefaultSort(ProcessDefinitionQuery query) {
        query.orderByProcessDefinitionId().asc();
    }

    @Override
    protected QueryProperty getOrderByProperty(Sort.Order order) {
        return orderByProperties.get(order.getProperty());
    }

}
