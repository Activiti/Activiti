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
package org.activiti.explorer.ui.alfresco;

import org.activiti.explorer.ui.mainlayout.MainMenuBar;


/**
 * @author Joram Barrez
 */
public class AlfrescoMainMenuBar extends MainMenuBar {

  private static final long serialVersionUID = 1L;
  
  @Override
  protected void initButtons() {
    // In Alfresco admin console, only mgmt should be accessible
    // so there is no point in offering any buttons
  }
  
  @Override
  public synchronized void setMainNavigation(String navigation) {
    // Not needed since there are no buttons in the menu
  }
  
  @Override
  protected boolean useProfile() {
    // Only show logout button, profile is not used in alfresco
    return false;
  }
}
