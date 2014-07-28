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
package org.activiti.explorer.ui.management.crystalball;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.ui.AbstractOneViewPage;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.management.ManagementMenuBarFactory;

import com.vaadin.ui.AbstractSelect;

/**
 * @author Tijs Rademakers
 */
public class CrystalBallPage extends AbstractOneViewPage {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected String managementId;
  
  public CrystalBallPage() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    setDetailComponent(new EventOverviewPanel());
  }

  @Override
  protected ToolBar createMenuBar() {
    return ExplorerApp.get().getComponentFactory(ManagementMenuBarFactory.class).create();
  }

  @Override
  protected AbstractSelect createSelectComponent() {
    return null;
  }

  @Override
  public void refreshSelectNext() {
  }

  @Override
  public void selectElement(int index) {
  }  
}
