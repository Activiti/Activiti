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

import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.task.Attachment;


/**
 * @author Joram Barrez
 */
public class AttachmentDataManagerImpl extends AbstractDataManager<AttachmentEntity> implements AttachmentDataManager {
  
  @Override
  public Class<AttachmentEntity> getManagedEntityClass() {
    return AttachmentEntity.class;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectAttachmentsByProcessInstanceId", processInstanceId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectAttachmentsByTaskId", taskId);
  }

}
