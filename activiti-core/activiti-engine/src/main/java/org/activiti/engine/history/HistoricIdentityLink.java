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


package org.activiti.engine.history;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;

/**
 * Historic counterpart of {@link IdentityLink} that represents the current state if any runtime link. Will be preserved when the runtime process instance or task is finished.
 *
 */
@Internal
public interface HistoricIdentityLink {

  /**
   * Returns the type of link. See {@link IdentityLinkType} for the native supported types by Activiti.
   */
  String getType();

  /**
   * If the identity link involves a user, then this will be a non-null id of a user. That userId can be used to query for user information through the {@link UserQuery} API.
   */
  String getUserId();

  /**
   * If the identity link involves a group, then this will be a non-null id of a group. That groupId can be used to query for user information through the {@link GroupQuery} API.
   */
  String getGroupId();

  /**
   * The id of the task associated with this identity link.
   */
  String getTaskId();

  /**
   * The id of the process instance associated with this identity link.
   */
  String getProcessInstanceId();
}
