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
package org.activiti.kickstart.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joram Barrez
 */
public class TaskBlock {

  protected List<BaseTaskDto> tasks;

  public TaskBlock() {
    this.tasks = new ArrayList<BaseTaskDto>();
  }

  public TaskBlock(List<BaseTaskDto> tasks) {
    this.tasks = tasks;
  }

  public List<BaseTaskDto> getTasks() {
    return tasks;
  }

  public void setTasks(List<BaseTaskDto> tasks) {
    this.tasks = tasks;
  }

  public void addTask(BaseTaskDto task) {
    tasks.add(task);
  }

  public BaseTaskDto get(int index) {
    return tasks.get(index);
  }

  public int getNrOfTasks() {
    return tasks.size();
  }

}
