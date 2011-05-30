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

import java.util.HashMap;
import java.util.Stack;

import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

/**
 * Manages the different views
 * 
 * @author Patrick Oberg
 * 
 */
public class ViewManager {

  HashMap<String, Layout> views = new HashMap<String, Layout>();
  Stack<Layout> screenStack = new Stack<Layout>();
  Panel window;

  public ViewManager(Panel window) {
    this.window = window;
  }

  public void init() {
  }

  /**
   * Switches current screen to the given screen.
   * 
   * @param screenType
   *          The screen to switch to.
   * @param newScreen
   *          If not null, the existing screen is replaced with the given
   *          screen.
   */
  public void switchScreen(String viewName, Layout newView) {
    Layout view;
    if (newView != null) {
      view = newView;
      views.put(viewName, newView);
    } else
      view = views.get("viewname");
    window.setContent(view);
  }

  /**
   * Switches to the given screen and pushes the current screen to stack. The
   * pushed screen can be switched back to by calling popScreen().
   * 
   * @param screenType
   *          Screen to switch to.
   */
  public void pushScreen(String viewName, Layout newView) {
    screenStack.push((Layout) window.getContent());
    switchScreen(viewName, newView);
  }

  /**
   * Switches back to the topmost screen in the screen stack.
   */
  public void popScreen() {
    window.setContent(screenStack.pop());
  }

  public Panel getWindow() {
    return window;
  }
}