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

import org.activiti.explorer.ui.NoParamComponentFactory;
import org.activiti.explorer.ui.alfresco.AlfrescoManagementMenuBar;
import org.activiti.explorer.ui.custom.ToolBar;


/**
 * @author Joram Barrez
 */
public class ManagementMenuBarFactory extends NoParamComponentFactory<ToolBar> {

  protected Class< ? extends ToolBar> getAlfrescoComponentClass() {
    return AlfrescoManagementMenuBar.class; 
  }

  protected Class< ? extends ToolBar> getDefaultComponentClass() {
    return ManagementMenuBar.class;
  }

}
