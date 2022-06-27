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

package org.activiti.engine.impl.asyncexecutor.multitenant;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;

/**
 * Factory used by the {@link ExecutorPerTenantAsyncExecutor} async executor.
 * This factory will be called when a new {@link AsyncExecutor} for a tenant needs to be created
 * and can be used to create special implementations for specific tenants.
 */
public interface TenantAwareAsyncExecutorFactory {

  /**
   * Allows to create an {@link AsyncExecutor} specifically for a tenant.
   */
  AsyncExecutor createAsyncExecutor(String tenantId);

}
