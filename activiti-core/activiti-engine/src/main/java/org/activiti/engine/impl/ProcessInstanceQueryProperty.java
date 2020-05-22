/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
import org.activiti.engine.runtime.ProcessInstanceQuery;

/**
 * Contains the possible properties that can be used in a {@link ProcessInstanceQuery}.
 *

 */
public class ProcessInstanceQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, ProcessInstanceQueryProperty> properties = new HashMap<String, ProcessInstanceQueryProperty>();

  public static final ProcessInstanceQueryProperty PROCESS_INSTANCE_ID = new ProcessInstanceQueryProperty("RES.ID_");
  public static final ProcessInstanceQueryProperty PROCESS_DEFINITION_KEY = new ProcessInstanceQueryProperty("ProcessDefinitionKey");
  public static final ProcessInstanceQueryProperty PROCESS_DEFINITION_ID = new ProcessInstanceQueryProperty("ProcessDefinitionId");
  public static final ProcessInstanceQueryProperty TENANT_ID = new ProcessInstanceQueryProperty("RES.TENANT_ID_");

  private String name;

  public ProcessInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static ProcessInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
