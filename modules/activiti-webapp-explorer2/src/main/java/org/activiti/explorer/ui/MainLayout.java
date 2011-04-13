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

package org.activiti.explorer.ui;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.CustomLayout;


/**
 * @author Joram Barrez
 */
public class MainLayout extends CustomLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected MainMenuBar mainMenuBar;
  
  public MainLayout() {
    super(ExplorerLayout.CUSTOM_LAYOUT_DEFAULT);
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    // Components visible on every page
    setSizeFull();
    initMainMenuBar();
  }
  
  protected void initMainMenuBar() {
    this.mainMenuBar = new MainMenuBar();
    addComponent(mainMenuBar, ExplorerLayout.LOCATION_MAIN_MENU);
  }
  
  public void setMainNavigation(String navigation) {
    mainMenuBar.setMainNavigation(navigation);
  }
}
