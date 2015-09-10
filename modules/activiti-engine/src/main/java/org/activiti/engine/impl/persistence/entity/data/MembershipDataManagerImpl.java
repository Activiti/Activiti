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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.impl.persistence.entity.MembershipEntityImpl;

/**
 * @author Joram Barrez
 */
public class MembershipDataManagerImpl extends AbstractDataManager<MembershipEntity> implements MembershipDataManager {
  
  public MembershipDataManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends MembershipEntity> getManagedEntityClass() {
    return MembershipEntityImpl.class;
  }
  
  @Override
  public MembershipEntity create() {
    return new MembershipEntityImpl();
  }
  
  @Override
  public void deleteMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    getDbSqlSession().delete("deleteMembership", parameters);
  }
  
  @Override
  public void deleteMembershipByGroupId(String groupId) {
    getDbSqlSession().delete("deleteMembershipsByGroupId", groupId);
  }
  
  @Override
  public void deleteMembershipByUserId(String userId) {
    getDbSqlSession().delete("deleteMembershipsByUserId", userId);
  }
  
}
