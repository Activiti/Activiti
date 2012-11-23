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

package org.activiti.explorer.ui.management.processinstance;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
public class DeleteProcessInstanceClickListener implements ClickListener {
  
  private static final long serialVersionUID = 1L;
  
  protected String processInstanceId;
  protected AbstractTablePage processInstancePage;
  
  public DeleteProcessInstanceClickListener(String processInstanceId, AbstractTablePage processInstancePage) {
    this.processInstanceId = processInstanceId;
    this.processInstancePage = processInstancePage;
  }

  public void buttonClick(ClickEvent event) {
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    ViewManager viewManager = ExplorerApp.get().getViewManager();
    
    final ConfirmationDialogPopupWindow confirmPopup = new ConfirmationDialogPopupWindow(
            i18nManager.getMessage(Messages.PROCESS_INSTANCE_DELETE_POPUP_TITLE, processInstanceId), 
            i18nManager.getMessage(Messages.PROCESS_INSTANCE_DELETE_POPUP_DESCRIPTION, processInstanceId));

    confirmPopup.addListener(new ConfirmationEventListener() {
      private static final long serialVersionUID = 1L;
      protected void confirmed(ConfirmationEvent event) {
        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
        runtimeService.deleteProcessInstance(processInstanceId, null);
        processInstancePage.refreshSelectNext();
      }
    });
    
    viewManager.showPopupWindow(confirmPopup);
  }

}
