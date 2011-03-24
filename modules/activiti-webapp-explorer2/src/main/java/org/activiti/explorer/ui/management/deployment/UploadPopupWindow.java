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
package org.activiti.explorer.ui.management.deployment;

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class UploadPopupWindow extends Window 
  implements StartedListener, FinishedListener, FailedListener, ProgressListener, DropHandler {
  
  private static final long serialVersionUID = 1L;
  
  protected ProgressIndicator progressIndicator;
  protected VerticalLayout layout;
  protected Upload upload;

  public UploadPopupWindow(String caption, String description, Receiver receiver) {
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    center();
    setCaption(caption);
    
    initLayout();
    addDescription(description);
    addUpload(receiver);
    addOrLabel();
    addDropPanel();
  }

  protected void initLayout() {
    this.layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setSizeFull();
    setContent(layout);
  }
  
  protected void addDescription(String description) {
    Label descriptionLabel = new Label(description);
    descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
    descriptionLabel.addStyleName(Constants.STYLE_DEPLOYMENT_UPLOAD_DESCRIPTION);
    layout.addComponent(descriptionLabel);
  }
  
  protected void addUpload(Receiver receiver) {
    this.upload = new Upload(null, receiver);
    upload.addStyleName(Constants.STYLE_DEPLOYMENT_UPLOAD_BUTTON);
    upload.setButtonCaption("Choose a file");
    upload.setImmediate(true);
    layout.addComponent(upload);
    layout.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
    
    // upload listeners
    upload.addListener((StartedListener) this);
    upload.addListener((FailedListener) this);
    upload.addListener((FinishedListener) this);
    upload.addListener((ProgressListener) this);
  }
  
  protected void addOrLabel() {
    Label orLabel = new Label("or");
    orLabel.setSizeUndefined();
    orLabel.addStyleName(Reindeer.LABEL_SMALL);
    layout.addComponent(orLabel);
    layout.setComponentAlignment(orLabel, Alignment.MIDDLE_CENTER);
  }

  protected void addDropPanel() {
    Panel dropPanel = new Panel();
    DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(dropPanel);
    dragAndDropWrapper.setWidth("80%");
    layout.addComponent(dragAndDropWrapper);
    layout.setComponentAlignment(dragAndDropWrapper, Alignment.MIDDLE_CENTER);
    
    Label dropLabel = new Label("Drop file here");
    dropLabel.setSizeUndefined();
    dropPanel.addComponent(dropLabel);
    ((VerticalLayout)dropPanel.getContent()).setComponentAlignment(dropLabel, Alignment.MIDDLE_CENTER);
    
   
  }

  public void updateProgress(long readBytes, long contentLength) {
    progressIndicator.setValue(new Float(readBytes / (float) contentLength));
  }

  public void uploadFinished(FinishedEvent event) {
    close();
  }

  public void uploadStarted(StartedEvent event) {
    removeAllComponents(); // Visible components are replaced by a progress bar
    
    this.progressIndicator = new ProgressIndicator();
    progressIndicator.setPollingInterval(500);
    layout.addComponent(progressIndicator);
    layout.setComponentAlignment(progressIndicator, Alignment.MIDDLE_CENTER);
  }

  public void uploadFailed(FailedEvent event) {
    ExplorerApplication.getCurrent().showErrorNotification("Upload failed...", event.getReason().getMessage());
  }
  
  // Add listeners

  public void addFinishedListener(FinishedListener finishedListener) {
    upload.addListener(finishedListener);
  }
  
  public void addStartedListener(StartedListener startedListener) {
    upload.addListener(startedListener);
  }
  
  public void addFailedListener(FailedListener failedListener) {
    upload.addListener(failedListener);
  }
  
  public void addProgressListener(ProgressListener progressListener) {
    upload.addListener(progressListener);
  }
  
  // Drag and Drop support
  
  public void drop(DragAndDropEvent event) {
    System.out.println("Dropped file !");
  }
  
  public AcceptCriterion getAcceptCriterion() {
    return AcceptAll.get(); // The receiveUpload() will handle everything
  }

}
