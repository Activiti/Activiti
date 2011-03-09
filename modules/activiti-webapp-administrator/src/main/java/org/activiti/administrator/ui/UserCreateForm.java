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

import java.util.Set;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import org.activiti.administrator.AdminApp;
import org.activiti.administrator.ui.i18n.Messages;

/**
 * Form for creating users
 * 
 * @author Patrick Oberg
 * 
 */
@SuppressWarnings("unchecked")
public class UserCreateForm extends Form implements ClickListener {

  private static final long serialVersionUID = 1L;
  private static final String[] visibleItems = new String[] { "id", "password", "email", "firstName", "lastName" };

  private final Button create;
  private final Button close;
  private final Button reset;
  private final AdminApp app;

  private TwinColSelect groups;

  public UserCreateForm(AdminApp application) {

    // Set application reference
    this.app = application;

    // Enable buffering so that commit() must be called for the form.
    setWriteThrough(false);

    // Set the form to act immediately on user input.
    setImmediate(true);

    // Set form size
    setSizeFull();

    // Setup footer layout
    HorizontalLayout footer = new HorizontalLayout();
    footer.setSpacing(true);
    footer.setWidth("100%");
    footer.setVisible(true);

    // Add footer
    setFooter(footer);

    // Init buttons
    create = new Button(app.getMessage(Messages.Create), (ClickListener) this);
    close = new Button(app.getMessage(Messages.Close), (ClickListener) this);
    reset = new Button(app.getMessage(Messages.Reset), this, "discard");

    // Set button grid
    GridLayout grid = new GridLayout(3, 1);
    grid.addComponent(create, 0, 0);
    grid.addComponent(reset, 1, 0);
    grid.addComponent(close, 2, 0);
    grid.setSpacing(true);

    // Add grid to footer
    footer.addComponent(grid);

    // Right align buttons in footer
    footer.setComponentAlignment(grid, Alignment.BOTTOM_RIGHT);

    // Get all available groups
    groups = new TwinColSelect(app.getMessage(Messages.Groups), app.getAdminService().getGroups());

    // Set column headers
    groups.setLeftColumnCaption(app.getMessage(Messages.AvailableGroups));
    groups.setRightColumnCaption(app.getMessage(Messages.MemberOfGroups));

    // Propagate changes directly
    groups.setImmediate(true);

    // Max width
    groups.setWidth("100%");

    // Field factory for over riding how fields are created
    setFormFieldFactory(new DefaultFieldFactory() {

      private static final long serialVersionUID = 1L;

      @Override
      public Field createField(Item item, Object propertyId, Component uiContext) {

        Field field = super.createField(item, propertyId, uiContext);

        field.setWidth("100%");

        // field.setVisible(false);

        if (propertyId.equals("id")) {
          TextField tf = (TextField) field;

          tf.setVisible(true);

          // Do not display "null" to the user when the field is empty
          tf.setNullRepresentation("");

          // Set as required field
          tf.setRequired(true);

          // Set validator example, should not be restricted in the
          // admin ui
          // tf.addValidator(new
          // RegexpValidator("^[a-zA-Z0-9_-]{4,20}",
          // app.getMessage(Messages.InvalidUsername)));

          // Set error message
          tf.setRequiredError(app.getMessage(Messages.UsernameIsMissing));

        } else if (propertyId.equals("password")) {
          TextField tf = (TextField) field;

          tf.setVisible(true);

          // Do not display "null" to the user when the field is empty
          tf.setNullRepresentation("");

          // Set as required field
          tf.setRequired(true);

          // Set as secret (todo: use password field instead of text
          // field)
          tf.setSecret(true);

          // Set error message
          tf.setRequiredError(app.getMessage(Messages.PasswordIsMissing));

        } else if (propertyId.equals("email")) {
          TextField tf = (TextField) field;

          tf.setVisible(true);

          // Do not display "null" to the user when the field is empty
          tf.setNullRepresentation("");

          // Set as required field, should not be required by default
          // in the admin ui
          // tf.setRequired(true);

          // Set error message
          // tf.setRequiredError(application.getMessage(Messages.EmailIsMissing));

          /* Add a validator for email and make it required */
          field.addValidator(new EmailValidator(app.getMessage(Messages.EmailFormatError)));

        } else if (propertyId.equals("firstName")) {
          TextField tf = (TextField) field;

          tf.setVisible(true);

          // Do not display "null" to the user when the field is empty
          tf.setNullRepresentation("");

        } else if (propertyId.equals("lastName")) {
          TextField tf = (TextField) field;

          tf.setVisible(true);

          // Do not display "null" to the user when the field is empty
          tf.setNullRepresentation("");

        }

        return field;
      }
    });

  }

  @Override
  public void setItemDataSource(Item item) {

    if (item != null) {

      super.setItemDataSource(item);

      // Ensure that the fields are shown in correct order
      setVisibleItemProperties(visibleItems);

      // Attach non-bean fields
      attachField("groups", groups);

      // Clear groups
      groups.setValue(null);

      // Set focus on field
      getField("id").focus();

    } else {
      super.setItemDataSource(null);
    }

  }

  public void buttonClick(ClickEvent event) {

    // Get source event
    Button source = event.getButton();

    if (source == create) {

      // If the given input is not valid there is no point in continuing
      if (!isValid()) {
        return;
      }

      // Commit the form
      commit();

      try {

        // Save the user in db
        app.getAdminService().saveUser(getItemDataSource(), (Set<String>) groups.getValue());

        // Show notification
        app.getMainWindow().showNotification(app.getMessage(Messages.UserCreatedMessage) + getItemDataSource().getItemProperty("id").toString());

        // Clear data source
        setItemDataSource(null);

        // Clear groups
        groups.setValue(null);

        // Close window
        app.getMainWindow().removeWindow(getWindow());

      } catch (InvalidValueException e) {
        // Failed to commit. The validation errors are
        // automatically shown to the user.
      }
    } else if (source == close) {

      // Clear data source
      setItemDataSource(null);

      // Clear groups
      groups.setValue(null);

      // Close window
      app.getMainWindow().removeWindow(getWindow());
    }
  }
}
