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

package org.activiti.persistence;

import org.activiti.persistence.entity.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


/**
 * @author Tom Baeyens
 */
public class Users {

  DBCollection users;

  public Users(DBCollection users) {
    this.users = users;
  }

  public void insertUser(User user) {
    users.insert(user.toJson());
  }

  public void deleteUser(String userId) {
    DBObject query = new BasicDBObject();
    query.put("userId", userId);
    users.remove(query);
  }

  public User findUser(String userId) {
    DBObject query = new BasicDBObject();
    query.put("userId", userId);
    DBObject userJson = users
      .find(query)
      .next();
    return new User(userJson);
  }
}
