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

package org.activiti.engine.impl.cfg.multitenant;

import java.util.Collection;


/**
 * Interface to be implemented when using the {@link MultiSchemaMultiTenantProcessEngineConfiguration} and used
 * to set/get the current user and tenant identifier.
 *
 * The engine will call the {@link #getCurrentTenantId()} method when it needs to know which database to use.
 *
 * Typically used with {@link ThreadLocal}'s in the implementation.
 *
 * @deprecated multi-tenant code will be removed in future version of Activiti and Activiti Cloud
 */
@Deprecated
public interface TenantInfoHolder {

  /**
   * Returns all known tenant identifiers.
   */
  Collection<String> getAllTenants();

  /**
   * Sets the current tenant identifier.
   */
  void setCurrentTenantId(String tenantid);

  /**
   * Returns the current tenant identifier.
   */
  String getCurrentTenantId();

  /**
   * Clears the current tenant identifier settings.
   */
  void clearCurrentTenantId();

}
