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

package org.activiti.explorer.ui.management.identity;

import org.activiti.engine.IdentityService;
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
public class DeleteMembershipListener implements ClickListener {
  
  private static final long serialVersionUID = 1L;
  protected IdentityService identityService;
  protected String userId;
  protected String groupId;
  protected MemberShipChangeListener membershipChangeListener;

  public DeleteMembershipListener(IdentityService identityService, String userId,
          String groupId, MemberShipChangeListener memberShipChangeListener ) {
    this.identityService = identityService;
    this.userId = userId;
    this.groupId = groupId;
    this.membershipChangeListener = memberShipChangeListener;
  }
  
  public void click(ClickEvent event) {
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    ViewManager viewManager = ExplorerApp.get().getViewManager();
    
    // Add listener to confirmation window. If confirmed, membership is deleted
    ConfirmationDialogPopupWindow confirmationPopup = 
      new ConfirmationDialogPopupWindow(i18nManager.getMessage(Messages.USER_CONFIRM_DELETE_GROUP, userId, groupId));
    confirmationPopup.addListener(new ConfirmationEventListener() {
      protected void rejected(ConfirmationEvent event) {
      }
      protected void confirmed(ConfirmationEvent event) {
        identityService.deleteMembership(userId, groupId);
        membershipChangeListener.notifyMembershipChanged();
      }
    });
    
    // Show popup
    viewManager.showPopupWindow(confirmationPopup);
  }

}
