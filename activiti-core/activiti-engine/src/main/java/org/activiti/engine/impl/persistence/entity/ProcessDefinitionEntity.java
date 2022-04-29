/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.List;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.repository.ProcessDefinition;

@Internal
public interface ProcessDefinitionEntity extends ProcessDefinition, Entity, HasRevision {

  List<IdentityLinkEntity> getIdentityLinks();

  void setKey(String key);

  void setName(String name);

  void setDescription(String description);

  void setDeploymentId(String deploymentId);

  void setVersion(int version);

  void setResourceName(String resourceName);

  void setTenantId(String tenantId);

  Integer getHistoryLevel();

  void setHistoryLevel(Integer historyLevel);

  void setCategory(String category);

  void setDiagramResourceName(String diagramResourceName);

  boolean getHasStartFormKey();

  void setStartFormKey(boolean hasStartFormKey);

  void setHasStartFormKey(boolean hasStartFormKey);

  boolean isGraphicalNotationDefined();

  void setGraphicalNotationDefined(boolean isGraphicalNotationDefined);

  int getSuspensionState();

  void setSuspensionState(int suspensionState);

  String getEngineVersion();

  void setEngineVersion(String engineVersion);

}
