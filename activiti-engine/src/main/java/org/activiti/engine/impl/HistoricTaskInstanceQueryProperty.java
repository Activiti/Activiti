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

 */
public class HistoricTaskInstanceQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, HistoricTaskInstanceQueryProperty> properties = new HashMap<String, HistoricTaskInstanceQueryProperty>();

  public static final HistoricTaskInstanceQueryProperty HISTORIC_TASK_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("RES.ID_");
  public static final HistoricTaskInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricTaskInstanceQueryProperty("RES.PROC_DEF_ID_");
  public static final HistoricTaskInstanceQueryProperty PROCESS_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("RES.PROC_INST_ID_");
  public static final HistoricTaskInstanceQueryProperty EXECUTION_ID = new HistoricTaskInstanceQueryProperty("RES.EXECUTION_ID_");
  public static final HistoricTaskInstanceQueryProperty TASK_NAME = new HistoricTaskInstanceQueryProperty("RES.NAME_");
  public static final HistoricTaskInstanceQueryProperty TASK_DESCRIPTION = new HistoricTaskInstanceQueryProperty("RES.DESCRIPTION_");
  public static final HistoricTaskInstanceQueryProperty TASK_ASSIGNEE = new HistoricTaskInstanceQueryProperty("RES.ASSIGNEE_");
  public static final HistoricTaskInstanceQueryProperty TASK_OWNER = new HistoricTaskInstanceQueryProperty("RES.OWNER_");
  public static final HistoricTaskInstanceQueryProperty TASK_DEFINITION_KEY = new HistoricTaskInstanceQueryProperty("RES.TASK_DEF_KEY_");
  public static final HistoricTaskInstanceQueryProperty DELETE_REASON = new HistoricTaskInstanceQueryProperty("RES.DELETE_REASON_");
  public static final HistoricTaskInstanceQueryProperty START = new HistoricTaskInstanceQueryProperty("RES.START_TIME_");
  public static final HistoricTaskInstanceQueryProperty END = new HistoricTaskInstanceQueryProperty("RES.END_TIME_");
  public static final HistoricTaskInstanceQueryProperty DURATION = new HistoricTaskInstanceQueryProperty("RES.DURATION_");
  public static final HistoricTaskInstanceQueryProperty TASK_PRIORITY = new HistoricTaskInstanceQueryProperty("RES.PRIORITY_");
  public static final HistoricTaskInstanceQueryProperty TASK_DUE_DATE = new HistoricTaskInstanceQueryProperty("RES.DUE_DATE_");
  public static final HistoricTaskInstanceQueryProperty TENANT_ID_ = new HistoricTaskInstanceQueryProperty("RES.TENANT_ID_");

  public static final HistoricTaskInstanceQueryProperty INCLUDED_VARIABLE_TIME = new HistoricTaskInstanceQueryProperty("VAR.LAST_UPDATED_TIME_");

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
