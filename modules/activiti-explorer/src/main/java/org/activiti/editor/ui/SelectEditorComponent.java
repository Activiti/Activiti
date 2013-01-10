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
package org.activiti.editor.ui;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class SelectEditorComponent extends VerticalLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected boolean enableHighlightWhenClicked;
  
  protected HorizontalLayout modelerLayout;
  protected Button modelerButton;
  protected Label modelerLabel;
  protected Label modelerDescriptionLabel;
  
  protected HorizontalLayout tableEditorLayout;
  protected Button tableEditorButton;
  protected Label tableEditorLabel;
  protected Label tableEditorDescriptionLabel;
  
  protected boolean modelerPreferred;
  
  protected EditorSelectedListener editorSelectedListener;
  
  public SelectEditorComponent() {
    this(true);
  }
  
  public SelectEditorComponent(boolean enableHighlightWhenClicked) {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.enableHighlightWhenClicked = enableHighlightWhenClicked;
    
    createModelerEditorChoice();
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    createTableDrivenEditorChoice();
    
    preferModeler(); // is default to select modeler
  }
  
  protected void createModelerEditorChoice() {
    modelerLayout = new HorizontalLayout();
    modelerLayout.setWidth("300px");
    modelerLayout.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    addComponent(modelerLayout);
    
    modelerButton = new Button();
    modelerButton.setIcon(Images.PROCESS_EDITOR_BPMN);
    modelerButton.setStyleName(Reindeer.BUTTON_LINK);
    modelerLayout.addComponent(modelerButton);
    modelerLayout.setComponentAlignment(modelerButton, Alignment.MIDDLE_LEFT);
    
    VerticalLayout modelerTextLayout = new VerticalLayout();
    modelerLayout.addComponent(modelerTextLayout);
    modelerLayout.setExpandRatio(modelerTextLayout, 1.0f);
    
    modelerLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_MODELER));
    modelerLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    modelerTextLayout.addComponent(modelerLabel);
    
    modelerDescriptionLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_MODELER_DESCRIPTION));
    modelerDescriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
    modelerDescriptionLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    modelerTextLayout.addComponent(modelerDescriptionLabel);
    
    modelerLayout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        preferModeler();
      }
    });
    
    modelerButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        preferModeler();
      }
    });
  }
  
  protected void createTableDrivenEditorChoice() {
    tableEditorLayout = new HorizontalLayout();
    tableEditorLayout.setWidth("300px");
    tableEditorLayout.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    addComponent(tableEditorLayout);
    
    tableEditorButton = new Button();
    tableEditorButton.setIcon(Images.PROCESS_EDITOR_TABLE);
    tableEditorButton.setStyleName(Reindeer.BUTTON_LINK);
    tableEditorLayout.addComponent(tableEditorButton);
    tableEditorLayout.setComponentAlignment(tableEditorButton, Alignment.MIDDLE_LEFT);
    
    VerticalLayout tableEditorTextLayout = new VerticalLayout();
    tableEditorLayout.addComponent(tableEditorTextLayout);
    tableEditorLayout.setExpandRatio(tableEditorTextLayout, 1.0f);
    
    tableEditorLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_TABLE));
    tableEditorLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    tableEditorTextLayout.addComponent(tableEditorLabel);
    
    tableEditorDescriptionLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_TABLE_DESCRIPTION));
    tableEditorDescriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
    tableEditorDescriptionLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    tableEditorTextLayout.addComponent(tableEditorDescriptionLabel);
    
    
    tableEditorLayout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        preferTableDrivenEditor();
      }
    });
    
    tableEditorButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        preferTableDrivenEditor();
      }
    });
  }
  
  public void preferModeler() {
    if (!modelerPreferred) {
      modelerPreferred = true;
      
      if (enableHighlightWhenClicked) {
        selectEditor(modelerLayout);
        deselectEditor(tableEditorLayout);
      
        modelerLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
        tableEditorLabel.removeStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      }
    }
    
    if (editorSelectedListener != null) {
      editorSelectedListener.editorSelectionChanged();
    }
  }
  
  public void preferTableDrivenEditor() {
    if (modelerPreferred) {
      modelerPreferred = false;
      
      if (enableHighlightWhenClicked) {
        selectEditor(tableEditorLayout);
        deselectEditor(modelerLayout);
      
        tableEditorLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
        modelerLabel.removeStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      }
    }
    
    if (editorSelectedListener != null) {
      editorSelectedListener.editorSelectionChanged();
    }
  }
  
  protected void selectEditor(AbstractLayout editorLayout) {
    editorLayout.addStyleName(ExplorerLayout.STYLE_PROCESS_EDITOR_CHOICE);
  }
  
  protected void deselectEditor(AbstractLayout editorLayout) {
    editorLayout.removeStyleName(ExplorerLayout.STYLE_PROCESS_EDITOR_CHOICE);
  }
  
  public HorizontalLayout getModelerLayout() {
    return modelerLayout;
  }
  
  public Button getModelerButton() {
    return modelerButton;
  }
  
  public HorizontalLayout getTableEditorLayout() {
    return tableEditorLayout;
  }
  
  public Button getTableEditorButton() {
    return tableEditorButton;
  }

  
  public Label getModelerLabel() {
    return modelerLabel;
  }

  
  public Label getModelerDescriptionLabel() {
    return modelerDescriptionLabel;
  }

  
  public Label getTableEditorLabel() {
    return tableEditorLabel;
  }

  
  public Label getTableEditorDescriptionLabel() {
    return tableEditorDescriptionLabel;
  }

  
  public boolean isModelerPreferred() {
    return modelerPreferred;
  }
  
  public EditorSelectedListener getEditorSelectedListener() {
    return editorSelectedListener;
  }

  public void setEditorSelectedListener(EditorSelectedListener editorSelectedListener) {
    this.editorSelectedListener = editorSelectedListener;
  }

  
  // Helper class

  public static interface EditorSelectedListener {
    
    void editorSelectionChanged();
    
  }
  
}
