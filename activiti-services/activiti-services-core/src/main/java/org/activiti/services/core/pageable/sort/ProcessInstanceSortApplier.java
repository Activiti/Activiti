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

import org.activiti.engine.impl.ProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceSortApplier extends BaseSortApplier<ProcessInstanceQuery> {

    private Map<String, QueryProperty> orderByProperties = new HashMap<>();

    public ProcessInstanceSortApplier() {
        orderByProperties.put("id", ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
        orderByProperties.put("processDefinitionId", ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    }

    @Override
    protected void applyDefaultSort(ProcessInstanceQuery query) {
        query.orderByProcessInstanceId().asc();
    }

    @Override
    protected QueryProperty getOrderByProperty(Sort.Order order) {
        return orderByProperties.get(order.getProperty());
    }
}
