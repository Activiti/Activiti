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
package org.activiti.engine.test.api.task;

import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;

public class DelegateTaskTestTaskListener implements TaskListener {

  public static final String VARNAME_CANDIDATE_USERS = "candidateUsers";
  public static final String VARNAME_CANDIDATE_GROUPS = "candidateGroups";

  public void notify(DelegateTask delegateTask) {
    Set<IdentityLink> candidates = delegateTask.getCandidates();
    Set<String> candidateUsers = new HashSet<String>();
    Set<String> candidateGroups = new HashSet<String>();
    for (IdentityLink candidate : candidates) {
      if (candidate.getUserId() != null) {
        candidateUsers.add(candidate.getUserId());
      } else if (candidate.getGroupId() != null) {
        candidateGroups.add(candidate.getGroupId());
      }
    }
    delegateTask.setVariable(VARNAME_CANDIDATE_USERS, candidateUsers);
    delegateTask.setVariable(VARNAME_CANDIDATE_GROUPS, candidateGroups);
  }

}
