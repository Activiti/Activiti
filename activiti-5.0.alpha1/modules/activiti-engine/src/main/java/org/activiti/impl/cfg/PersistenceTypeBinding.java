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
package org.activiti.impl.cfg;

import org.activiti.Configuration;
import org.activiti.impl.xml.Element;


/**
 * Lets you configure what sort of persistence type you have.
 * What this accepts is likely to change dramatically once
 *  we start supporting multiple DB types, and again
 *  when we also support Cloud storage.
 * @author Nick Burch
 */
public class PersistenceTypeBinding implements DefaultProvidingConfigurationBinding {
  
  private static final String TAG_NAME = "persistence-type";
  private static final String CONFIG_NAME = Configuration.NAME_PERSISTENCETYPEISSQL;
  
  /**
   * The default is to be SQL based
   */
  @Override
  public void supplyDefaults(ConfigurationParse configurationParse) {
    configurationParse.configuration.configurationObject(
        CONFIG_NAME, Boolean.TRUE
    );
  }

  public boolean matches(Element element, ConfigurationParse configurationParse) {
    return TAG_NAME.equals(element.getTagName());
  }

  public Object parse(Element element, ConfigurationParse configurationParse) {
    String sql = element.attribute("sql");
    Boolean isSql = Boolean.FALSE;
    
    if(sql == null || "true".equalsIgnoreCase(sql) ||
        "yes".equalsIgnoreCase(sql)) {
      // Is SQL Based
      isSql = Boolean.TRUE;
    }
    
    // Save
    configurationParse.configuration.configurationObject(
        CONFIG_NAME, isSql
    );
    
    // We're not object based
    return null;
  }

}
