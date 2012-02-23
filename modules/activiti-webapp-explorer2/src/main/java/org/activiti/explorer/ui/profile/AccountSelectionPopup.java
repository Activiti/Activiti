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

package org.activiti.explorer.ui.profile;

import java.util.HashMap;
import java.util.Map;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.TabbedSelectionWindow;
import org.activiti.explorer.ui.event.SubmitEvent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;


/**
 * @author Joram Barrez
 */
public class AccountSelectionPopup extends TabbedSelectionWindow {

  private static final long serialVersionUID = 1L;
  protected I18nManager i18nManager;
  
  protected Form imapForm;
  protected ClickListener imapClickListener;
  
  protected Form alfrescoForm;
  protected ClickListener alfrescoClickListener;
  
  public AccountSelectionPopup(String title) {
    super(title); // builds up UI
    setWidth(600, UNITS_PIXELS);
    setHeight(400, UNITS_PIXELS);
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    // TODO: components are eager loaded. For performance they should be lazy loaded (eg through factory)
    
    // Imap
    initImapComponent();
    String imap = i18nManager.getMessage(Messages.PROFILE_ACCOUNT_IMAP);
    addSelectionItem(new Embedded(null, Images.IMAP), imap, imapForm, imapClickListener);
    
    // Alfresco
    initAlfrescoComponent();
    addSelectionItem(new Embedded(null, Images.ALFRESCO), 
            i18nManager.getMessage(Messages.PROFILE_ACCOUNT_ALFRESCO), 
            alfrescoForm, alfrescoClickListener);
    
    selectionTable.select(imap);
  }
  
  protected void initImapComponent() {
    imapForm = new Form();
    imapForm.setDescription(i18nManager.getMessage(Messages.IMAP_DESCRIPTION));
    
    final TextField imapServer = new TextField(i18nManager.getMessage(Messages.IMAP_SERVER));
    imapForm.getLayout().addComponent(imapServer);
    
    final TextField imapPort = new TextField(i18nManager.getMessage(Messages.IMAP_PORT));
    imapPort.setWidth(30, UNITS_PIXELS);
    imapPort.setValue(143); // Default imap port (non-ssl)
    imapForm.getLayout().addComponent(imapPort);
    
    final CheckBox useSSL = new CheckBox(i18nManager.getMessage(Messages.IMAP_SSL));
    useSSL.setValue(false);
    useSSL.setImmediate(true);
    imapForm.getLayout().addComponent(useSSL);
    useSSL.addListener(new ValueChangeListener() {
      public void valueChange(ValueChangeEvent event) {
        imapPort.setValue( ((Boolean) useSSL.getValue()) ? 993 : 143);
      }
    });
    
    final TextField imapUserName = new TextField(i18nManager.getMessage(Messages.IMAP_USERNAME));
    imapForm.getLayout().addComponent(imapUserName);
    
    final PasswordField imapPassword = new PasswordField(i18nManager.getMessage(Messages.IMAP_PASSWORD));
    imapForm.getLayout().addComponent(imapPassword);
    
    // Matching listener
    imapClickListener = new ClickListener() {
      public void buttonClick(ClickEvent event) {
        Map<String, Object> accountDetails = createAccountDetails(
                "imap", 
                imapUserName.getValue().toString(), 
                imapPassword.getValue().toString(),
                "server", imapServer.getValue().toString(),
                "port", imapPort.getValue().toString(),
                "ssl", imapPort.getValue().toString()
                ); 
        fireEvent(new SubmitEvent(AccountSelectionPopup.this, SubmitEvent.SUBMITTED, accountDetails));
      }
    };
  }
  
  protected void initAlfrescoComponent() {
    alfrescoForm = new Form();
    alfrescoForm.setDescription(i18nManager.getMessage(Messages.ALFRESCO_DESCRIPTION));
    
    final TextField alfrescoServer = new TextField(i18nManager.getMessage(Messages.ALFRESCO_SERVER));
    alfrescoForm.getLayout().addComponent(alfrescoServer);
    
    final TextField alfrescoUserName = new TextField(i18nManager.getMessage(Messages.ALFRESCO_USERNAME));
    alfrescoForm.getLayout().addComponent(alfrescoUserName);
    
    final PasswordField alfrescoPassword = new PasswordField(i18nManager.getMessage(Messages.ALFRESCO_PASSWORD));
    alfrescoForm.getLayout().addComponent(alfrescoPassword);
    
    // Matching listener
    alfrescoClickListener = new ClickListener() {
      public void buttonClick(ClickEvent event) {
        Map<String, Object> accountDetails = createAccountDetails(
                "alfresco", 
                alfrescoUserName.getValue().toString(), 
                alfrescoPassword.getValue().toString(),
                "server", alfrescoServer.getValue().toString()
                ); 
        fireEvent(new SubmitEvent(AccountSelectionPopup.this, SubmitEvent.SUBMITTED, accountDetails));
      }
    };
  }
  
  protected Map<String, Object> createAccountDetails(String acountName, String userName, 
          String password, String ... additionalDetails) {
    Map<String, Object> accountDetails = new HashMap<String, Object>();
    accountDetails.put("accountName", acountName);
    accountDetails.put("userName", userName);
    accountDetails.put("password", password);
    
    if (additionalDetails != null && additionalDetails.length > 0) {
      Map<String, String> additional = new HashMap<String, String>();
      for (int i=0; i<additionalDetails.length; i+=2) {
        additional.put(additionalDetails[i], additionalDetails[i+1]);
      }
      accountDetails.put("additional", additional);
    }
    
    return accountDetails;
  }

}
