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

import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel that should be used for main-content. Use {@link #setDetailContainer(ComponentContainer)}
 * and {@link #setFixedButtons(Component)} to add main components. By default, the detailContent will 
 * be a verticallayout which has 100% width and undefined height and margins enabled. Add components
 * to detail container using {@link #addDetailComponent(Component)} etc.
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
    
    CssLayout cssLayout = new CssLayout(); // Needed for rounded corners
    cssLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_PANEL);
    cssLayout.setSizeFull();
    super.addComponent(cssLayout);
    
    mainPanel = new Panel();
    mainPanel.addStyleName(Reindeer.PANEL_LIGHT);
    mainPanel.setSizeFull();
    cssLayout.addComponent(mainPanel);
    
    // Use default layout
    VerticalLayout verticalLayout = new VerticalLayout();
    verticalLayout.setWidth(100, UNITS_PERCENTAGE);
    verticalLayout.setMargin(true);
    mainPanel.setContent(verticalLayout);
  }
  
  /**
   * Set the actual content of the panel.
   */
  public void setDetailContainer(ComponentContainer component) {
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
     mainPanel.addComponent(c);
  }
  
  /**
   * Add component to detail-container.
   */
  public void addDetailComponent(Component c) {
    mainPanel.addComponent(c);
 }
  
  @Override
  public void addComponent(Component c, int index) {
    throw new UnsupportedOperationException("Cannot add components directly. Use addDetailComponent or setDetailContainer");
  }
  
  /**
   * Add component to detail-container.
   */
  public void addDetailComponent(Component c, int index) {
    if(mainPanel.getContent() instanceof AbstractOrderedLayout) {
      ((AbstractOrderedLayout) mainPanel.getContent()).addComponent(c, index);      
    } else {
      throw new UnsupportedOperationException("Cannot add components indexed component, detail content is not AbstractOrderedLayout");
    }
  }
  
  /**
   * Set expand-ratio of detail-component
   */
  public void setDetailExpandRatio(Component component, float ratio) {
    if(mainPanel.getContent() instanceof AbstractOrderedLayout) {
      ((AbstractOrderedLayout) mainPanel.getContent()).setExpandRatio(component, ratio);      
    } else {
      throw new UnsupportedOperationException("Cannot set ExpandRatio, detail content is not AbstractOrderedLayout");
    }
  }
  
  @Override
  public void addComponentAsFirst(Component c) {
    addComponent(c, 0);
  }
}
