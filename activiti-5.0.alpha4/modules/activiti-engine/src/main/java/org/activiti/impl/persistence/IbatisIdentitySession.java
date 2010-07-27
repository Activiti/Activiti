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

package org.activiti.impl.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 */
public class IbatisIdentitySession implements Session {

//  public void saveUser(UserImpl user) {
//    if (user.isNew()) {
//      sqlSession.insert(statement("insertUser"), user);
//    } else {
//      sqlSession.update(statement("updateUser"), user);
//    }
//  }
//
//  public UserImpl findUser(String userId) {
//    return (UserImpl) sqlSession.selectOne(statement("selectUser"), userId);
//  }
//  
//  public List<UserImpl> findUsersByGroup(String groupId) {
//    return sqlSession.selectList(statement("selectUsersByGroup"), groupId);
//  }
//  
//  public List<UserImpl> findUsers() {
//    return sqlSession.selectList(statement("selectUsers"));
//  }
//  
//  public boolean isValidUser(String userId) {
//    return findUser(userId) != null;
//  }
//  
//  public void deleteUser(String userId) {
//    sqlSession.delete(statement("deleteMembershipsForUser"), userId);
//    sqlSession.delete(statement("deleteUser"), userId);
//  }
//
//  public void saveGroup(GroupImpl group) {
//    if (group.isNew()) {
//      sqlSession.insert(statement("insertGroup"), group);
//    } else {
//      sqlSession.update(statement("updateGroup"), group);
//    }
//  }
//  
//  public GroupImpl findGroup(String groupId) {
//    return (GroupImpl) sqlSession.selectOne(statement("selectGroup"), groupId);
//  }
//  
//  public List<GroupImpl> findGroupsByUser(String userId) {
//    return sqlSession.selectList(statement("selectGroupsByUser"), userId);
//  }
//  
//  public List<GroupImpl> findGroupsByUserAndType(String userId, String groupType) {
//    Map<String, Object> parameters = new HashMap<String, Object>();
//    parameters.put("userId", userId);
//    parameters.put("groupType", groupType);
//    return sqlSession.selectList(statement("selectGroupsByUserAndType"), parameters);
//  }
//
//  public List<GroupImpl> findGroups() {
//    return sqlSession.selectList(statement("selectGroups"));
//  }
//
//  public void deleteGroup(String groupId) {
//    sqlSession.delete(statement("deleteMembershipsForGroup"), groupId);
//    sqlSession.delete(statement("deleteGroup"), groupId);
//  }  
//
//  public void createMembership(String userId, String groupId) {
//    Map<String, Object> parameters = new HashMap<String, Object>();
//    parameters.put("userId", userId);
//    parameters.put("groupId", groupId);
//    sqlSession.insert(statement("insertMembership"), parameters);
//  }
//
//  public void deleteMembership(String userId, String groupId) {
//    Map<String, Object> parameters = new HashMap<String, Object>();
//    parameters.put("userId", userId);
//    parameters.put("groupId", groupId);
//    sqlSession.delete(statement("deleteMembership"), parameters);
//  }


  public void close() {
  }

  public void flush() {
  }
}
