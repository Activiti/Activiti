/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.task;

import org.activiti.engine.api.internal.Internal;

import java.util.List;

/**
 * Allows programmatic querying of {@link Task}s;
 *
 */
@Internal
public interface TaskQuery extends TaskInfoQuery<TaskQuery, Task> {

  /** Only select tasks which don't have an assignee. */
  TaskQuery taskUnassigned();

  /** Only select tasks with the given {@link DelegationState}. */
  TaskQuery taskDelegationState(DelegationState delegationState);

  /**
   * Select tasks that has been claimed or assigned to user or waiting to claim by user (candidate user or groups). You can invoke {@link #taskCandidateGroupIn(List)} to include tasks that can be
   * claimed by a user in the given groups while set property <strong>dbIdentityUsed</strong> to <strong>false</strong> in process engine configuration or using custom session factory of
   * GroupIdentityManager.
   */
  TaskQuery taskCandidateOrAssigned(String userIdForCandidateAndAssignee);

  /**
   * Select tasks that has been claimed or assigned to user or waiting to claim by user (candidate user or groups).
   */
  TaskQuery taskCandidateOrAssigned(String userIdForCandidateAndAssignee, List<String> usersGroups);

  /** Only select tasks that have no parent (i.e. do not select subtasks). */
  TaskQuery excludeSubtasks();

  /**
   * Only selects tasks which are suspended, because its process instance was suspended.
   */
  TaskQuery suspended();

  /**
   * Only selects tasks which are active (ie. not suspended)
   */
  TaskQuery active();
}
