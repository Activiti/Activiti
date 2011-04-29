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

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;


/**
 * @author Joram Barrez
 */
public class DeleteAccountClickListener implements ClickListener {
  
  private static final long serialVersionUID = 1L;
  protected String userId;
  protected String accountName;
  protected ProfilePanel profilePanel;
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected IdentityService identityService;
  
  public DeleteAccountClickListener(String userId, String accountName, ProfilePanel profilePanel) {
    this.userId = userId;
    this.accountName = accountName;
    this.profilePanel = profilePanel;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  }

  public void click(ClickEvent event) {
    ConfirmationDialogPopupWindow popup = new ConfirmationDialogPopupWindow(
            i18nManager.getMessage(Messages.PROFILE_DELETE_ACCOUNT_TITLE, accountName),
            i18nManager.getMessage(Messages.PROFILE_DELETE_ACCOUNT_DESCRIPTION, accountName));
    
    popup.addListener(new ConfirmationEventListener() {
      private static final long serialVersionUID = 1L;
      protected void rejected(ConfirmationEvent event) {
      }
      protected void confirmed(ConfirmationEvent event) {
        identityService.deleteUserAccount(userId, accountName);
        profilePanel.refreshAccounts();
      }
    });
    
    viewManager.showPopupWindow(popup);
  }

}
