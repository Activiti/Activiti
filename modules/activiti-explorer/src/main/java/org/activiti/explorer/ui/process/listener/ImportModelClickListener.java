package org.activiti.explorer.ui.process.listener;

import org.activiti.editor.ui.ImportUploadReceiver;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.ImportPopupWindow;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class ImportModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  public void buttonClick(ClickEvent event) {
    ImportUploadReceiver receiver = new ImportUploadReceiver();
    ImportPopupWindow importPopupWindow = new ImportPopupWindow(
            ExplorerApp.get().getI18nManager().getMessage(Messages.MODEL_IMPORT),
            ExplorerApp.get().getI18nManager().getMessage(Messages.MODEL_IMPORT_DESCRIPTION), receiver);
    
    importPopupWindow.addFinishedListener(receiver);
    ExplorerApp.get().getViewManager().showPopupWindow(importPopupWindow);
  }
}
