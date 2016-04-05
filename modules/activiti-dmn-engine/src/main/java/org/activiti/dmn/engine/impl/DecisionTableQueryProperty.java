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

package org.activiti.dmn.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.engine.query.QueryProperty;

/**
 * Contains the possible properties that can be used in a {@link ProcessDefinitionQuery}.
 * 
 * @author Joram Barrez
 */
public class DecisionTableQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, DecisionTableQueryProperty> properties = new HashMap<String, DecisionTableQueryProperty>();

  public static final DecisionTableQueryProperty DECISION_TABLE_KEY = new DecisionTableQueryProperty("RES.KEY_");
  public static final DecisionTableQueryProperty DECISION_TABLE_CATEGORY = new DecisionTableQueryProperty("RES.CATEGORY_");
  public static final DecisionTableQueryProperty DECISION_TABLE_ID = new DecisionTableQueryProperty("RES.ID_");
  public static final DecisionTableQueryProperty DECISION_TABLE_VERSION = new DecisionTableQueryProperty("RES.VERSION_");
  public static final DecisionTableQueryProperty DECISION_TABLE_NAME = new DecisionTableQueryProperty("RES.NAME_");
  public static final DecisionTableQueryProperty DEPLOYMENT_ID = new DecisionTableQueryProperty("RES.DEPLOYMENT_ID_");
  public static final DecisionTableQueryProperty DECISION_TABLE_TENANT_ID = new DecisionTableQueryProperty("RES.TENANT_ID_");

  private String name;

  public DecisionTableQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static DecisionTableQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
