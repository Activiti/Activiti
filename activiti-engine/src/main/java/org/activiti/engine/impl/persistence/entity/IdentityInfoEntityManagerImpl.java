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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.IdentityInfoDataManager;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class IdentityInfoEntityManagerImpl extends AbstractEntityManager<IdentityInfoEntity> implements IdentityInfoEntityManager {
  
  protected IdentityInfoDataManager identityInfoDataManager;
  
  public IdentityInfoEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, IdentityInfoDataManager identityInfoDataManager) {
    super(processEngineConfiguration);
    this.identityInfoDataManager = identityInfoDataManager;
  }

  @Override
  protected DataManager<IdentityInfoEntity> getDataManager() {
    return identityInfoDataManager;
  }
  
  @Override
  public void deleteUserInfoByUserIdAndKey(String userId, String key) {
    IdentityInfoEntity identityInfoEntity = findUserInfoByUserIdAndKey(userId, key);
    if (identityInfoEntity != null) {
      delete(identityInfoEntity);
    }
  }

  @Override
  public void updateUserInfo(String userId, String userPassword, String type, String key, String value, String accountPassword, Map<String, String> accountDetails) {
    byte[] storedPassword = null;
    if (accountPassword != null) {
      storedPassword = encryptPassword(accountPassword, userPassword);
    }

    IdentityInfoEntity identityInfoEntity = findUserInfoByUserIdAndKey(userId, key);
    if (identityInfoEntity != null) {
      // update
      identityInfoEntity.setValue(value);
      identityInfoEntity.setPasswordBytes(storedPassword);

      if (accountDetails == null) {
        accountDetails = new HashMap<String, String>();
      }

      Set<String> newKeys = new HashSet<String>(accountDetails.keySet());
      List<IdentityInfoEntity> identityInfoDetails = identityInfoDataManager.findIdentityInfoDetails(identityInfoEntity.getId());
      for (IdentityInfoEntity identityInfoDetail : identityInfoDetails) {
        String detailKey = identityInfoDetail.getKey();
        newKeys.remove(detailKey);
        String newDetailValue = accountDetails.get(detailKey);
        if (newDetailValue == null) {
          delete(identityInfoDetail);
        } else {
          // update detail
          identityInfoDetail.setValue(newDetailValue);
        }
      }
      insertAccountDetails(identityInfoEntity, accountDetails, newKeys);

    } else {
      // insert
      identityInfoEntity = identityInfoDataManager.create(); 
      identityInfoEntity.setUserId(userId);
      identityInfoEntity.setType(type);
      identityInfoEntity.setKey(key);
      identityInfoEntity.setValue(value);
      identityInfoEntity.setPasswordBytes(storedPassword);
      insert(identityInfoEntity, false);
      if (accountDetails != null) {
        insertAccountDetails(identityInfoEntity, accountDetails, accountDetails.keySet());
      }
    }
  }

  protected void insertAccountDetails(IdentityInfoEntity identityInfoEntity, Map<String, String> accountDetails, Set<String> keys) {
    for (String newKey : keys) {
      // insert detail
      IdentityInfoEntity identityInfoDetail = identityInfoDataManager.create();
      identityInfoDetail.setParentId(identityInfoEntity.getId());
      identityInfoDetail.setKey(newKey);
      identityInfoDetail.setValue(accountDetails.get(newKey));
      insert(identityInfoDetail, false);
    }
  }

  protected byte[] encryptPassword(String accountPassword, String userPassword) {
    return accountPassword.getBytes();
  }

  protected String decryptPassword(byte[] storedPassword, String userPassword) {
    return new String(storedPassword);
  }

  @Override
  public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
    return identityInfoDataManager.findUserInfoByUserIdAndKey(userId, key);
  }
  
  @Override
  public List<IdentityInfoEntity> findIdentityInfoByUserId(String userId) {
    return identityInfoDataManager.findIdentityInfoByUserId(userId);
  }

  @Override
  public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
    return identityInfoDataManager.findUserInfoKeysByUserIdAndType(userId, type);
  }

  public IdentityInfoDataManager getIdentityInfoDataManager() {
    return identityInfoDataManager;
  }

  public void setIdentityInfoDataManager(IdentityInfoDataManager identityInfoDataManager) {
    this.identityInfoDataManager = identityInfoDataManager;
  }
  
}
