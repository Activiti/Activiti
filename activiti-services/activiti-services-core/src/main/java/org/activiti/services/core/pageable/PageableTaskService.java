/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.core.pageable;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.UserGroupLookupProxy;
import org.activiti.engine.task.TaskQuery;
import org.activiti.services.core.AuthenticationWrapper;
import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.converter.TaskConverter;
import org.activiti.services.core.pageable.sort.TaskSortApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableTaskService {

    private final TaskService taskService;
    private final TaskConverter taskConverter;
    private final PageRetriever pageRetriever;
    private final TaskSortApplier sortApplier;

    @Autowired(required = false)
    private UserGroupLookupProxy userGroupLookupProxy;

    private AuthenticationWrapper authenticationWrapper = new AuthenticationWrapper();

    @Autowired
    public PageableTaskService(TaskService taskService,
                               TaskConverter taskConverter,
                               PageRetriever pageRetriever,
                               TaskSortApplier sortApplier) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.pageRetriever = pageRetriever;
        this.sortApplier = sortApplier;
    }

    public Page<Task> getTasks(Pageable pageable) {

        String userId = authenticationWrapper.getAuthenticatedUserId();
        TaskQuery query = taskService.createTaskQuery();
        if (userId != null) {
            List<String> groups = null;
            if (userGroupLookupProxy != null) {
                groups = userGroupLookupProxy.getGroupsForCandidateUser(userId);
            }
            query = query.taskCandidateOrAssigned(userId,
                                                  groups);
        }
        sortApplier.applySort(query,
                              pageable);
        return pageRetriever.loadPage(query,
                                      pageable,
                                      taskConverter);
    }

    public Page<Task> getTasks(String processInstanceId,
                               Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery().processInstanceId(processInstanceId);
        sortApplier.applySort(query,
                              pageable);
        return pageRetriever.loadPage(query,
                                      pageable,
                                      taskConverter);
    }

    public UserGroupLookupProxy getUserGroupLookupProxy() {
        return userGroupLookupProxy;
    }

    public void setUserGroupLookupProxy(UserGroupLookupProxy userGroupLookupProxy) {
        this.userGroupLookupProxy = userGroupLookupProxy;
    }

    public AuthenticationWrapper getAuthenticationWrapper() {
        return authenticationWrapper;
    }

    public void setAuthenticationWrapper(AuthenticationWrapper authenticationWrapper) {
        this.authenticationWrapper = authenticationWrapper;
    }
}
