package org.activiti.explorer.ui.process.listener;

import org.activiti.editor.ui.NewModelPopupWindow;
import org.activiti.explorer.ExplorerApp;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class NewModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  public void buttonClick(ClickEvent event) {
    ExplorerApp.get().getViewManager().showPopupWindow(new NewModelPopupWindow());
  }
}
