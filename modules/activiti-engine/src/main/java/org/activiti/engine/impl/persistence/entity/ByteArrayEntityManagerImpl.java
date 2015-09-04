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

import org.activiti.engine.impl.persistence.entity.data.ByteArrayDataManager;
import org.activiti.engine.impl.persistence.entity.data.DataManager;


/**
 * @author Joram Barrez
 * @author Marcus Klimstra (CGI)
 */
public class ByteArrayEntityManagerImpl extends AbstractEntityManager<ByteArrayEntity> implements ByteArrayEntityManager {
  
  protected ByteArrayDataManager byteArrayDataManager;
  
  public ByteArrayEntityManagerImpl() {
    
  }
  
  public ByteArrayEntityManagerImpl(ByteArrayDataManager byteArrayDataManager) {
    this.byteArrayDataManager = byteArrayDataManager;
  }
   
  @Override
  protected DataManager<ByteArrayEntity> getDataManager() {
    return byteArrayDataManager;
  }
  
  @Override
  public ByteArrayEntity createAndInsert(byte[] bytes) {
    return createAndInsert(null, bytes);
  }
  
  @Override
  public ByteArrayEntity createAndInsert(String name, byte[] bytes) {
    ByteArrayEntity byteArrayEntity = new ByteArrayEntity(name, bytes);
    byteArrayDataManager.insert(byteArrayEntity);
    return byteArrayEntity;
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
