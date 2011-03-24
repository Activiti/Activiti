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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Html5File;
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
  
  // Ui components
  protected ProgressIndicator progressIndicator;
  protected VerticalLayout layout;
  protected Upload upload;
  protected Receiver receiver;
  
  // Additional listeners
  protected List<FinishedListener> finishedListeners = new ArrayList<FinishedListener>();
  protected List<StartedListener> startedListeners = new ArrayList<StartedListener>();
  protected List<FailedListener> failedListeners = new ArrayList<FailedListener>();
  protected List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();


  public UploadPopupWindow(String caption, String description, Receiver receiver) {
    this.receiver = receiver;
    initWindow(caption);
    addDescription(description);
    addUpload();
    addOrLabel();
    addDropPanel();
  }
  
  // UI initialisation ----------------------------------------------------------------------------

  protected void initWindow(String caption) {
    // Fixed width/height since otherwise the layout can be screwed by the drag and drop
    setWidth("300px");
    setHeight("300px");
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    center();
    setCaption(caption);
    
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
  
  protected void addUpload() {
    this.upload = new Upload(null, receiver);
    upload.addStyleName(Constants.STYLE_DEPLOYMENT_UPLOAD_BUTTON);
    upload.setButtonCaption("Choose a file");
    upload.setImmediate(true);
    layout.addComponent(upload);
    layout.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
    
    // register ourselves as listener for upload events
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
    dragAndDropWrapper.setDropHandler(this);
    dragAndDropWrapper.setWidth("80%");
    layout.addComponent(dragAndDropWrapper);
    layout.setComponentAlignment(dragAndDropWrapper, Alignment.MIDDLE_CENTER);
    
    Label dropLabel = new Label("Drop file here");
    dropLabel.setSizeUndefined();
    dropPanel.addComponent(dropLabel);
    ((VerticalLayout)dropPanel.getContent()).setComponentAlignment(dropLabel, Alignment.MIDDLE_CENTER);
  }
  
  // File upload event handling -------------------------------------------------------------------
  
  public void uploadStarted(StartedEvent event) {
    removeAllComponents(); // Visible components are replaced by a progress bar
    
    this.progressIndicator = new ProgressIndicator();
    progressIndicator.setPollingInterval(500);
    layout.addComponent(progressIndicator);
    layout.setComponentAlignment(progressIndicator, Alignment.MIDDLE_CENTER);
    
    for (StartedListener startedListener : startedListeners) {
      startedListener.uploadStarted(event);
    }
  }
  
  public void updateProgress(long readBytes, long contentLength) {
    progressIndicator.setValue(new Float(readBytes / (float) contentLength));
    
    for (ProgressListener progressListener : progressListeners) {
      progressListener.updateProgress(readBytes, contentLength);
    }
  }

  public void uploadFinished(FinishedEvent event) {
    close();
    
    for (FinishedListener finishedListener : finishedListeners) {
      finishedListener.uploadFinished(event);
    }
  }

  public void uploadFailed(FailedEvent event) {
    uploadFailed(event.getReason().getMessage());
    
    for (FailedListener failedListener : failedListeners) {
      failedListener.uploadFailed(event);
    }
  }
  
  protected void uploadFailed(String errorMessage) {
    ExplorerApplication.getCurrent().showErrorNotification("Upload failed...", errorMessage);
  }
  
  // Drag and drop handling (DropHandler) ---------------------------------------------------------
  
  public void drop(DragAndDropEvent event) {
    WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
    Html5File[] files = transferable.getFiles();
    if (files.length > 0) {
      final Html5File file = files[0]; // only support for one file upload at this moment
      file.setStreamVariable(new StreamVariable() {
        public void streamingStarted(StreamingStartEvent event) {
          uploadStarted(null); // event doesnt matter here
        }
        public void streamingFinished(StreamingEndEvent event) {
          uploadFinished(null); // event doesnt matter here
        }
        public void streamingFailed(StreamingErrorEvent event) {
          uploadFailed(event.getException().getMessage());
        }
        public void onProgress(StreamingProgressEvent event) {
          updateProgress(event.getBytesReceived(), event.getContentLength());
        }
        public boolean listenProgress() {
          return true;
        }
        public boolean isInterrupted() {
          return false;
        }
        public OutputStream getOutputStream() {
          return receiver.receiveUpload(file.getFileName(), file.getType());
        }
      });
    }
  }
  
  public AcceptCriterion getAcceptCriterion() {
    return AcceptAll.get();
  }
  
  // Upload Listeners ----------------------------------------------------------------------------
  
  public void addFinishedListener(FinishedListener finishedListener) {
    finishedListeners.add(finishedListener);
  }
  
  public void addStartedListener(StartedListener startedListener) {
    startedListeners.add(startedListener);
  }
  
  public void addFailedListener(FailedListener failedListener) {
    failedListeners.add(failedListener);
  }
  
  public void addProgressListener(ProgressListener progressListener) {
    progressListeners.add(progressListener);
  }
  
  
}
