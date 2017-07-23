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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.converter.ProcessDefinitionConverter;
import org.activiti.services.core.pageable.sort.ProcessDefinitionSortApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableRepositoryService {

    private final RepositoryService repositoryService;

    private final PageRetriever pageRetriever;

    private final ProcessDefinitionConverter processDefinitionConverter;

    private final ProcessDefinitionSortApplier sortApplier;

    @Autowired
    public PageableRepositoryService(RepositoryService repositoryService,
                                     PageRetriever pageRetriever,
                                     ProcessDefinitionConverter processDefinitionConverter,
                                     ProcessDefinitionSortApplier sortApplier) {
        this.repositoryService = repositoryService;
        this.pageRetriever = pageRetriever;
        this.processDefinitionConverter = processDefinitionConverter;
        this.sortApplier = sortApplier;
    }

    public Page<ProcessDefinition> getProcessDefinitions(Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        sortApplier.applySort(query,
                              pageable);
        return pageRetriever.loadPage(query,
                                      pageable,
                                      processDefinitionConverter);
    }
}
