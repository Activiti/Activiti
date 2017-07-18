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

package org.activiti.spring.test.transaction;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**

 */
public class UserBean {

  /**
   * injected by Spring
   */
  private RuntimeService runtimeService;

  /**
   * injected by Spring
   */
  private TaskService taskService;

  /**
   * injected by Spring
   */
  private DataSource dataSource;

  @Transactional
  public void hello() {
    // here you can do transactional stuff in your domain model
    // and it will be combined in the same transaction as
    // the startProcessInstanceByKey to the Activiti RuntimeService
    runtimeService.startProcessInstanceByKey("helloProcess");
  }

  @Transactional
  public void completeTask(String taskId) {

    // First insert a record in the MY_TABLE table
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    int nrOfRows = jdbcTemplate.update("insert into MY_TABLE values ('test');");
    if (nrOfRows != 1) {
      throw new RuntimeException("Insert into MY_TABLE failed");
    }

    taskService.complete(taskId);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  @Required
  public void setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  @Required
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

}
