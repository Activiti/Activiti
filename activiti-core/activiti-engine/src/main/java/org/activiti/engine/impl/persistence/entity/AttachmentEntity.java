/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.persistence.entity;

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.task.Attachment;


@Internal
@Deprecated
public interface AttachmentEntity extends Attachment, Entity, HasRevision {

  void setType(String type);

  void setTaskId(String taskId);

  void setProcessInstanceId(String processInstanceId);

  void setUrl(String url);

  void setContentId(String contentId);

  ByteArrayEntity getContent();

  void setContent(ByteArrayEntity content);

  void setUserId(String userId);

  String getUserId();

  Date getTime();

  void setTime(Date time);

}
