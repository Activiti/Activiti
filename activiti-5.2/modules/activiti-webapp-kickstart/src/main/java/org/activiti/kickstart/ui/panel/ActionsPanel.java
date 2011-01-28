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
package org.activiti.kickstart.ui.panel;

import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.listener.CreateKickstartWorkflowClickListener;
import org.activiti.kickstart.ui.listener.EnhanceWorkflowClickListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class ActionsPanel extends Panel {

  protected static final long serialVersionUID = -8360966745210668309L;

  protected static final String CREATE_WORKFLOW = "Create workflow";
  protected static final String ENHANCE_WORKFLOW = "Enhance workflow";

  public ActionsPanel(ViewManager viewManager) {
    setStyleName(Reindeer.PANEL_LIGHT);
    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);

    Button createAdhocWorkflowButton = new Button(CREATE_WORKFLOW);
    createAdhocWorkflowButton.setWidth("132px");
    createAdhocWorkflowButton.addListener(new CreateKickstartWorkflowClickListener(viewManager));

    Button enhanceWorkflowButton = new Button(ENHANCE_WORKFLOW);
    enhanceWorkflowButton.setWidth("132px");
    enhanceWorkflowButton.addListener(new EnhanceWorkflowClickListener(viewManager));

    layout.addComponent(createAdhocWorkflowButton);
    layout.addComponent(enhanceWorkflowButton);
    addComponent(layout);
  }

}
