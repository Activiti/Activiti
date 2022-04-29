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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;

/**
 * Contains the possible properties that can be used in a {@link TaskQuery}.
 *

 */
public class TaskQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, TaskQueryProperty> properties = new HashMap<String, TaskQueryProperty>();

  public static final TaskQueryProperty TASK_ID = new TaskQueryProperty("RES.ID_");
  public static final TaskQueryProperty NAME = new TaskQueryProperty("RES.NAME_");
  public static final TaskQueryProperty DESCRIPTION = new TaskQueryProperty("RES.DESCRIPTION_");
  public static final TaskQueryProperty PRIORITY = new TaskQueryProperty("RES.PRIORITY_");
  public static final TaskQueryProperty ASSIGNEE = new TaskQueryProperty("RES.ASSIGNEE_");
  public static final TaskQueryProperty OWNER = new TaskQueryProperty("RES.OWNER_");
  public static final TaskQueryProperty CREATE_TIME = new TaskQueryProperty("RES.CREATE_TIME_");
  public static final TaskQueryProperty PROCESS_INSTANCE_ID = new TaskQueryProperty("RES.PROC_INST_ID_");
  public static final TaskQueryProperty EXECUTION_ID = new TaskQueryProperty("RES.EXECUTION_ID_");
  public static final TaskQueryProperty PROCESS_DEFINITION_ID = new TaskQueryProperty("RES.PROC_DEF_ID_");
  public static final TaskQueryProperty DUE_DATE = new TaskQueryProperty("RES.DUE_DATE_");
  public static final TaskQueryProperty TENANT_ID = new TaskQueryProperty("RES.TENANT_ID_");
  public static final TaskQueryProperty TASK_DEFINITION_KEY = new TaskQueryProperty("RES.TASK_DEF_KEY_");

  private String name;

  public TaskQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static TaskQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
