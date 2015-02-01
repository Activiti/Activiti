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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class GetIdentityLinksForTaskCmd implements Command<List<IdentityLink>>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected String taskId;

  public GetIdentityLinksForTaskCmd(String taskId) {
    this.taskId = taskId;
  }
  
  @SuppressWarnings({"unchecked", "rawtypes" })
  public List<IdentityLink> execute(CommandContext commandContext) {
    TaskEntity task = commandContext
      .getTaskEntityManager()
      .findTaskById(taskId);

    List<IdentityLink> identityLinks = (List) task.getIdentityLinks();
    
    // assignee is not part of identity links in the db. 
    // so if there is one, we add it here.
    // @Tom: we discussed this long on skype and you agreed ;-)
    // an assignee *is* an identityLink, and so must it be reflected in the API
    //
    // Note: we cant move this code to the TaskEntity (which would be cleaner),
    // since the task.delete cascased to all associated identityLinks 
    // and of course this leads to exception while trying to delete a non-existing identityLink
    if (task.getAssignee() != null) {
      IdentityLinkEntity identityLink = new IdentityLinkEntity();
      identityLink.setUserId(task.getAssignee());
      identityLink.setType(IdentityLinkType.ASSIGNEE);
      identityLink.setTaskId(task.getId());
      identityLinks.add(identityLink);
    }
    if (task.getOwner() != null) {
      IdentityLinkEntity identityLink = new IdentityLinkEntity();
      identityLink.setUserId(task.getOwner());
      identityLink.setTaskId(task.getId());
      identityLink.setType(IdentityLinkType.OWNER);
      identityLinks.add(identityLink);
    }
    
    return (List) task.getIdentityLinks();
  }
  
}
