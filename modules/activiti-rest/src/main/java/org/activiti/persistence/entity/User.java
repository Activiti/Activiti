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

package org.activiti.persistence.entity;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 * @author Tom Baeyens
 */
public class User {

  String userId;
  String password;

  public User() {
    super();
  }

  public User(DBObject userJson) {
    this.userId = (String) userJson.get("userId");
    this.password = (String) userJson.get("password");
  }

  public DBObject toJson() {
    BasicDBObject userJson = new BasicDBObject();
    userJson.put("userId", userId);
    userJson.put("password", password);
    return userJson;
  }

  public String getUserId() {
    return userId;
  }
  
  public User setUserId(String userId) {
    this.userId = userId;
    return this;
  }
  
  public String getPassword() {
    return password;
  }
  
  public User setPassword(String password) {
    this.password = password;
    return this;
  }
}
