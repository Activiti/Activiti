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
package org.activiti.kickstart.ui.listener;

import org.activiti.kickstart.dto.AdhocWorkflowDto;
import org.activiti.kickstart.service.AdhocWorkflowService;
import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.panel.AdhocWorkflowPanel;
import org.activiti.kickstart.ui.popup.ErrorPopupWindow;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * @author Joram Barrez
 */
public class EditExistingAdhocWorkflowClickListener implements Button.ClickListener {

  protected static final long serialVersionUID = -2160682103119947071L;

  protected ViewManager viewManager;
  protected AdhocWorkflowService adhocWorkflowService;

  public EditExistingAdhocWorkflowClickListener(ViewManager viewManager, AdhocWorkflowService adhocWorkflowService) {
    this.viewManager = viewManager;
    this.adhocWorkflowService = adhocWorkflowService;
  }

  public void buttonClick(ClickEvent event) {
    try {
      AdhocWorkflowDto adhocWorkflow = adhocWorkflowService.findAdhocWorkflowByKey((String) event.getButton().getData());
      viewManager.switchWorkArea(ViewManager.EDIT_ADHOC_WORKFLOW, new AdhocWorkflowPanel(viewManager, adhocWorkflow));
    } catch (Exception e) {
      e.printStackTrace();
      viewManager.showPopupWindow(new ErrorPopupWindow(e));
    }
  }

}
