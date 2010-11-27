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

package org.activiti.rest.builder;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for implementing a {@link JSONObjectBuilder}. Adds utility methods
 * for easy manipulation of JSON.
 * 
 * @author Frederik Heremans
 */
public abstract class BaseJSONObjectBuilder implements JSONObjectBuilder {

  protected String templateName;

  public abstract JSONObject createJsonObject(Object model) throws JSONException;
  
  @SuppressWarnings("unchecked")
  protected Map<String, Object> getModelAsMap(Object model) {
    if(model instanceof Map<?, ?>) {
      return (Map<String, Object>) model;
    }
    throw new RuntimeException("The model is not a map");
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

}
