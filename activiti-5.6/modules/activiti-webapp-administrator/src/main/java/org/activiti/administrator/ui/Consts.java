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
package org.activiti.administrator.ui;

import com.vaadin.ui.themes.Reindeer;

/**
 * Application constants
 * 
 * @author Patrick Oberg
 * 
 */
public final class Consts {

  // Application Layout
  public static final String THEME = "activiti";

  // Application main layout
  public static final String LAYOUT = Reindeer.LAYOUT_WHITE;

  // Application header layout
  public static final String HEADER = Reindeer.LAYOUT_BLACK;

  // Application window layout
  public static final String POPUP = Reindeer.WINDOW_LIGHT;

  // Id of div in layout where content is placed
  public static final String CONTENT = "content";

  // Id of div in layout where logout button is placed
  public static final String LOGOUT = "logout";

  // Prevent calling the constructor
  private Consts() {
    throw new AssertionError();
  }

}
