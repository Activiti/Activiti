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

import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.query.QueryProperty;


/**
 * Contains the possible properties which can be used in a {@link HistoricActivityInstanceQuery}.
 * 
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, HistoricActivityInstanceQueryProperty> properties = new HashMap<String, HistoricActivityInstanceQueryProperty>();

  public static final HistoricActivityInstanceQueryProperty HISTORIC_ACTIVITY_INSTANCE_ID = new HistoricActivityInstanceQueryProperty("ID_");
  public static final HistoricActivityInstanceQueryProperty PROCESS_INSTANCE_ID = new HistoricActivityInstanceQueryProperty("PROC_INST_ID_");
  public static final HistoricActivityInstanceQueryProperty EXECUTION_ID = new HistoricActivityInstanceQueryProperty("EXECUTION_ID_");
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_ID = new HistoricActivityInstanceQueryProperty("ACT_ID_");
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_NAME = new HistoricActivityInstanceQueryProperty("ACT_NAME_");
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_TYPE = new HistoricActivityInstanceQueryProperty("ACT_TYPE_");
  public static final HistoricActivityInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricActivityInstanceQueryProperty("PROC_DEF_ID_");
  public static final HistoricActivityInstanceQueryProperty START = new HistoricActivityInstanceQueryProperty("START_TIME_");
  public static final HistoricActivityInstanceQueryProperty END = new HistoricActivityInstanceQueryProperty("END_TIME_");
  public static final HistoricActivityInstanceQueryProperty DURATION = new HistoricActivityInstanceQueryProperty("DURATION_");
  public static final HistoricActivityInstanceQueryProperty TENANT_ID = new HistoricActivityInstanceQueryProperty("TENANT_ID_");

  private String name;

  public HistoricActivityInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static HistoricActivityInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
