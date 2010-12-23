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

/**
 * @author Joram Barrez
 */
public class TaskDto {

  protected String id;
  protected String name;
  protected String assignee;
  protected String description;
  protected FormDto form;
  protected boolean startWithPrevious;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public FormDto getForm() {
    return form;
  }
  public void setForm(FormDto formDto) {
    this.form = formDto;
  }
  public boolean getStartsWithPrevious() {
    return startWithPrevious;
  }
  public void setStartWithPrevious(boolean startWithPrevious) {
    this.startWithPrevious = startWithPrevious;
  }
  public String generateDefaultFormName() {
    return name.replace(" ", "_") + ".form";
  }

}
