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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.MembershipDataManager;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class MembershipEntityManagerImpl extends AbstractEntityManager<MembershipEntity> implements MembershipEntityManager {

  protected MembershipDataManager membershipDataManager;
  
  public MembershipEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, MembershipDataManager membershipDataManager) {
    super(processEngineConfiguration);
    this.membershipDataManager = membershipDataManager;
  }
 
  @Override
  protected DataManager<MembershipEntity> getDataManager() {
    return membershipDataManager;
  }
 
  public void createMembership(String userId, String groupId) {
    MembershipEntity membershipEntity = create();
    membershipEntity.setUserId(userId);
    membershipEntity.setGroupId(groupId);
    insert(membershipEntity, false);

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createMembershipEvent(ActivitiEventType.MEMBERSHIP_CREATED, groupId, userId));
    }
  }

  public void deleteMembership(String userId, String groupId) {
    membershipDataManager.deleteMembership(userId, groupId);  
    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createMembershipEvent(ActivitiEventType.MEMBERSHIP_DELETED, groupId, userId));
    }
  }
  
  @Override
  public void deleteMembershipByGroupId(String groupId) {
    membershipDataManager.deleteMembershipByGroupId(groupId);
  }
  
  @Override
  public void deleteMembershipByUserId(String userId) {
    membershipDataManager.deleteMembershipByUserId(userId);
  }

  public MembershipDataManager getMembershipDataManager() {
    return membershipDataManager;
  }

  public void setMembershipDataManager(MembershipDataManager membershipDataManager) {
    this.membershipDataManager = membershipDataManager;
  }
  
}
