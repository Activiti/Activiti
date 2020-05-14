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

import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.query.QueryProperty;

/**
 * Contains the possible properties which can be used in a {@link HistoricDetailQuery}.
 *

 */
public class HistoricDetailQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, HistoricDetailQueryProperty> properties = new HashMap<String, HistoricDetailQueryProperty>();

  public static final HistoricDetailQueryProperty PROCESS_INSTANCE_ID = new HistoricDetailQueryProperty("PROC_INST_ID_");
  public static final HistoricDetailQueryProperty VARIABLE_NAME = new HistoricDetailQueryProperty("NAME_");
  public static final HistoricDetailQueryProperty VARIABLE_TYPE = new HistoricDetailQueryProperty("TYPE_");
  public static final HistoricDetailQueryProperty VARIABLE_REVISION = new HistoricDetailQueryProperty("REV_");
  public static final HistoricDetailQueryProperty TIME = new HistoricDetailQueryProperty("TIME_");

  private String name;

  public HistoricDetailQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static HistoricDetailQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
