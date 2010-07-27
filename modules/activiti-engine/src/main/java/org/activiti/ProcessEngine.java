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
package org.activiti;

/**
 * provides access to all the services that expose the BPM and workflow operations.
 * 
 * <ul>
 * <li>
 * <b>{@link org.activiti.ProcessService}: </b> Allows the creation of
 * {@link org.activiti.Deployment}s and the starting of and searching on
 * {@link org.activiti.ProcessInstance}s.</li>
 * <li>
 * <b>{@link org.activiti.TaskService}: </b> Exposes operations to manage human
 * (standalone) {@link org.activiti.Task}s, such as claiming, completing and
 * assigning tasks</li>
 * <li>
 * <b>{@link org.activiti.IdentityService}: </b> Used for managing
 * {@link org.activiti.identity.User}s, {@link org.activiti.identity.Group}s and
 * the relations between them<</li>
 * <li>
 * <b>{@link org.activiti.ManagementService}: </b> Exposes engine admin and
 * maintenance operations</li>
 * </ul>
 * 
 * Typically, there will be only one central ProcessEngine instance needed in a
 * end-user application. Building a ProcessEngine is done through a
 * {@link DbProcessEngineBuilder} instance and is a costly operation which should be
 * avoided. For that purpose, it is advised to store it in a static field or
 * JNDI location (or something similar). This is a thread-safe object, so no
 * special precautions need to be taken.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ProcessEngine {

  /** the version of the activiti library */
  public static String VERSION = "5.0.alpha4";

  /** the name as specified in the 'process.engine.name' property in 
   * the activiti.properties configuration file or in the 
   * The default name for a process engine is 'default */
  String getName();

  ProcessService getProcessService();
  TaskService getTaskService();
  HistoricDataService getHistoricDataService();
  IdentityService getIdentityService();
  ManagementService getManagementService();

  void close();
}
