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

import java.util.Arrays;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;


/**
 * Popup window to create a new group.
 * 
 * @author Joram Barrez
 */
public class NewGroupPopupWindow extends PopupWindow {
  
  private static final long serialVersionUID = 1L;
  protected transient IdentityService identityService;
  protected I18nManager i18nManager;
  protected Form form;
  
  public NewGroupPopupWindow() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setCaption(i18nManager.getMessage(Messages.GROUP_CREATE));
    setModal(true);
    center();
    setResizable(false);
    setWidth(265, UNITS_PIXELS);
    setHeight(240, UNITS_PIXELS);
    addStyleName(Reindeer.WINDOW_LIGHT);
    
    initEnterKeyListener();
    initForm();
  }
  
  protected void initEnterKeyListener() {
    addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        handleFormSubmit();
      }
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {new ShortcutAction("enter", ShortcutAction.KeyCode.ENTER, null)};
      }
    });
  }
  
  protected void initForm() {
    form = new Form();
    form.setValidationVisibleOnCommit(true);
    form.setImmediate(true);
    addComponent(form);
    
    initInputFields();
    initCreateButton();
  }

  protected void initInputFields() {
    // Input fields
    form.addField("id", new TextField(i18nManager.getMessage(Messages.GROUP_ID)));
    
    // Set id field to required
    form.getField("id").setRequired(true);
    form.getField("id").setRequiredError(i18nManager.getMessage(Messages.GROUP_ID_REQUIRED));
    form.getField("id").focus();
    
    // Set id field to be unique
    form.getField("id").addValidator(new Validator() {
      public void validate(Object value) throws InvalidValueException {
        if (!isValid(value)) {
          throw new InvalidValueException(i18nManager.getMessage(Messages.GROUP_ID_UNIQUE));
        }
      }
      public boolean isValid(Object value) {
        if (value != null) {
          return identityService.createGroupQuery().groupId(value.toString()).singleResult() == null;
        }
        return false;
      }
    });
    
    form.addField("name", new TextField(i18nManager.getMessage(Messages.GROUP_NAME)));
    ComboBox typeComboBox = new ComboBox(i18nManager.getMessage(Messages.GROUP_TYPE), 
            Arrays.asList("assignment", "security-role"));
    typeComboBox.select("assignment");
    form.addField("type", typeComboBox);
  }
  
  protected void initCreateButton() {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setWidth(100, UNITS_PERCENTAGE);
    form.getFooter().setWidth(100, UNITS_PERCENTAGE);
    form.getFooter().addComponent(buttonLayout);
    
    Button createButton = new Button(i18nManager.getMessage(Messages.GROUP_CREATE));
    buttonLayout.addComponent(createButton);
    buttonLayout.setComponentAlignment(createButton, Alignment.BOTTOM_RIGHT);
    
    createButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
      handleFormSubmit();
      }
    });
  }
  
  protected void handleFormSubmit() {
    try {
      // create user
      form.commit(); // will throw exception in case validation is false
      Group group = createGroup();
      
      // close popup and navigate to new group
      close();
      ExplorerApp.get().getViewManager().showGroupPage(group.getId());
      
    } catch (InvalidValueException e) {
      // Do nothing: the Form component will render the errormsgs automatically
      setHeight(270, UNITS_PIXELS);
    }
  }
  
  protected Group createGroup() {
    Group group = identityService.newGroup(form.getField("id").getValue().toString());
    group.setName(form.getField("name").getValue().toString());
    group.setType(form.getField("type").getValue().toString());
    identityService.saveGroup(group);
    return group;
  }

}
