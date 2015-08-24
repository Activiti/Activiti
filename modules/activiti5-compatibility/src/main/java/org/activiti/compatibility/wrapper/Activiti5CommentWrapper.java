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

package org.activiti.compatibility.wrapper;

import java.util.Date;

import org.activiti.engine.task.Comment;

/**
 * Wraps an Activiti 5 comment to an Activiti 6 {@link Comment}.
 * 
 * @author Tijs Rademakers
 */
public class Activiti5CommentWrapper implements Comment {

  private org.activiti5.engine.task.Comment activit5Comment;
  
  public Activiti5CommentWrapper(org.activiti5.engine.task.Comment activit5Comment) {
    this.activit5Comment = activit5Comment;
  }

  @Override
  public String getId() {
    return activit5Comment.getId();
  }

  @Override
  public String getUserId() {
    return activit5Comment.getUserId();
  }

  @Override
  public Date getTime() {
    return activit5Comment.getTime();
  }

  @Override
  public String getTaskId() {
    return activit5Comment.getTaskId();
  }

  @Override
  public String getProcessInstanceId() {
    return activit5Comment.getProcessInstanceId();
  }

  @Override
  public String getType() {
    return activit5Comment.getType();
  }

  @Override
  public String getFullMessage() {
    return activit5Comment.getFullMessage();
  }
  
  public org.activiti5.engine.task.Comment getRawObject() {
    return activit5Comment;
  }
}
