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

import org.activiti.explorer.ExplorerApp;


/**
 * @author Joram Barrez
 */
public class GroupNavigator extends ManagementNavigator {

 public static final String GROUP_URI_PART = "group";
  
  public String getTrigger() {
    return GROUP_URI_PART;
  }
  
  public void handleManagementNavigation(UriFragment uriFragment) {
    String groupId = uriFragment.getUriPart(1);
    
    if(groupId != null) {
      ExplorerApp.get().getViewManager().showGroupPage(groupId);
    } else {
      ExplorerApp.get().getViewManager().showGroupPage();
    }
  }


}
