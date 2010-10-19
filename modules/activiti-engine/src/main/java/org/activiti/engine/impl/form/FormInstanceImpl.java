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

package org.activiti.engine.impl.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.form.FormInstance;


/**
 * @author Tom Baeyens
 */
public abstract class FormInstanceImpl implements FormInstance, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String formKey;
  protected String deploymentId;
  protected Map<String, Object> properties = new HashMap<String, Object>();
  
  public Object getProperty(String propertyName) {
    return properties.get(propertyName);
  }
  
  public void setProperty(String propertyName, Object propertyValue) {
    properties.put(propertyName, propertyValue);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getFormKey() {
    return formKey;
  }
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
  public Map<String, Object> getProperties() {
    return properties;
  }
  public String getDeploymentId() {
    return deploymentId;
  }
}
