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

package org.activiti.explorer.ui.custom;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

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
import com.vaadin.ui.themes.Reindeer;


/**
 * Component that can be used to upload files using file-select and 
 * HTML5 file dragging. Exposes events to listen to upload start, progress, 
 * finish and cancel.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class UploadComponent extends VerticalLayout implements StartedListener, FinishedListener, 
  FailedListener, ProgressListener, DropHandler {
  
  private static final long serialVersionUID = 1L;
  
  // Services
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  
  // Ui components
  protected ProgressIndicator progressIndicator;
  protected Upload upload;
  protected Receiver receiver;
  protected Label descriptionLabel;
  protected boolean enableDrop = true;
  
  // Additional listeners can be attached to the upload components
  protected List<FinishedListener> finishedListeners = new ArrayList<FinishedListener>();
  protected List<StartedListener> startedListeners = new ArrayList<StartedListener>();
  protected boolean showGenericFailureMessage = true;
  protected List<FailedListener> failedListeners = new ArrayList<FailedListener>();
  protected List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
  


  public UploadComponent(String description, Receiver receiver) {
    this.receiver = receiver;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    init();
  }
  
  public UploadComponent(boolean enableDrop) {
    this(null, null);
    enableDrop = true;
  }
  
  public void setDescription(String description) {
    if(description != null) {
      descriptionLabel.setValue(description);
      descriptionLabel.setVisible(true);
    } else {
      descriptionLabel.setVisible(false);
    }
  }
  
  public void setReceiver(Receiver receiver) {
  	this.receiver = receiver;
    upload.setReceiver(receiver);
  }

  // UI initialisation ----------------------------------------------------------------------------
  
  protected void init() {
    setSpacing(true);
    setSizeFull();
    
    addDescription();
    addUpload();
    
    if(enableDrop) {
      addOrLabel();
      addDropPanel();
    }
  }

  protected void addDescription() {
      descriptionLabel = new Label();
      descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
      descriptionLabel.addStyleName(ExplorerLayout.STYLE_DEPLOYMENT_UPLOAD_DESCRIPTION);
      descriptionLabel.setVisible(false);
      addComponent(descriptionLabel);      
  }
  
  protected void addUpload() {
    this.upload = new Upload(null, receiver);
    upload.setButtonCaption(i18nManager.getMessage(Messages.UPLOAD_SELECT));
    upload.setImmediate(true);
    addComponent(upload);
    setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
    
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
    addComponent(orLabel);
    setComponentAlignment(orLabel, Alignment.MIDDLE_CENTER);
  }

  protected void addDropPanel() {
    Panel dropPanel = new Panel();
    DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(dropPanel);
    dragAndDropWrapper.setDropHandler(this);
    dragAndDropWrapper.setWidth("80%");
    addComponent(dragAndDropWrapper);
    setComponentAlignment(dragAndDropWrapper, Alignment.MIDDLE_CENTER);
    
    Label dropLabel = new Label(i18nManager.getMessage(Messages.UPLOAD_DROP));
    dropLabel.setSizeUndefined();
    dropPanel.addComponent(dropLabel);
    ((VerticalLayout)dropPanel.getContent()).setComponentAlignment(dropLabel, Alignment.MIDDLE_CENTER);
  }
  
  // File upload event handling -------------------------------------------------------------------
  
  public void uploadStarted(StartedEvent event) {
    removeAllComponents(); // Visible components are replaced by a progress bar
    
    this.progressIndicator = new ProgressIndicator();
    progressIndicator.setPollingInterval(500);
    addComponent(progressIndicator);
    setComponentAlignment(progressIndicator, Alignment.MIDDLE_CENTER);
    
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
    // Hide progress indicator
    progressIndicator.setVisible(false);
    for (FinishedListener finishedListener : finishedListeners) {
      finishedListener.uploadFinished(event);
    }
  }

  public void uploadFailed(FailedEvent event) {
    for (FailedListener failedListener : failedListeners) {
      failedListener.uploadFailed(event);
    }
  }
  
  // Drag and drop handling (DropHandler) ---------------------------------------------------------
  
  public void drop(DragAndDropEvent event) {
    WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
    Html5File[] files = transferable.getFiles();
    if (files != null && files.length > 0) {
      final Html5File file = files[0]; // only support for one file upload at this moment
      file.setStreamVariable(new StreamVariable() {
        
        private static final long serialVersionUID = 1L;
        
        public void streamingStarted(StreamingStartEvent event) {
          uploadStarted(null); // event doesnt matter here
        }
        public void streamingFinished(StreamingEndEvent event) {
          uploadFinished(null); // event doesnt matter here
        }
        public void streamingFailed(StreamingErrorEvent event) {
          uploadFailed(null);
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
