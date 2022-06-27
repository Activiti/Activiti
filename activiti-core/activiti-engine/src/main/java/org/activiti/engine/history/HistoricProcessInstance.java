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

import java.util.Date;
import java.util.Map;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * A single execution of a whole process definition that is stored permanently.
 *
 */
@Internal
public interface HistoricProcessInstance {

  /**
   * The process instance id (== as the id for the runtime {@link ProcessInstance process instance}).
   */
  String getId();

  /** The user provided unique reference to this process instance. */
  String getBusinessKey();

  /** The process definition reference. */
  String getProcessDefinitionId();

  /** The name of the process definition of the process instance. */
  String getProcessDefinitionName();

  /** The key of the process definition of the process instance. */
  String getProcessDefinitionKey();

  /** The version of the process definition of the process instance. */
  Integer getProcessDefinitionVersion();

  /**
   * The deployment id of the process definition of the process instance.
   */
  String getDeploymentId();

  /** The time the process was started. */
  Date getStartTime();

  /** The time the process was ended. */
  Date getEndTime();

  /**
   * The difference between {@link #getEndTime()} and {@link #getStartTime()} .
   */
  Long getDurationInMillis();

  /**
   * Reference to the activity in which this process instance ended. Note that a process instance can have multiple end events, in this case it might not be deterministic which activity id will be
   * referenced here. Use a {@link HistoricActivityInstanceQuery} instead to query for end events of the process instance (use the activityTYpe attribute)
   * */
  String getEndActivityId();

  /**
   * The authenticated user that started this process instance.
   *
   * @see IdentityService#setAuthenticatedUserId(String)
   */
  String getStartUserId();

  /** The start activity. */
  String getStartActivityId();

  /** Obtains the reason for the process instance's deletion. */
  String getDeleteReason();

  /**
   * The process instance id of a potential super process instance or null if no super process instance exists
   */
  String getSuperProcessInstanceId();

  /**
   * The tenant identifier for the process instance.
   */
  String getTenantId();

  /**
   * The name for the process instance.
   */
  String getName();

  /**
   * The description for the process instance.
   */
  String getDescription();

  /** Returns the process variables if requested in the process instance query */
  Map<String, Object> getProcessVariables();
}
