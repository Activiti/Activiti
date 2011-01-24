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

import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.panel.KickstartWorkflowPanel;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * @author Joram Barrez
 */
public class CreateKickstartWorkflowClickListener implements Button.ClickListener {

  private static final long serialVersionUID = 3743698821958704189L;

  protected ViewManager viewManager;

  public CreateKickstartWorkflowClickListener(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  public void buttonClick(ClickEvent event) {
    viewManager.switchWorkArea(ViewManager.EDIT_ADHOC_WORKFLOW, new KickstartWorkflowPanel(viewManager));
  }

}
