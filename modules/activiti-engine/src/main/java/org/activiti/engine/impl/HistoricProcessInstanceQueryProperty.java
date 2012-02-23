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
 * Contains the possible properties which can be used in a {@link HistoricProcessInstanceQueryProperty}.
 * 
 * @author Joram Barrez
 */
public class HistoricProcessInstanceQueryProperty implements QueryProperty {
  
  private static final long serialVersionUID = 1L;

  private static final Map<String, HistoricProcessInstanceQueryProperty> properties = new HashMap<String, HistoricProcessInstanceQueryProperty>();

  public static final HistoricProcessInstanceQueryProperty PROCESS_INSTANCE_ID_ = new HistoricProcessInstanceQueryProperty("PROC_INST_ID_");
  public static final HistoricProcessInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricProcessInstanceQueryProperty("PROC_DEF_ID_");
  public static final HistoricProcessInstanceQueryProperty BUSINESS_KEY = new HistoricProcessInstanceQueryProperty("BUSINESS_KEY_");
  public static final HistoricProcessInstanceQueryProperty START_TIME = new HistoricProcessInstanceQueryProperty("START_TIME_");
  public static final HistoricProcessInstanceQueryProperty END_TIME = new HistoricProcessInstanceQueryProperty("END_TIME_");
  public static final HistoricProcessInstanceQueryProperty DURATION = new HistoricProcessInstanceQueryProperty("DURATION_");
  
  private String name;

  public HistoricProcessInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static HistoricProcessInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
