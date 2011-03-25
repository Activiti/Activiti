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

import org.activiti.engine.form.FormProperty;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.model.RestFormProperty;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Stefan Schröder
 * 
 */
public class PropertyJSONConverter implements JSONConverter<FormProperty> {

  public JSONObject getJSONObject(FormProperty object) throws JSONException {
    JSONObject json = new JSONObject();
    FormProperty property = (RestFormProperty) object;

    if (property != null) {
      JSONUtil.putRetainNull(json, "id", property.getId());
      JSONUtil.putRetainNull(json, "name", property.getName());
      JSONUtil.putRetainNull(json, "value", property.getValue());
      JSONUtil.putRetainNull(json, "readable", property.isReadable());
      JSONUtil.putRetainNull(json, "required", property.isRequired());
      JSONUtil.putRetainNull(json, "writable", property.isWritable());
      if (property instanceof RestFormProperty)
        JSONUtil.putRetainNull(json, "type", ((RestFormProperty) property).getFormType());
      else
        JSONUtil.putRetainNull(json, "type", null);
    }
    return json;
  }

  public FormProperty getObject(JSONObject jsonObject) throws JSONException {
    throw new UnsupportedOperationException();
  }
}
