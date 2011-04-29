/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */

package org.activiti.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryProperty implements QueryProperty {

  private static final Map<String, HistoricTaskInstanceQueryProperty> properties = new HashMap<String, HistoricTaskInstanceQueryProperty>();

  public static final HistoricTaskInstanceQueryProperty HISTORIC_TASK_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("ID_");
  public static final HistoricTaskInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricTaskInstanceQueryProperty("PROC_DEF_ID_");
  public static final HistoricTaskInstanceQueryProperty PROCESS_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("PROC_INST_ID_");
  public static final HistoricTaskInstanceQueryProperty EXECUTION_ID = new HistoricTaskInstanceQueryProperty("EXECUTION_ID_");
  public static final HistoricTaskInstanceQueryProperty TASK_NAME = new HistoricTaskInstanceQueryProperty("NAME_");
  public static final HistoricTaskInstanceQueryProperty TASK_DESCRIPTION = new HistoricTaskInstanceQueryProperty("DESCRIPTION_");
  public static final HistoricTaskInstanceQueryProperty TASK_ASSIGNEE = new HistoricTaskInstanceQueryProperty("ASSIGNEE_");
  public static final HistoricTaskInstanceQueryProperty TASK_OWNER = new HistoricTaskInstanceQueryProperty("OWNER_");
  public static final HistoricTaskInstanceQueryProperty TASK_DEFINITION_KEY = new HistoricTaskInstanceQueryProperty("TASK_DEF_ID_");
  public static final HistoricTaskInstanceQueryProperty DELETE_REASON = new HistoricTaskInstanceQueryProperty("DELETE_REASON_");
  public static final HistoricTaskInstanceQueryProperty START = new HistoricTaskInstanceQueryProperty("START_TIME_");
  public static final HistoricTaskInstanceQueryProperty END = new HistoricTaskInstanceQueryProperty("END_TIME_");
  public static final HistoricTaskInstanceQueryProperty DURATION = new HistoricTaskInstanceQueryProperty("DURATION_");
  public static final HistoricTaskInstanceQueryProperty TASK_PRIORITY = new HistoricTaskInstanceQueryProperty("PRIORITY_");

  private String name;

  public HistoricTaskInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static HistoricTaskInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
