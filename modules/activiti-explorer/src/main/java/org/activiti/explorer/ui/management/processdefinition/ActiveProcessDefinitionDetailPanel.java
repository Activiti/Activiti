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
package org.activiti.explorer.ui.management.processdefinition;

import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.process.AbstractProcessDefinitionDetailPanel;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * @author Joram Barrez
 */
public class ActiveProcessDefinitionDetailPanel extends AbstractProcessDefinitionDetailPanel {

  private static final long serialVersionUID = 1L;

  public ActiveProcessDefinitionDetailPanel(String processDefinitionId,  ActiveProcessDefinitionPage activeProcessDefinitionPage) {
    super(processDefinitionId, activeProcessDefinitionPage);
  }
  
  protected void initActions(final AbstractPage parentPage) {
    ActiveProcessDefinitionPage processDefinitionPage = (ActiveProcessDefinitionPage) parentPage;

    Button suspendButton = new Button(i18nManager.getMessage(Messages.PROCESS_SUSPEND));
    suspendButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        ChangeProcessSuspensionStatePopupWindow popupWindow = 
                new ChangeProcessSuspensionStatePopupWindow(processDefinition.getId(), parentPage, true);
        ExplorerApp.get().getViewManager().showPopupWindow(popupWindow);
      }
      
    });
    
    // Check if button must be disabled
    boolean suspendJobPending = false;
    List<Job> jobs = ProcessEngines.getDefaultProcessEngine().getManagementService()
            .createJobQuery().processDefinitionId(processDefinition.getId()).list();
    for (Job job : jobs) {
      // TODO: this is a hack. Needs to be cleaner in engine!
      if ( ((JobEntity) job).getJobHandlerType().equals(TimerSuspendProcessDefinitionHandler.TYPE) ) {
        suspendJobPending = true;
        break;
      }
    }
    suspendButton.setEnabled(!suspendJobPending);
    
    // Clear toolbar and add 'start' button
    processDefinitionPage.getToolBar().removeAllButtons();
    processDefinitionPage.getToolBar().addButton(suspendButton);
  }

}
