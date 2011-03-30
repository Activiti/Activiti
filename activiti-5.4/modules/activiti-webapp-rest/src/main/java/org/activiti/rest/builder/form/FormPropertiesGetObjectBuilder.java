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

package org.activiti.rest.builder.form;

import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Stefan Schröder
 * 
 */
public class FormPropertiesGetObjectBuilder extends BaseJSONObjectBuilder {

  private PropertyJSONConverter converter = new PropertyJSONConverter();

  @SuppressWarnings("unchecked")
  @Override
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    JSONObject result = new JSONObject();
    Map<String, Object> model = getModelAsMap(modelObject);

    List<FormProperty> properties = (List<FormProperty>) model.get("formproperties");
    JSONArray propertyArray = JSONUtil.putNewArray(result, "data");

    if (properties != null) {
      for (FormProperty property : properties) {
        propertyArray.put(converter.getJSONObject(property));
      }
    }
    return result;
  }

}
