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

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;

import com.vaadin.ui.VerticalLayout;


/**
 * @author Joram Barrez
 */
public class DatabasePage extends ManagementPage {

  private static final long serialVersionUID = -3989067128946859490L;
  
  // services
  protected ManagementService managementService;
  
  // ui
  protected VerticalLayout databasePageLayout;
  
  public DatabasePage() {
    super();
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
  }

}
