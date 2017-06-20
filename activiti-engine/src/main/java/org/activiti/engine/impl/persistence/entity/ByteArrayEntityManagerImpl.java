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

import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.ByteArrayDataManager;
import org.activiti.engine.impl.persistence.entity.data.DataManager;


/**


 */
public class ByteArrayEntityManagerImpl extends AbstractEntityManager<ByteArrayEntity> implements ByteArrayEntityManager {
  
  protected ByteArrayDataManager byteArrayDataManager;
  
  public ByteArrayEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, ByteArrayDataManager byteArrayDataManager) {
    super(processEngineConfiguration);
    this.byteArrayDataManager = byteArrayDataManager;
  }
   
  @Override
  protected DataManager<ByteArrayEntity> getDataManager() {
    return byteArrayDataManager;
  }
  
  @Override
  public List<ByteArrayEntity> findAll() {
    return byteArrayDataManager.findAll();
  }
  
  @Override
  public void deleteByteArrayById(String byteArrayEntityId) {
    byteArrayDataManager.deleteByteArrayNoRevisionCheck(byteArrayEntityId);
  }

  public ByteArrayDataManager getByteArrayDataManager() {
    return byteArrayDataManager;
  }

  public void setByteArrayDataManager(ByteArrayDataManager byteArrayDataManager) {
    this.byteArrayDataManager = byteArrayDataManager;
  }
  
}
