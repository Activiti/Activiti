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

package org.activiti.engine.repository;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * An object structure representing an executable process composed of activities and transitions.
 *
 * Business processes are often created with graphical editors that store the process definition in certain file format. These files can be added to a {@link Deployment} artifact, such as for example
 * a Business Archive (.bar) file.
 *
 * At deploy time, the engine will then parse the process definition files to an executable instance of this class, that can be used to start a {@link ProcessInstance}.
 *
 */
@Internal
public interface ProcessDefinition {

  /** unique identifier */
  String getId();

  /**
   * category name which is derived from the targetNamespace attribute in the definitions element
   */
  String getCategory();

  /** label used for display purposes */
  String getName();

  /** unique name for all versions this process definitions */
  String getKey();

  /** description of this process **/
  String getDescription();

  /** version of this process definition */
  int getVersion();

  /**
   * name of {@link RepositoryService#getResourceAsStream(String, String) the resource} of this process definition.
   */
  String getResourceName();

  /** The deployment in which this process definition is contained. */
  String getDeploymentId();

  /** The resource name in the deployment of the diagram image (if any). */
  String getDiagramResourceName();

  /**
   * Does this process definition has a {@link FormService#getStartFormData(String) start form key}.
   */
  boolean hasStartFormKey();

  /**
   * Does this process definition has a graphical notation defined (such that a diagram can be generated)?
   */
  boolean hasGraphicalNotation();

  /** Returns true if the process definition is in suspended state. */
  boolean isSuspended();

  /** The tenant identifier of this process definition */
  String getTenantId();

  /** The engine version for this process definition (5 or 6) */
  String getEngineVersion();

  void setAppVersion(Integer appVersion);

  Integer getAppVersion();

}
