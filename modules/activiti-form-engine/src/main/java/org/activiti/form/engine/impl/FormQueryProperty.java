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

package org.activiti.form.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.form.api.QueryProperty;

/**
 * Contains the possible properties that can be used in a {@link ProcessDefinitionQuery}.
 * 
 * @author Joram Barrez
 */
public class FormQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, FormQueryProperty> properties = new HashMap<String, FormQueryProperty>();

  public static final FormQueryProperty FORM_DEFINITION_KEY = new FormQueryProperty("RES.KEY_");
  public static final FormQueryProperty FORM_CATEGORY = new FormQueryProperty("RES.CATEGORY_");
  public static final FormQueryProperty FORM_ID = new FormQueryProperty("RES.ID_");
  public static final FormQueryProperty FORM_VERSION = new FormQueryProperty("RES.VERSION_");
  public static final FormQueryProperty FORM_NAME = new FormQueryProperty("RES.NAME_");
  public static final FormQueryProperty DEPLOYMENT_ID = new FormQueryProperty("RES.DEPLOYMENT_ID_");
  public static final FormQueryProperty FORM_TENANT_ID = new FormQueryProperty("RES.TENANT_ID_");

  private String name;

  public FormQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static FormQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
