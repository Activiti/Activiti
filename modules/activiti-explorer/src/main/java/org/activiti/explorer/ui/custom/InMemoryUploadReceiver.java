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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.util.StringUtil;

import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;


/**
 * Generic {@link Receiver} for an {@link Upload} component.
 * Stores the bytes in-memory, so be careful to limit the size of the uploads. 
 * 
 * @author Joram Barrez
 */
public class InMemoryUploadReceiver implements Receiver, StartedListener, ProgressListener {
  
  private static final long serialVersionUID = 1L;
  protected NotificationManager notificationManager;
  protected Upload upload;
  protected boolean interrupted;
  protected String fileName;
  protected String mimeType;
  protected long maxFileSize;
  protected ByteArrayOutputStream outputStream;
  protected Collection<String> acceptedMimeTypes;
  
  /**
   * @param upload The component that will serve this receiver
   * @param maxFileSize The maximum size of files that will be accepted (in bytes). -1 in case of no limit.
   */
  public InMemoryUploadReceiver(Upload upload, long maxFileSize) {
    this.upload = upload;
    this.maxFileSize = maxFileSize;
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    upload.setReceiver(this);
    upload.addListener((StartedListener) this);
    upload.addListener((ProgressListener) this);
  }

  public OutputStream receiveUpload(String filename, String mimeType) {
    this.fileName = filename;
    this.mimeType = mimeType;
    this.outputStream = new ByteArrayOutputStream();
    return outputStream;
  }
  
  public void uploadStarted(StartedEvent event) {
    checkFileSize(event.getContentLength());
    if (!interrupted) { // upload can be interrupted by invalid file size
      checkMimeType(event.getMIMEType());
    }
  }

  public void updateProgress(long readBytes, long contentLength) {
    if (contentLength == -1) { // Older browsers don't support getting the filesize
      checkFileSize(readBytes);
    } else {
      checkFileSize(contentLength);
    }
  }
  
  public byte[] getBytes() {
    return outputStream.toByteArray();
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public String getMimeType() {
    return mimeType;
  }
  
  public boolean isInterruped() {
    return interrupted;
  }

  protected void checkFileSize(long receivedLength) {
    if (receivedLength > maxFileSize) {
      interrupt();
      notificationManager.showWarningNotification(Messages.UPLOAD_FAILED, 
              Messages.UPLOAD_LIMIT, maxFileSize/1024 + "kb");
    }
  }
  
  protected void checkMimeType(String mimeType) {
    if (acceptedMimeTypes != null && !acceptedMimeTypes.contains(mimeType)) {
      interrupt();
      notificationManager.showWarningNotification(Messages.UPLOAD_FAILED,
                Messages.UPLOAD_INVALID_MIMETYPE,
                StringUtil.toReadableString(acceptedMimeTypes));
    }
  }
  
  protected void interrupt() {
    upload.interruptUpload();
    interrupted = true;
  }
  
  /**
   * By default, all mime types are accepted.
   * By providing a set of mimetypes, the receiver will only work with the given mimetypes
   */
  public void setAcceptedMimeTypes(Collection<String> acceptedMimeTypes) {
    this.acceptedMimeTypes = acceptedMimeTypes;
  }
  
  public void reset() {
    interrupted = false;
    outputStream = null;
    fileName = null;
    mimeType = null;
  }
  
}
