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
package org.activiti.cycle.service;

import org.activiti.cycle.impl.service.CycleServiceConfiguration;

/**
 * This is the central entry point for Activiti Cycle and provides access to the
 * {@link CycleRepositoryService}, the {@link CycleTagService} and the
 * {@link CycleConfigurationService}.
 * 
 */
public class CycleServiceFactory {

  public static CycleRepositoryService getRepositoryService() {
    return CycleServiceConfiguration.getInstance().getRepositoryService();
  }

  public static CycleTagService getTagService() {
    return CycleServiceConfiguration.getInstance().getTagService();
  }

  public static CycleConfigurationService getConfigurationService() {
    return CycleServiceConfiguration.getInstance().getConfigurationService();
  }

  public static CyclePluginService getCyclePluginService() {
    return CycleServiceConfiguration.getInstance().getCyclePluginService();
  }

  public static CycleContentService getContentService() {
    return CycleServiceConfiguration.getInstance().getCycleContentServiceImpl();
  }

}
