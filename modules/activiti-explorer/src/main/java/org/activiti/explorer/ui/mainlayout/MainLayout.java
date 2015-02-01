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

package org.activiti.explorer.ui.mainlayout;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class MainLayout extends VerticalLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected MainMenuBar mainMenuBar;
  
  protected CssLayout header;
  protected CssLayout main;
  protected CssLayout footer;
  
  public MainLayout() {
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setSizeFull();
    addStyleName(ExplorerLayout.STYLE_MAIN_WRAPPER);
    
    initHeader();
    initMainMenuBar();
    initMain();
    initFooter();
  }
  
  public void setMainContent(Component mainContent) {
    main.removeAllComponents();
    main.addComponent(mainContent);
  }
  
  public void setFooter(Component footerContent) {
    footer.removeAllComponents();
    footer.addComponent(footerContent);
  }
  
  public void setMainNavigation(String navigation) {
    mainMenuBar.setMainNavigation(navigation);
  }
  
  protected void initHeader() {
    header = new CssLayout();
    header.addStyleName(ExplorerLayout.STYLE_HEADER);
    header.setWidth(100, UNITS_PERCENTAGE);
    addComponent(header);
  }

  protected void initMain() {
    main = new CssLayout();
    main.setSizeFull();
    main.addStyleName(ExplorerLayout.STYLE_MAIN_CONTENT);
    addComponent(main);
    setExpandRatio(main, 1.0f);
  }

  protected void initFooter() {
    footer = new CssLayout();
    footer.setWidth(100, UNITS_PERCENTAGE);
    footer.addStyleName(ExplorerLayout.STYLE_MAIN_FOOTER);
    addComponent(footer);
    
    Label footerLabel = new Label();
    footerLabel.setContentMode(Label.CONTENT_XHTML);
    footerLabel.setValue(i18nManager.getMessage(Messages.FOOTER_MESSAGE));
    footerLabel.setWidth(100, UNITS_PERCENTAGE);
    footer.addComponent(footerLabel);
  }

  protected void initMainMenuBar() {
    this.mainMenuBar = ExplorerApp.get().getComponentFactory(MainMenuBarFactory.class).create(); 
    header.addComponent(mainMenuBar);
  }
}
