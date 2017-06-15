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
package org.activiti.engine.impl.persistence.entity.data;

import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;

/**
 * @author Joram Barrez
 */
public interface UserDataManager extends DataManager<UserEntity> {

  List<User> findUserByQueryCriteria(UserQueryImpl query, Page page);

  long findUserCountByQueryCriteria(UserQueryImpl query);
 
  List<Group> findGroupsByUser(String userId);
  
  List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findUserCountByNativeQuery(Map<String, Object> parameterMap);

  
}
