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

package org.activiti;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
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
  
  protected Application application;
  
  public ProfilePanel(Application application) {
    this.application = application;
    
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();

    HorizontalLayout mainPanelLayout = new HorizontalLayout();
    mainPanelLayout.setSizeFull();
    setContent(mainPanelLayout);
    
    initImagePanel(mainPanelLayout);
    
    Panel rightPanel = new Panel();
    rightPanel.setStyleName(Reindeer.PANEL_LIGHT);
    rightPanel.setSizeFull();
    mainPanelLayout.addComponent(rightPanel);
    mainPanelLayout.setExpandRatio(rightPanel, 1.0f);
    
    VerticalLayout rightPanelLayout = new VerticalLayout();
    rightPanel.setContent(rightPanelLayout);
    
    initAboutSection(rightPanelLayout);
    initContactSection(rightPanelLayout);
    initAccountsSection(rightPanelLayout);
  }
  
  protected void initImagePanel(HorizontalLayout mainPanelLayout) {
    Panel imagePanel = new Panel();
    imagePanel.setStyleName(Reindeer.PANEL_LIGHT);
    Embedded image = new Embedded(null, new ClassResource("images/kermit.jpg", application));
    imagePanel.addComponent(image);
    imagePanel.getContent().setHeight("100%");
    imagePanel.getContent().setWidth(image.getWidth(), image.getWidthUnits());
    imagePanel.setHeight("100%");
    imagePanel.setWidth(image.getWidth(), image.getWidthUnits());
    mainPanelLayout.addComponent(imagePanel);
  }

  protected void initAboutSection(VerticalLayout rightPanelLayout) {
    Label aboutLabel = new Label("About");
    aboutLabel.setWidth("90%");
    aboutLabel.addStyleName("profile-header");
    rightPanelLayout.addComponent(aboutLabel);
    
    GridLayout aboutLayout = new GridLayout(2, 4);
    aboutLayout.setSpacing(true);
    aboutLayout.addStyleName("profile-layout");
    rightPanelLayout.addComponent(aboutLayout);
    
    Label nameLabel = new Label("Name: ");
    nameLabel.setSizeUndefined();
    nameLabel.addStyleName("profile-field");
    aboutLayout.addComponent(nameLabel);
    
    Label nameValueLabel = new Label("Kermit The Frog");
    nameValueLabel.setSizeUndefined();
    aboutLayout.addComponent(nameValueLabel);
    
    Label jobTitleLabel = new Label("Job title: ");
    jobTitleLabel.setSizeUndefined();
    jobTitleLabel.addStyleName("profile-field");
    aboutLayout.addComponent(jobTitleLabel);
    
    Label jobTitleValueLabel = new Label("Activiti core mascot");
    jobTitleValueLabel.setSizeUndefined();
    aboutLayout.addComponent(jobTitleValueLabel);
    
    Label birthDateLabel = new Label("Birth date: ");
    birthDateLabel.setSizeUndefined();
    birthDateLabel.addStyleName("profile-field");
    aboutLayout.addComponent(birthDateLabel);
    
    Label birthDateValueLabel = new Label("01/01/1955");
    birthDateValueLabel.setSizeUndefined();
    aboutLayout.addComponent(birthDateValueLabel);
    
    Label locationLabel = new Label("Location: ");
    locationLabel.setSizeUndefined();
    locationLabel.addStyleName("profile-field");
    aboutLayout.addComponent(locationLabel);
    
    Label locationValueLabel = new Label("Muppet Country");
    locationValueLabel.setSizeUndefined();
    aboutLayout.addComponent(locationValueLabel);
  }
  
  protected void initContactSection(VerticalLayout rightPanelLayout) {
    Label contactLabel = new Label("Contact");
    contactLabel.setWidth("90%");
    contactLabel.addStyleName("profile-header");
    rightPanelLayout.addComponent(contactLabel);
    
    GridLayout contactLayout = new GridLayout(2, 3);
    contactLayout.setSpacing(true);
    contactLayout.addStyleName("profile-layout");
    rightPanelLayout.addComponent(contactLayout);
    
    Label emailLabel = new Label("Email: ");
    emailLabel.setSizeUndefined();
    emailLabel.addStyleName("profile-field");
    contactLayout.addComponent(emailLabel);
    
    Label emailValueLabel = new Label("kermit@muppets.com");
    emailValueLabel.setSizeUndefined();
    contactLayout.addComponent(emailValueLabel);
    
    Label cellPhoneLabel = new Label("Phone: ");
    cellPhoneLabel.setSizeUndefined();
    cellPhoneLabel.addStyleName("profile-field");
    contactLayout.addComponent(cellPhoneLabel);
    
    Label cellPhoneValueLabel = new Label("+1458962645");
    cellPhoneValueLabel.setSizeUndefined();
    contactLayout.addComponent(cellPhoneValueLabel);
    
    Label skypeLabel = new Label("Skype: ");
    skypeLabel.setSizeUndefined();
    skypeLabel.addStyleName("profile-field");
    contactLayout.addComponent(skypeLabel);
    
    GridLayout skypeLayout = new GridLayout(3,1);
    skypeLayout.setSizeUndefined();
    contactLayout.addComponent(skypeLayout);
    
    Label skypeValueLabel = new Label("kermit.frog");
    skypeValueLabel.setSizeUndefined();
    skypeLayout.addComponent(skypeValueLabel);
    
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    skypeLayout.addComponent(emptySpace);
    
    Embedded skypeImage = new Embedded(null, new ClassResource("images/skype.png", application));
    skypeImage.setSizeUndefined();
    skypeLayout.addComponent(skypeImage);
  }
  
  protected void  initAccountsSection(VerticalLayout rightPanelLayout) {
    Label accountsLabel = new Label("Configured accounts");
    accountsLabel.setWidth("90%");
    accountsLabel.addStyleName("profile-header");
    rightPanelLayout.addComponent(accountsLabel);
    
    GridLayout accountLayout = new GridLayout(3, 2);
    accountLayout.setSpacing(true);
    accountLayout.addStyleName("profile-layout");
    rightPanelLayout.addComponent(accountLayout);
    
    Embedded googleImage = new Embedded(null, new ClassResource("images/google.png", application));
    googleImage.setSizeUndefined();
    accountLayout.addComponent(googleImage);
    
    Label googleLabel = new Label("Google: ");
    googleLabel.setSizeUndefined();
    googleLabel.addStyleName("profile-field");
    accountLayout.addComponent(googleLabel);
    
    Label googleValueLabel = new Label("mr_kermit_frog@gmail.com");
    googleValueLabel.setSizeUndefined();
    accountLayout.addComponent(googleValueLabel);
    
    Embedded alfrescoImage = new Embedded(null, new ClassResource("images/alfresco.gif", application));
    alfrescoImage.setSizeUndefined();
    accountLayout.addComponent(alfrescoImage);
    
    Label alfrescoLabel = new Label("Alfresco: ");
    alfrescoLabel.setSizeUndefined();
    alfrescoLabel.addStyleName("profile-field");
    accountLayout.addComponent(alfrescoLabel);
    
    Label alfrescoValueLabel = new Label("kermittf_alfr");
    alfrescoValueLabel.setSizeUndefined();
    accountLayout.addComponent(alfrescoValueLabel);
    
  }

}
