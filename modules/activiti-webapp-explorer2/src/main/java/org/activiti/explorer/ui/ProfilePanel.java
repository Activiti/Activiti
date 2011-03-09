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

package org.activiti.explorer.ui;

import org.activiti.explorer.Constants;

import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ProfilePanel extends Panel {
  
  private static final long serialVersionUID = -4274649964206760400L;
  
  protected ViewManager viewManager;
  protected HorizontalLayout profilePanelLayout;
  protected VerticalLayout infoPanelLayout;
  
  public ProfilePanel(ViewManager viewManager) {
    this.viewManager = viewManager;
    
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();

    // Profile page is a horizontal layout: left we have a panel with the picture, 
    // and one the right there is another panel the about, contact, etc information
    this.profilePanelLayout = new HorizontalLayout();
    profilePanelLayout.setSizeFull();
    setContent(profilePanelLayout);
    
    // init both panels
    initImagePanel();
    initInformationPanel();
  }
  
  protected void initImagePanel() {
    // Panel
    Panel imagePanel = new Panel();
    imagePanel.setStyleName(Reindeer.PANEL_LIGHT);
    
    // Image
    Embedded image = new Embedded(null, viewManager.getClassResource("images/kermit.jpg"));
    imagePanel.addComponent(image);
    imagePanel.setHeight("100%");
    imagePanel.getContent().setHeight("100%");
    imagePanel.getContent().setWidth(image.getWidth(), image.getWidthUnits());
    imagePanel.setWidth(image.getWidth(), image.getWidthUnits());
    
    profilePanelLayout.addComponent(imagePanel);
  }
  
  protected void initInformationPanel() {
    Panel infoPanel = new Panel();
    infoPanel.setStyleName(Reindeer.PANEL_LIGHT);
    infoPanel.setSizeFull();
    
    profilePanelLayout.addComponent(infoPanel);
    profilePanelLayout.setExpandRatio(infoPanel, 1.0f); // info panel should take all the width available
    
    // All the information sections are put under each other in a vertical layout
    this.infoPanelLayout = new VerticalLayout();
    infoPanel.setContent(infoPanelLayout);
    
    initAboutSection();
    initContactSection();
    initAccountsSection();
  }

  protected void initAboutSection() {
    addProfileHeader(infoPanelLayout, "About");
    GridLayout aboutLayout = createInfoSectionLayout(2, 4); 

    addProfileEntry(aboutLayout, "Name: ", "Kermit The Frog");
    addProfileEntry(aboutLayout, "Job title: ", "Activiti core mascot");
    addProfileEntry(aboutLayout, "Birth date: ", "01/01/1955");
    addProfileEntry(aboutLayout, "Location: ", "Muppet Country");
  }
  
  protected void initContactSection() {
    addProfileHeader(infoPanelLayout, "Contact");
    GridLayout contactLayout = createInfoSectionLayout(2, 3); 
    
    addProfileEntry(contactLayout, "Email: ", "kermit@muppets.com");
    addProfileEntry(contactLayout, "Phone: ", "+145893689");

    // The skype entry shows the name + skype icon, laid out in a small grid
    GridLayout skypeLayout = new GridLayout(3,1);
    skypeLayout.setSizeUndefined();
    
    Label skypeValueLabel = new Label("kermit.frog");
    skypeValueLabel.setSizeUndefined();
    skypeLayout.addComponent(skypeValueLabel);
    
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    skypeLayout.addComponent(emptySpace);
    
    Embedded skypeImage = new Embedded(null, viewManager.getClassResource("images/skype.png"));
    skypeImage.setSizeUndefined();
    skypeLayout.addComponent(skypeImage);
    
    addProfileEntry(contactLayout, "Skype: ", skypeLayout);
  }
  
  protected void  initAccountsSection() {
    addProfileHeader(infoPanelLayout, "Accounts");
    GridLayout accountLayout = createInfoSectionLayout(3, 2); 

    // Google
    Embedded googleImage = new Embedded(null, viewManager.getClassResource(("images/google.png")));
    googleImage.setSizeUndefined();
    accountLayout.addComponent(googleImage);
    addProfileEntry(accountLayout, "Google: ", "mr_kermit_frog@gmail.com");
    
    // Alfresco
    Embedded alfrescoImage = new Embedded(null, viewManager.getClassResource("images/alfresco.gif"));
    alfrescoImage.setSizeUndefined();
    accountLayout.addComponent(alfrescoImage);
    addProfileEntry(accountLayout, "Alfresco: ", "kermit_alfresco");
  }
  
  protected void addProfileHeader(VerticalLayout infoLayout, String headerName) {
    Label aboutLabel = new Label(headerName);
    aboutLabel.setWidth("90%");
    aboutLabel.addStyleName(Constants.STYLE_PROFILE_HEADER);
    infoLayout.addComponent(aboutLabel);
  }
  
  protected GridLayout createInfoSectionLayout(int columns, int rows) {
    GridLayout layout = new GridLayout(columns, rows);
    layout.setSpacing(true);
    layout.addStyleName(Constants.STYLE_PROFILE_LAYOUT);
    infoPanelLayout.addComponent(layout);
    return layout;
  }
  
  protected void addProfileEntry(GridLayout layout, String name, String value) {
    addProfileEntry(layout, name, new Label(value));
  }
  
  protected void addProfileEntry(GridLayout layout, String name, Component value) {
    addProfileEntry(layout, new Label(name), value);
  }
  
  protected void addProfileEntry(GridLayout layout, Component name, Component value) {
    name.addStyleName(Constants.STYLE_PROFILE_FIELD);
    name.setSizeUndefined();
    layout.addComponent(name);
    
    value.setSizeUndefined();
    layout.addComponent(value);
  }

}
