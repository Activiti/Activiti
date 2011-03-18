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
package org.activiti.explorer.ui.management;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ManagementPage extends CustomComponent {
  
  private static final long serialVersionUID = -138537246412384952L;
  
  protected VerticalLayout managementPageLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  
  public ManagementPage() {
    addManagementPageLayout();
    addManagementMenuBar();
    addMainSplitPanel();
  }
  
  protected void addManagementPageLayout() {
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    managementPageLayout = new VerticalLayout();
    managementPageLayout.setSizeFull();
    setCompositionRoot(managementPageLayout);
    setSizeFull();
  }
  
  protected void addManagementMenuBar() {
    managementPageLayout.addComponent(new ManagementMenuBar());
  }
  
  protected void addMainSplitPanel() {
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(17, HorizontalSplitPanel.UNITS_PERCENTAGE);
    
    managementPageLayout.addComponent(mainSplitPanel);
    managementPageLayout.setExpandRatio(mainSplitPanel, 1.0f);
  }

}
