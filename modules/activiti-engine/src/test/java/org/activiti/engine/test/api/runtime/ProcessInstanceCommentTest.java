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
package org.activiti.engine.test.api.runtime;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class ProcessInstanceCommentTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testAddCommentToProcessInstance() {
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcessInstanceComment");
      
      taskService.addComment(null, processInstance.getId(), "Hello World");
      
      List<Comment> comments = taskService.getProcessInstanceComments(processInstance.getId());
      assertEquals(1, comments.size());

      List<Comment> commentsByType = taskService.getProcessInstanceComments(processInstance.getId(), "comment");
      assertEquals(1, commentsByType.size());

      commentsByType = taskService.getProcessInstanceComments(processInstance.getId(), "noThisType");
      assertEquals(0, commentsByType.size());
      
      // Suspend process instance
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      try {
        taskService.addComment(null, processInstance.getId(), "Hello World 2");
      } catch (ActivitiException e) {
        assertTextPresent("Cannot add a comment to a suspended execution", e.getMessage());
      }
      
      // Delete comments again
      taskService.deleteComments(null, processInstance.getId());
    }
  }

}
