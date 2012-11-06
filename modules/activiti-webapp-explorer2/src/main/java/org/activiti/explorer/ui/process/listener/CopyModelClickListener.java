package org.activiti.explorer.ui.process.listener;

import org.activiti.editor.data.model.ModelData;
import org.activiti.editor.ui.CopyModelPopupWindow;
import org.activiti.explorer.ExplorerApp;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 */
public class CopyModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  private ModelData modelData;
  
  public CopyModelClickListener(ModelData model) {
    this.modelData = model;
  }
  
  public void buttonClick(ClickEvent event) {
    ExplorerApp.get().getViewManager().showPopupWindow(new CopyModelPopupWindow(modelData));
  }
}
