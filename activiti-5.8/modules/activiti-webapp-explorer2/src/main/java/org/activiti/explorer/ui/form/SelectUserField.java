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

package org.activiti.explorer.ui.form;

import java.util.Collection;

import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.SelectUsersPopupWindow;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;


/**
 * Field which allows you to select a user. The field-value is the
 * id of the selected user.
 * 
 * @author Frederik Heremans
 */
public class SelectUserField extends HorizontalLayout implements Field {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  
  protected TextField wrappedField;
  protected Label selectedUserLabel;
  protected Button selectUserButton;
  
  protected User selectedUser;
  
  public SelectUserField(String caption) {
    i18nManager = ExplorerApp.get().getI18nManager();
    
    setSpacing(true);
    setCaption(caption);
        
    selectedUserLabel = new Label();
    selectedUserLabel.setValue(i18nManager.getMessage(Messages.FORM_USER_NO_USER_SELECTED));
    selectedUserLabel.addStyleName(ExplorerLayout.STYLE_FORM_NO_USER_SELECTED);
    addComponent(selectedUserLabel);
    
    selectUserButton = new Button();
    selectUserButton.addStyleName(Reindeer.BUTTON_SMALL);
    selectUserButton.setCaption(i18nManager.getMessage(Messages.FORM_USER_SELECT));
    addComponent(selectUserButton);
    
    selectUserButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        final SelectUsersPopupWindow window = new SelectUsersPopupWindow(
          i18nManager.getMessage(Messages.FORM_USER_SELECT), false);
        window.addListener(new SubmitEventListener() {
          private static final long serialVersionUID = 1L;

          @Override
          protected void submitted(SubmitEvent event) {
            String userId = window.getSelectedUserId();
            setValue(userId);
          }
          
          @Override
          protected void cancelled(SubmitEvent event) {
          }
        });
        ExplorerApp.get().getViewManager().showPopupWindow(window);
      }
    });
        
    // Invisible textfield, only used as wrapped field
    wrappedField = new TextField();
    wrappedField.setVisible(false);
    addComponent(wrappedField);
  }

  public boolean isInvalidCommitted() {
    return wrappedField.isInvalidCommitted();
  }

  public void setInvalidCommitted(boolean isCommitted) {
    wrappedField.setInvalidCommitted(isCommitted);
  }

  public void commit() throws SourceException, InvalidValueException {
    wrappedField.commit();
  }

  public void discard() throws SourceException {
    wrappedField.discard();
  }

  public boolean isWriteThrough() {
    return wrappedField.isWriteThrough();
  }

  public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
    wrappedField.setWriteThrough(true);
  }

  public boolean isReadThrough() {
    return wrappedField.isReadThrough();
  }

  public void setReadThrough(boolean readThrough) throws SourceException {
    wrappedField.setReadThrough(readThrough);
  }

  public boolean isModified() {
    return wrappedField.isModified();
  }

  public void addValidator(Validator validator) {
    wrappedField.addValidator(validator);
  }

  public void removeValidator(Validator validator) {
    wrappedField.removeValidator(validator);
  }

  public Collection<Validator> getValidators() {
    return wrappedField.getValidators();
  }

  public boolean isValid() {
    return wrappedField.isValid();
  }

  public void validate() throws InvalidValueException {
    wrappedField.validate();
  }

  public boolean isInvalidAllowed() {
    return wrappedField.isInvalidAllowed();
  }

  public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
    wrappedField.setInvalidAllowed(invalidValueAllowed);
  }

  public Object getValue() {
    return wrappedField.getValue();
  }

  public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
    wrappedField.setValue(newValue);
    
    // Update label
    if(newValue != null) {
      if(selectedUser == null || !selectedUser.getId().equals(newValue)) {
        // fetch selected user based on id
         selectedUser = ExplorerApp.get().getUserCache().findUser((String) newValue);
      }
      selectedUserLabel.setValue(getSelectedUserLabel());
      selectedUserLabel.addStyleName(ExplorerLayout.STYLE_FORM_USER_SELECTED);
      selectedUserLabel.removeStyleName(ExplorerLayout.STYLE_FORM_NO_USER_SELECTED);
    } else {
      selectedUser = null;
      selectedUserLabel.setValue(i18nManager.getMessage(Messages.FORM_USER_NO_USER_SELECTED));
      selectedUserLabel.addStyleName(ExplorerLayout.STYLE_FORM_NO_USER_SELECTED);
      selectedUserLabel.removeStyleName(ExplorerLayout.STYLE_FORM_USER_SELECTED);
    }
  }

  protected Object getSelectedUserLabel() {
    if(selectedUser != null) {
      return selectedUser.getFirstName() + " " + selectedUser.getLastName();
    } else {
      return wrappedField.getValue();
    }
  }

  public Class< ? > getType() {
    return wrappedField.getType();
  }

  public void addListener(ValueChangeListener listener) {
    wrappedField.addListener(listener);
  }

  public void removeListener(ValueChangeListener listener) {
    wrappedField.removeListener(listener);
  }

  public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
    wrappedField.valueChange(event);
  }

  public void setPropertyDataSource(Property newDataSource) {
    wrappedField.setPropertyDataSource(newDataSource);
  }

  public Property getPropertyDataSource() {
    return wrappedField.getPropertyDataSource();
  }

  public int getTabIndex() {
    return wrappedField.getTabIndex();
  }

  public void setTabIndex(int tabIndex) {
    wrappedField.setTabIndex(tabIndex);
  }

  public boolean isRequired() {
    return wrappedField.isRequired();
  }

  public void setRequired(boolean required) {
    wrappedField.setRequired(required);
  }

  public void setRequiredError(String requiredMessage) {
    wrappedField.setRequiredError(requiredMessage);
  }

  public String getRequiredError() {
    return wrappedField.getRequiredError();
  }
  
  @Override
  public void focus() {
    wrappedField.focus();
  }
}
