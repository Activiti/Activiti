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

package org.activiti.engine.impl.mail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.task.AttachmentEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.json.JSONObject;

public class MailTransformer {
  
  private static Logger log = Logger.getLogger(MailTransformer.class.getName());
  
  public static int ATTACHMENT_SIZE_LIMIT = 1024*1024*10; // 10MB 
  
  protected boolean containsHtml = false;
  protected StringBuilder messageText = new StringBuilder();
  protected StringBuilder messageHtml = new StringBuilder();
  protected List<String> recipients = new ArrayList<String>();
  
  protected List<AttachmentEntity> attachments = new ArrayList<AttachmentEntity>();

  public MailTransformer(Message message) throws Exception {
    processRecipients(message);
    processContentPart(0, message);
    
    AttachmentEntity attachment = new AttachmentEntity();
    attachment.setName(message.getSubject());
    attachment.setType("email");
    attachments.add(attachment);
    
    JSONObject jsonMail = new JSONObject();
    jsonMail.put("recipients", recipients);
    jsonMail.put("sentDate", message.getSentDate());
    jsonMail.put("receivedDate", message.getReceivedDate());
    jsonMail.put("subject", message.getSubject());
    jsonMail.put("htmlContent", getHtml());
    String jsonMailString = jsonMail.toString(2);
    byte[] bytes = jsonMailString.getBytes();
    attachment.setContent(new ByteArrayEntity(bytes));
    
    log.fine("=== json ==========================");
    log.fine(jsonMailString);

    log.fine("=== attachments ==========================");
    for (AttachmentEntity attachmentForLogging: attachments) {
      log.fine(attachmentForLogging.getName()+" | "+attachmentForLogging.getType()+" | "+attachmentForLogging.getContent().getBytes().length);
    }
  }

  public String getHtml() {
    return (containsHtml ? messageHtml.toString() : messageText.toString());
  }

  public List<AttachmentEntity> getAttachments() {
    return attachments;
  }
  
  protected void processContentPart(int indent, Part part) throws Exception {
    if (part.getContent() instanceof MimeMultipart) {
      log(indent, "--- multipart "+getMimeType(part)+" ----------------------------------");
      MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();
      for (int i=0; i<mimeMultipart.getCount(); i++) {
        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
        processContentPart(indent+1, bodyPart);
      }
      
    } else {
      log(indent, "--- part "+getMimeType(part)+" ----------------------------------");
      if (part.isMimeType("text/plain")) {
        String contentText = (String) part.getContent();
        log(indent, "adding plain text: "+contentText);
        messageText.append(contentText);
        
      } else if (part.isMimeType("text/html")){
        String rawHtml = (String) part.getContent();
        log(indent, "raw html: "+rawHtml);
        String cleanedUpHtml = htmlExtractBodyContent(rawHtml);
        log(indent, "adding cleaned up html: "+cleanedUpHtml);
        containsHtml = true;
        messageHtml.append(cleanedUpHtml);

      } else {
        String fileName = part.getFileName();
        log(indent, "unknown content part | "+part.getContentType()+" | "+part.getDisposition()+" | "+Arrays.toString(part.getHeader("Content-ID"))+" | "+fileName+" | "+part.getContent().getClass().getName());
        
        if (part.getSize()!=-1 && part.getSize()<ATTACHMENT_SIZE_LIMIT && (part.getContent() instanceof InputStream)) {
          String attachmentName = null;
          String attachmentType = null;
          String[] contentIdArray = part.getHeader("Content-ID");
          if (contentIdArray!=null && contentIdArray.length>0) {
            attachmentName = contentIdArray[0].trim();
            if (attachmentName.startsWith("<") && attachmentName.endsWith(">")) {
              attachmentName = attachmentName.substring(1, attachmentName.length()-2).trim();
            }
            attachmentType = "email-inline-image";
          } else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
            attachmentName = fileName;
            attachmentType = "email-inline-image";
            messageText.append("<img id=\"cid:"+fileName+"\" src=\"cid:"+fileName+"\" />");
            messageHtml.append("<img id=\"cid:"+fileName+"\" src=\"cid:"+fileName+"\" />");
          }
          if (attachmentName==null) {
            attachmentName = fileName;
            attachmentType = "email-attachment";
          }

          AttachmentEntity attachment = new AttachmentEntity();
          attachment.setName(attachmentName);
          attachment.setType(attachmentType);
          attachments.add(attachment);
          
          byte[] bytes = IoUtil.readInputStream((InputStream)part.getContent(), "mail attachment "+attachmentName);
          attachment.setContent(new ByteArrayEntity(bytes));
        }
      }
    }
    
  }
  
  private static final String INDENT_WHITESPACE = "                                                                                ";
  void log(int indent, String msg) {
    log.fine(INDENT_WHITESPACE.substring(0, indent*2)+msg.replaceAll("\\s", " "));
  }

  protected void processRecipients(Message message) throws Exception {
    for (Address recipientAddress: message.getAllRecipients()) {
      String recipient = recipientAddress.toString();
      int ltIndex = recipient.indexOf('<');
      int atIndex = recipient.indexOf('@');
      int gtIndex = recipient.indexOf('>');
      if ( (ltIndex!=-1)
           && (atIndex!=-1)
           && (gtIndex!=-1)
           && (ltIndex<atIndex)
           && (atIndex<gtIndex) ) {
        recipient = recipient.substring(ltIndex+1, gtIndex);
      }
      recipients.add(recipient);
    }
  }

  public String htmlExtractBodyContent(String rawHtml) {
    String html = rawHtml.trim();
    html = htmlRemoveOuterTag(html, "html");
    html = html.trim();
    html = htmlRemoveStartTag(html, "head");
    html = htmlRemoveOuterTag(html, "body");
    html = htmlRemoveStartTag(html, "head");
    return html;
  }

  protected String htmlRemoveStartTag(String html, String tagName) {
    int endIndex = -1;
    if (html.startsWith("<"+tagName)) {
      endIndex = html.indexOf("</"+tagName+">");
    } else if (html.startsWith("<"+tagName.toUpperCase())) {
      endIndex = html.indexOf("</"+tagName.toUpperCase()+">");
    }
    if (endIndex!=-1) {
      endIndex += tagName.length()+3;
      html = html.substring(endIndex);
    }
    return html;
  }

  protected String htmlRemoveOuterTag(String html, String tagName) {
    html = html.trim();
    if ( (html.startsWith("<"+tagName))
         || (html.startsWith("<"+tagName.toUpperCase()))
       ) {
      int endIndex = html.indexOf('>');
      html = html.substring(endIndex+1, html.length()-(tagName.length()+3));
    }
    return html;
  }

  public String getMimeType(Part part) throws Exception {
    String mimeType = part.getContentType();
    int semicolonIndex = mimeType.indexOf(';');
    if (semicolonIndex!=-1) {
      mimeType = mimeType
        .substring(0, semicolonIndex);
    }
    mimeType = mimeType.trim();
    mimeType = mimeType.toLowerCase();
    return mimeType;
  }

  
  public List<String> getRecipients() {
    return recipients;
  }
  
  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }
}