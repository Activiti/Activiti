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
 * @author Frederik Heremans
 */
public class SavedReportNavigator implements Navigator {

  private static final long serialVersionUID = 1L;
  
  public static final String SAVED_REPORT_URI_PART = "savedReport";
  
  public String getTrigger() {
    return SAVED_REPORT_URI_PART;
  }

  public void handleNavigation(UriFragment uriFragment) {
    String modelId = uriFragment.getUriPart(1);
    
    if(modelId != null) {
      ExplorerApp.get().getViewManager().showSavedReportPage(modelId);
    } else {
      ExplorerApp.get().getViewManager().showSavedReportPage();
    }
  }

}
