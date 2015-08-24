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


/**
 * @author Joram Barrez
 * @author Marcus Klimstra (CGI)
 */
public class ByteArrayEntityManagerImpl extends AbstractEntityManager<ByteArrayEntity> implements ByteArrayEntityManager {
  
  @Override
  public ByteArrayEntity createAndInsert(byte[] bytes) {
    return createAndInsert(null, bytes);
  }
  
  @Override
  public ByteArrayEntity createAndInsert(String name, byte[] bytes) {
    ByteArrayEntity byteArrayEntity = new ByteArrayEntity(name, bytes);
    insert(byteArrayEntity);
    return byteArrayEntity;
  }

  @Override
  public ByteArrayEntity findById(String byteArrayEntityId) {
    return getDbSqlSession().selectById(ByteArrayEntity.class, byteArrayEntityId);
  }

  @Override
  public void deleteByteArrayById(String byteArrayEntityId) {
    getDbSqlSession().delete("deleteByteArrayNoRevisionCheck", byteArrayEntityId);
  }

  @Override
  public void deleteByteArray(ByteArrayEntity byteArray) {
    getDbSqlSession().delete(byteArray);
  }

}
