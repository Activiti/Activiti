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

package org.activiti.rest.builder.identity;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.rest.builder.JSONConverter;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frederik Heremans
 */
class UserJSONConverter implements JSONConverter<User> {

  public JSONObject getJSONObject(User user) throws JSONException {
    UserEntity userEntity = (UserEntity) user;
    JSONObject json = new JSONObject();
    JSONUtil.putEmptyStringIfNull(json, "id", userEntity.getId());
    JSONUtil.putEmptyStringIfNull(json, "firstName", userEntity.getFirstName());
    JSONUtil.putEmptyStringIfNull(json, "lastName", userEntity.getLastName());
    JSONUtil.putEmptyStringIfNull(json, "email", userEntity.getEmail());
    return json;
  }

  public User getObject(JSONObject jsonObject) {
    throw new UnsupportedOperationException();
  }

}
