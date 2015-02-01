package org.activiti.explorer.ui.process.listener;

import org.activiti.editor.ui.ConvertProcessDefinitionPopupWindow;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.NotificationManager;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class ConvertProcessDefinitionToModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected NotificationManager notificationManager;
  protected ProcessDefinition processDefinition;
  
  public ConvertProcessDefinitionToModelClickListener(ProcessDefinition processDefinition) {
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    
    this.processDefinition = processDefinition;
  }

  public void buttonClick(ClickEvent event) {
    ExplorerApp.get().getViewManager().showPopupWindow(new ConvertProcessDefinitionPopupWindow(processDefinition));
  }
}
