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

package org.activiti.explorer.navigation;

import org.activiti.explorer.ExplorerApplication;

import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;


/**
 * FragmentChangedListener that is responsible to navigating the application to the right
 * page depending on the URI fragment.
 * 
 * @author Frederik Heremans
 */
public class NavigationFragmentChangeListener implements FragmentChangedListener {

  private static final long serialVersionUID = 4797018291237796530L;

  public void fragmentChanged(FragmentChangedEvent source) {
    String fragment = source.getUriFragmentUtility().getFragment();

    UriFragment uriFragment = new UriFragment(fragment);
    
    // Find appropriate handler based on the first part of the URI
    NavigationHandler navigationHandler = null;
    if(uriFragment.getUriParts() != null && uriFragment.getUriParts().size() > 0) {
      navigationHandler = NavigationHandlers.getHandler(uriFragment.getUriParts().get(0));
    }
    
    if(navigationHandler == null) {
      navigationHandler = NavigationHandlers.getDefaultHandler();
    }
    
    // Delegate navigation to handler
    navigationHandler.handleNavigation(uriFragment);
    
    // Set the parameter and parts on the current ExplorerApplication
    ExplorerApplication.getCurrent().setCurrentUriFragment(uriFragment);
  }

}
