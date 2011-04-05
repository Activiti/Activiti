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
import org.activiti.explorer.Messages;
import org.activiti.explorer.navigation.NavigationFragmentChangeListener;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.login.LoginPage;

import com.vaadin.ui.Component;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.Window;

/**
 * @author Joram Barrez
 */
public class MainWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  protected MainLayout mainLayout;
  protected UriFragmentUtility uriFragmentUtility;
  protected UriFragment currentUriFragment;
  protected boolean showingLoginPage;

  public MainWindow() {
    super(ExplorerApp.get().getI18nManager().getMessage(Messages.APP_TITLE));
    setTheme(ExplorerLayout.THEME);
  }

  public void showLoginPage() {
    showingLoginPage = true;
    addStyleName(ExplorerLayout.STYLE_LOGIN_PAGE);
    setContent(new LoginPage());
  }
  
  public void showDefaultContent() {
    showingLoginPage = false;
    removeStyleName(ExplorerLayout.STYLE_LOGIN_PAGE);
    addStyleName("Default style"); // Bug: must set something or old style (eg. login page style) is not overwritten
    
    // init general look and feel
    mainLayout = new MainLayout();
    setContent(mainLayout);

    // init hidden components
    initHiddenComponents();
  }
  
  // View handling
  
  public void switchView(Component component) {
    mainLayout.addComponent(component, ExplorerLayout.LOCATION_CONTENT);
  }
  
  // URL handling
  
  protected void initHiddenComponents() {
    // Add the URI Fragent utility
    uriFragmentUtility = new UriFragmentUtility();
    mainLayout.addComponent(uriFragmentUtility, ExplorerLayout.LOCATION_HIDDEN);
    
    // Add listener to control page flow based on URI
    uriFragmentUtility.addListener(new NavigationFragmentChangeListener());
  }
  
  public UriFragment getCurrentUriFragment() {
    return currentUriFragment;
  }

  /**
   * Sets the current {@link UriFragment}. 
   * Won't trigger navigation, just updates the URI fragment in the browser.
   */
  public void setCurrentUriFragment(UriFragment fragment) {
    this.currentUriFragment = fragment;
    
    if(fragmentChanged(fragment)) {
      
      if(fragment != null) {
        uriFragmentUtility.setFragment(fragment.toString(), false);      
      } else {
        uriFragmentUtility.setFragment("", false);      
      }
    }
  }

  private boolean fragmentChanged(UriFragment fragment) {
    String fragmentString = fragment.toString();
    if(fragmentString == null) {
      return uriFragmentUtility.getFragment() != null;
    } else {
      return !fragmentString.equals(uriFragmentUtility.getFragment());
    }
  }
  
  public boolean isShowingLoginPage() {
    return showingLoginPage;
  }

}
