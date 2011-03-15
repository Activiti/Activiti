/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.activiti.explorer;

import org.activiti.engine.ProcessEngines;
import org.activiti.explorer.ui.MainLayout;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * @author Joram Barrez
 */
public class ExplorerApplication extends Application {

  private static final long serialVersionUID = -8923370280251348552L;
  
  protected Window mainWindow;
  protected MainLayout mainLayout;

  public void init() {
    
    // Demo
    setUser(ProcessEngines.getDefaultProcessEngine().getIdentityService()
             .createUserQuery().userId("kermit").singleResult());
    ProcessEngines.getDefaultProcessEngine().getIdentityService().setAuthenticatedUserId("kermit");
    // Demo
    
    // init window
    mainWindow = new Window("My pretty Vaadin Application");
    setMainWindow(mainWindow);
    setTheme(Constants.THEME);

    // init general look and feel
    mainLayout = new MainLayout(this); 
    mainWindow.setContent(mainLayout);
    
  }
  
}
