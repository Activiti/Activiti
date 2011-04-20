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

package org.activiti.explorer.ui.custom;

import org.activiti.explorer.ui.ExplorerLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel that should be used for main-content. Only use {@link #setDetailContent(ComponentContainer)}
 * and {@link #setFixedButtons(Component)} to add components.
 * 
 * @author Frederik Heremans
 */
public class DetailPanel extends VerticalLayout {

  private static final long serialVersionUID = 1L;
  
  protected Panel mainPanel;
  
  public DetailPanel() {
    setSizeFull();
    addStyleName(ExplorerLayout.STYLE_DETAIL_PANEL);
    setMargin(true);
    
    CssLayout cssLayout = new CssLayout();
    cssLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_PANEL);
    cssLayout.setSizeFull();
    super.addComponent(cssLayout);
    
    mainPanel = new Panel();
    mainPanel.addStyleName(Reindeer.PANEL_LIGHT);
    mainPanel.setSizeFull();
    cssLayout.addComponent(mainPanel);
  }
  
  /**
   * Set the actual content of the panel.
   */
  public void setDetailContent(ComponentContainer component) {
    mainPanel.setContent(component);
  }
  
  /**
   * Set the component that is rendered in a fixed position
   * below the content. When content is scrolled, this component
   * stays visible all the time.
   */
  public void setFixedButtons(Component component) {
    if(getComponentCount() == 2) {
      removeComponent(getComponent(1));
    }
    addComponent(component);
  }
  
  @Override
  public void addComponent(Component c) {
    throw new UnsupportedOperationException("Cannot add components manually. Use setDetailContent or setFixedButtons");
  }
  
  @Override
  public void addComponent(Component c, int index) {
     throw new UnsupportedOperationException("Cannot add components manually. Use setDetailContent or setFixedButtons");
  }
  
  @Override
  public void addComponentAsFirst(Component c) {
    throw new UnsupportedOperationException("Cannot add components manually. Use setDetailContent or setFixedButtons");
  }
}
