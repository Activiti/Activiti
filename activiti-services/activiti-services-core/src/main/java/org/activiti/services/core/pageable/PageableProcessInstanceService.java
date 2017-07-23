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

package org.activiti.services.core.pageable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.converter.ProcessInstanceConverter;
import org.activiti.services.core.pageable.sort.ProcessInstanceSortApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableProcessInstanceService {

    private PageRetriever pageRetriever;

    private RuntimeService runtimeService;

    private ProcessInstanceSortApplier sortApplier;

    private ProcessInstanceConverter processInstanceConverter;

    @Autowired
    public PageableProcessInstanceService(PageRetriever pageRetriever,
                                          RuntimeService runtimeService,
                                          ProcessInstanceSortApplier sortApplier,
                                          ProcessInstanceConverter processInstanceConverter) {
        this.pageRetriever = pageRetriever;
        this.runtimeService = runtimeService;
        this.sortApplier = sortApplier;
        this.processInstanceConverter = processInstanceConverter;
    }

    public Page<ProcessInstance> getProcessInstances(Pageable pageable) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        sortApplier.applySort(query,
                              pageable);
        return pageRetriever.loadPage(query,
                                      pageable,
                                      processInstanceConverter);
    }
}
