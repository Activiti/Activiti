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
package org.activiti.explorer.ui.management;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.custom.ToolBar;


/**
 * @author Joram Barrez
 */
public abstract class ManagementPage extends AbstractTablePage {
  
  private static final long serialVersionUID = 1L;
  
  @Override
  protected ToolBar createMenuBar() {
    return ExplorerApp.get().getComponentFactory(ManagementMenuBarFactory.class).create();
  }
  
}
