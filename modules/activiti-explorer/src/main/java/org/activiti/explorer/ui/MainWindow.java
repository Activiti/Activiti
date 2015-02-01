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

import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.navigation.NavigationFragmentChangeListener;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.login.LoginPage;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.mainlayout.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Component;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.Window;

/**
 * @author Joram Barrez
 */
public class MainWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  @Autowired
  protected I18nManager i18nManager;
  
  @Autowired
  protected NavigationFragmentChangeListener navigationFragmentChangeListener;
  
  // UI
  protected MainLayout mainLayout;
  protected UriFragmentUtility uriFragmentUtility;
  protected UriFragment currentUriFragment;
  protected boolean showingLoginPage;

  public MainWindow() {
    setTheme(ExplorerLayout.THEME);
  }
  
  @Override
  public void attach() {
    super.attach();
    setCaption(i18nManager.getMessage(Messages.APP_TITLE));
  }

  public void showLoginPage() {
    showingLoginPage = true;
    addStyleName(ExplorerLayout.STYLE_LOGIN_PAGE);
    setContent(new LoginPage());
  }
  
  public void showDefaultContent() {
    showingLoginPage = false;
    removeStyleName(ExplorerLayout.STYLE_LOGIN_PAGE);
    addStyleName("Default style"); // Vaadin bug: must set something or old style (eg. login page style) is not overwritten
    
    // init general look and feel
    mainLayout = new MainLayout();
    setContent(mainLayout);

    // init hidden components
    initHiddenComponents();
  }
  
  // View handling
  
  public void switchView(Component component) {
    mainLayout.setMainContent(component);
  }
  
  public void setMainNavigation(String navigation) {
    mainLayout.setMainNavigation(navigation);
  }
  
  // URL handling
  
  protected void initHiddenComponents() {
    // Add the URI Fragent utility
    uriFragmentUtility = new UriFragmentUtility();
    mainLayout.addComponent(uriFragmentUtility);
    
    // Add listener to control page flow based on URI
    uriFragmentUtility.addListener(navigationFragmentChangeListener);
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
  
  public void setNavigationFragmentChangeListener(NavigationFragmentChangeListener navigationFragmentChangeListener) {
    this.navigationFragmentChangeListener = navigationFragmentChangeListener;
  }
  
  public void setI18nManager(I18nManager i18nManager) {
    this.i18nManager = i18nManager;
  }
}
