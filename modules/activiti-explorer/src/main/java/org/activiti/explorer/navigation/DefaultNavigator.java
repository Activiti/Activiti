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


/**
 * @author Frederik Heremans
 */
public class DefaultNavigator implements Navigator {

  public String getTrigger() {
    // Handles all non-matched navigation events, we can return null.
    return null;
  }

  public void handleNavigation(UriFragment uriFragment) {
    // TODO: What should happen with unhandled URL's?
  }

}
