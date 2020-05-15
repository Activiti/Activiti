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

package org.activiti.engine.repository;

import org.activiti.engine.api.internal.Internal;

import java.util.Date;

/**
 * Represents a model that is stored in the model repository. In addition, a model can be deployed to the Activiti Engine in a separate deployment step.
 *
 * A model is a container for the meta data and sources of a process model that typically can be edited in a modeling environment.
 *
 */
@Internal
public interface Model {

  String getId();

  String getName();

  void setName(String name);

  String getKey();

  void setKey(String key);

  String getCategory();

  void setCategory(String category);

  Date getCreateTime();

  Date getLastUpdateTime();

  Integer getVersion();

  void setVersion(Integer version);

  String getMetaInfo();

  void setMetaInfo(String metaInfo);

  String getDeploymentId();

  void setDeploymentId(String deploymentId);

  void setTenantId(String tenantId);

  String getTenantId();

  /** whether this model has editor source */
  boolean hasEditorSource();

  /** whether this model has editor source extra */
  boolean hasEditorSourceExtra();
}
