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
package org.activiti.engine;


import org.activiti.engine.api.internal.Internal;

/**
 * Provides access to all the services that expose the BPM and workflow operations.
 *
 * <ul>
 * <li>
 * <b>{@link org.activiti.engine.RuntimeService}: </b> Allows the creation of {@link org.activiti.engine.repository.Deployment}s and the starting of and searching on
 * {@link org.activiti.engine.runtime.ProcessInstance}s.</li>
 * <li>
 * <b>{@link org.activiti.engine.TaskService}: </b> Exposes operations to manage human (standalone) {@link org.activiti.engine.task.Task}s, such as claiming, completing and assigning tasks</li>
 * <li>
 * <b>{@link org.activiti.engine.ManagementService}: </b> Exposes engine admin and maintenance operations</li>
 * <li>
 * <b>{@link org.activiti.engine.HistoryService}: </b> Service exposing information about ongoing and past process instances.</li>
 * </ul>
 *
 * Typically, there will be only one central ProcessEngine instance needed in a end-user application. Building a ProcessEngine is done through a {@link ProcessEngineConfiguration} instance and is a
 * costly operation which should be avoided. For that purpose, it is advised to store it in a static field or JNDI location (or something similar). This is a thread-safe object, so no special
 * precautions need to be taken.
 *
 */
@Internal
public interface ProcessEngine {

  /** the version of the activiti library */
  public static String VERSION = "7.1.0-M6"; // Note the extra -x at the end. To cater for snapshot releases with different database changes

  /**
   * The name as specified in 'process-engine-name' in the activiti.cfg.xml configuration file. The default name for a process engine is 'default
   */
  String getName();

  void close();

  RepositoryService getRepositoryService();

  RuntimeService getRuntimeService();

  TaskService getTaskService();

  HistoryService getHistoryService();

  ManagementService getManagementService();

  DynamicBpmnService getDynamicBpmnService();

  ProcessEngineConfiguration getProcessEngineConfiguration();

}
