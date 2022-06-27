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


package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.variable.HistoricJPAEntityListVariableType;
import org.activiti.engine.impl.variable.HistoricJPAEntityVariableType;
import org.activiti.engine.impl.variable.JPAEntityListVariableType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;

/**


 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String taskId;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String type;
  protected boolean excludeTaskRelated;

  public HistoricDetailQueryImpl() {
  }

  public HistoricDetailQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDetailQueryImpl id(String id) {
    this.id = id;
    return this;
  }

  public HistoricDetailQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricDetailQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricDetailQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricDetailQueryImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricDetailQueryImpl taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoricDetailQueryImpl formProperties() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQueryImpl variableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }

  public HistoricDetailQueryImpl excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getHistoricDetailEntityManager().findHistoricDetailCountByQueryCriteria(this);
  }

  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<HistoricDetail> historicDetails = commandContext.getHistoricDetailEntityManager().findHistoricDetailsByQueryCriteria(this, page);

    HistoricDetailVariableInstanceUpdateEntity varUpdate = null;
    if (historicDetails != null) {
      for (HistoricDetail historicDetail : historicDetails) {
        if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
          varUpdate = (HistoricDetailVariableInstanceUpdateEntity) historicDetail;

          // Touch byte-array to ensure initialized inside context
          // TODO there should be a generic way to initialize variable
          // values
          varUpdate.getBytes();

          // ACT-863: EntityManagerFactorySession instance needed for
          // fetching value, touch while inside context to store
          // cached value
          if (varUpdate.getVariableType() instanceof JPAEntityVariableType) {
            // Use HistoricJPAEntityVariableType to force caching of
            // value to return from query
            varUpdate.setVariableType(HistoricJPAEntityVariableType.getSharedInstance());
            varUpdate.getValue();
          } else if (varUpdate.getVariableType() instanceof JPAEntityListVariableType) {
            // Use HistoricJPAEntityListVariableType to force
            // caching of list to return from query
            varUpdate.setVariableType(HistoricJPAEntityListVariableType.getSharedInstance());
            varUpdate.getValue();
          }
        }
      }
    }
    return historicDetails;
  }

  // order by
  // /////////////////////////////////////////////////////////////////

  public HistoricDetailQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricDetailQueryImpl orderByTime() {
    orderBy(HistoricDetailQueryProperty.TIME);
    return this;
  }

  public HistoricDetailQueryImpl orderByVariableName() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQueryImpl orderByFormPropertyId() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQueryImpl orderByVariableRevision() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_REVISION);
    return this;
  }

  public HistoricDetailQueryImpl orderByVariableType() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_TYPE);
    return this;
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getType() {
    return type;
  }

  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

}
