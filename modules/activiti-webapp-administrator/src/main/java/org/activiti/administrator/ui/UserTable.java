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

import java.util.HashSet;
import java.util.Set;
import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import org.activiti.administrator.AdminApp;
import org.activiti.administrator.ui.i18n.Messages;

/**
 * User table
 * 
 * @author Patrick Oberg
 * 
 */
public class UserTable extends Table implements ClickListener, ItemClickListener, CloseListener {

  private static final long serialVersionUID = 1L;
  private final AdminApp app;
  private UserEditForm form;
  private Window popupWindow;
  HashSet<Object> markedRows = new HashSet<Object>();
  private final Action ACTION_EDIT;
  private final Action ACTION_DELETE;
  private final Action[] ACTIONS_UNMARKED;
  private final Action[] ACTIONS_MARKED;
  private final Object[] columnHeaders = new Object[] { "id", "email", "firstName", "lastName" };

  /**
   * Create the user table
   * 
   * @param app
   */
  @SuppressWarnings("serial")
  public UserTable(AdminApp application) {

    // Set application reference
    this.app = application;

    // Maximize the table width and height
    setSizeFull();

    // Set infinite page length
    setPageLength(size());

    // Remove border
    // addStyleName(Reindeer.TABLE_BORDERLESS);

    // Allow selecting items from the table.
    setSelectable(true);

    // Send changes in selection immediately to server.
    setImmediate(true);

    // Disable null selection
    setNullSelectionAllowed(false);

    // Allow column to collapse
    setColumnCollapsingAllowed(true);

    // Allow reordering of columns
    setColumnReorderingAllowed(true);

    // Add table data source
    setContainerDataSource(app.getAdminService().getUsers());

    // Set column headers
    setColumnHeader("id", "Id");
    setColumnHeader("email", "Email");
    setColumnHeader("firstName", "First Name");
    setColumnHeader("lastName", "Last Name");

    // Set column headers alignment
    setColumnAlignment("id", ALIGN_LEFT);
    setColumnAlignment("email", ALIGN_LEFT);
    setColumnAlignment("firstName", ALIGN_LEFT);
    setColumnAlignment("lastName", ALIGN_LEFT);

    // Define the names and data types of columns.
    setVisibleColumns(columnHeaders);

    // Add table listener
    addListener((ItemClickListener) this);

    // Init actions must be set here to use i18n support
    ACTION_EDIT = new Action(app.getMessage(Messages.Edit));
    ACTION_DELETE = new Action(app.getMessage(Messages.Delete));
    ACTIONS_UNMARKED = new Action[] { ACTION_EDIT, ACTION_DELETE };
    ACTIONS_MARKED = new Action[] { ACTION_EDIT, ACTION_DELETE };

    // Actions (a.k.a context menu)
    addActionHandler(new Action.Handler() {

      public Action[] getActions(Object target, Object sender) {
        if (markedRows.contains(target)) {
          return ACTIONS_MARKED;
        } else {
          return ACTIONS_UNMARKED;
        }
      }

      public void handleAction(Action action, Object sender, Object target) {
        if (ACTION_EDIT.equals(action)) {
          editUser(target);
        } else if (ACTION_DELETE.equals(action)) {
          deleteUser(target);
        }
      }
    });
  }

  /**
   * Edit the user
   * 
   * @param itemId
   *          the BeanItem id of the user
   */
  private void editUser(Object itemId) {

    Item item = getItem(itemId);

    // Edit form
    if (form == null) {
      form = new UserEditForm(app);
    }

    if (item != form.getItemDataSource()) {

      // Set double-clicked item
      form.setItemDataSource(item);

      // Create a new popup window
      popupWindow = new Window();
      popupWindow.center();
      popupWindow.setModal(true);
      popupWindow.setWidth("400px");
      popupWindow.addStyleName(Consts.POPUP);

      // Set caption
      popupWindow.setCaption(app.getMessage(Messages.EditUser));

      // Add form
      popupWindow.addComponent(form);

      // Listen for close events for the window.
      popupWindow.addListener(this);
    }

    // Add the window inside the main window.
    app.getMainWindow().addWindow(popupWindow);

  }

  /**
   * Delete the user
   * 
   * @param target
   *          the BeanItem id of the user
   */
  private void deleteUser(final Object target) {

    // Get user id
    final String id = getItem(target).getItemProperty("id").toString();

    // Get task ids assigned to the user
    Set<String> assignedTasks = app.getAdminService().getAssignedTaskIds(id);

    // Get task ids waiting to be assigned to the user
    Set<String> unassignedTasks = app.getAdminService().getUnassignedTaskIds(id);

    // Create pre-formatted confirm dialog message about out deleting the
    // user
    String message = "";

    if (!assignedTasks.isEmpty() || !unassignedTasks.isEmpty()) {

      // One or more tasks will be affected if deleting the user
      message = app.getMessage(Messages.DeleteUserAffectsTasks) + "<ul>";

      if (!assignedTasks.isEmpty()) {

        // One or more assigned tasks will be inaccessible
        message = message + "<li>" + app.getMessage(Messages.AssignedTasks) + assignedTasks + "</li>";
      }

      if (!unassignedTasks.isEmpty()) {

        // One or more unassigned tasks will be inaccessible
        message = message + "<li>" + app.getMessage(Messages.UnassignedTasks) + unassignedTasks + "</li>";
      }

      // Insert end of list
      message = message + "</ul><br/>";

    } else {

      // Info when no task is affected
      message = app.getMessage(Messages.DeleteUserAffectsNoTasks) + "<br/><br/>";

    }

    // Show delete user confirmation dialog
    ConfirmDialog dialog = new ConfirmDialog(app.getMessage(Messages.ConfirmDelete) + id, message, app.getMessage(Messages.OkKey),
            app.getMessage(Messages.CancelKey), new ConfirmDialog.ConfirmationDialogCallback() {

              @Override
              public void response(boolean ok) {

                // Delete user confirmed?
                if (ok) {

                  // Delete user
                  app.getAdminService().deleteUser(id);

                  // Remove user from container
                  app.getAdminService().getUsers().removeItem(target);

                  // Show confirmation
                  app.getMainWindow().showNotification(app.getMessage(Messages.DeletedUser) + id);
                }
              }
            });

    // Add dialog styles
    dialog.addStyleName(Consts.POPUP);

    // Open the confirmation dialog
    app.getMainWindow().addWindow(dialog);

  }

  public void refresh() {

    // Refresh users
    app.getAdminService().refreshUsers();

    // Add table data source
    setContainerDataSource(app.getAdminService().getUsers());

    // Define the names and data types of columns.
    setVisibleColumns(columnHeaders);

  }

  @Override
  public void itemClick(ItemClickEvent event) {
    if (event.isDoubleClick()) {
      // Edit user
      editUser(event.getItemId());
    }
  }

  @Override
  public void buttonClick(ClickEvent event) {

    // TODO Auto-generated method stub

  }

  @Override
  public void windowClose(CloseEvent e) {
    // TODO Auto-generated method stub

  }

}
