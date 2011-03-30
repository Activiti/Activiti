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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.activiti.administrator.AdminApp;
import org.activiti.administrator.ui.i18n.Messages;

/**
 * Main view of the application
 * 
 * @author Patrick Oberg
 * 
 */
public class MainView extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  private AdminApp app;

  public MainView(AdminApp application) {

    // Set application
    this.app = application;

    // Setup main layout
    setStyleName(Reindeer.LAYOUT_WHITE);
    setMargin(false);
    setSpacing(false);
    setSizeFull();

    // Create tab sheet
    TabSheet tabs = new TabSheet();
    tabs.setSizeFull();

    // Add tab styles
    tabs.addStyleName(Reindeer.TABSHEET_BORDERLESS);
    tabs.addStyleName(Reindeer.LAYOUT_WHITE);

    // Add task view tab
    tabs.addTab(new UserTab(app));
    tabs.addTab(new GroupTab(app));

    // Add tab sheet to layout
    addComponent(tabs);
    setExpandRatio(tabs, 1.0F);

    // Add footer text
    Label footerText = new Label(app.getMessage(Messages.Footer));
    footerText.setSizeUndefined();
    footerText.setStyleName(Reindeer.LABEL_SMALL);
    footerText.addStyleName("footer");
    addComponent(footerText);
    setComponentAlignment(footerText, Alignment.BOTTOM_CENTER);

  }

}
