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
package org.activiti.rest.api.process;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.ProcessInstanceQueryProperty;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

import java.util.List;
import java.util.Map;

public class ProcessInstancesGet extends ActivitiPagingWebScript {

    public ProcessInstancesGet() {
        properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
        properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
        properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
        properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);

    }

    @Override
    @SuppressWarnings("unchecked")
    protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
        paginateList(req, createQuery(req), "processInstances", model, "id");


    }
    /*
         * We are using HistoricProcessInstanceQuery but return only active processes
     */
    private HistoricProcessInstanceQuery createQuery(ActivitiRequest req) {
        HistoricProcessInstanceQuery query = getHistoryService().createHistoricProcessInstanceQuery();
        query = query.unfinished();
        String processDefinitionId = req.getString("processDefinitionId", null);
        String processInstanceKey = req.getString("businessKey", null);
        query = processDefinitionId == null ? query : query.processDefinitionId(processDefinitionId);
        query = processInstanceKey == null ? query : query.processInstanceBusinessKey(processInstanceKey);
        return query;
    }


}

