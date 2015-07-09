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

package org.activiti.engine.test.bpmn.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.test.Deployment;
import org.subethamail.wiser.WiserMessage;


/**
 * @author Joram Barrez
 * @author Tim Stephenson
 */
public class EmailServiceTaskTest extends EmailTestCase {

  @Deployment
  public void testSimpleTextMail() throws Exception {
    String procId = runtimeService.startProcessInstanceByKey("simpleTextOnly").getId();

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "activiti@localhost",
        Arrays.asList("kermit@activiti.org"), null);
    assertProcessEnded(procId);
  }

  public void testSimpleTextMailWhenMultiTenant() throws Exception {
    String tenantId = "myEmailTenant";

    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/mail/EmailSendTaskTest.testSimpleTextMail.bpmn20.xml").tenantId(tenantId).deploy();
    String procId = runtimeService.startProcessInstanceByKeyAndTenantId("simpleTextOnly", tenantId).getId();

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "activiti@myTenant.com",
        Arrays.asList("kermit@activiti.org"), null);
    assertProcessEnded(procId);

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testSimpleTextMailForNonExistentTenant() throws Exception {
    String tenantId = "nonExistentTenant";

    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/mail/EmailSendTaskTest.testSimpleTextMail.bpmn20.xml").tenantId(tenantId).deploy();
    String procId = runtimeService.startProcessInstanceByKeyAndTenantId("simpleTextOnly", tenantId).getId();

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, "Hello Kermit!", "This a text only e-mail.", "activiti@localhost",
        Arrays.asList("kermit@activiti.org"), null);
    assertProcessEnded(procId);

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment
  public void testSimpleTextMailMultipleRecipients() {
    runtimeService.startProcessInstanceByKey("simpleTextOnlyMultipleRecipients");

    // 3 recipients == 3 emails in wiser with different receivers
    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(3, messages.size());

    // sort recipients for easy assertion
    List<String> recipients = new ArrayList<String>();
    for (WiserMessage message : messages) {
      recipients.add(message.getEnvelopeReceiver());
    }
    Collections.sort(recipients);

    assertEquals("fozzie@activiti.org", recipients.get(0));
    assertEquals("kermit@activiti.org", recipients.get(1));
    assertEquals("mispiggy@activiti.org", recipients.get(2));
  }

  @Deployment
  public void testTextMailExpressions() throws Exception {

    String sender = "mispiggy@activiti.org";
    String recipient = "fozziebear@activiti.org";
    String recipientName = "Mr. Fozzie";
    String subject = "Fozzie, you should see this!";

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sender", sender);
    vars.put("recipient", recipient);
    vars.put("recipientName", recipientName);
    vars.put("subject", subject);

    runtimeService.startProcessInstanceByKey("textMailExpressions", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());

    WiserMessage message = messages.get(0);
    assertEmailSend(message, false, subject, "Hello " + recipientName + ", this is an e-mail",
        sender, Arrays.asList(recipient), null);
  }

  @Deployment
  public void testCcAndBcc() throws Exception {
    runtimeService.startProcessInstanceByKey("ccAndBcc");

    List<WiserMessage> messages = wiser.getMessages();
    assertEmailSend(messages.get(0), false, "Hello world", "This is the content", "activiti@localhost",
        Arrays.asList("kermit@activiti.org"), Arrays.asList("fozzie@activiti.org"));

    // Bcc is not stored in the header (obviously)
    // so the only way to verify the bcc, is that there are three messages send.
    assertEquals(3, messages.size());
  }

  @Deployment
  public void testHtmlMail() throws Exception {
    runtimeService.startProcessInstanceByKey("htmlMail", CollectionUtil.singletonMap("gender", "male"));

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    assertEmailSend(messages.get(0), true, "Test", "Mr. <b>Kermit</b>", "activiti@localhost", Arrays.asList("kermit@activiti.org"), null);
  }

  @Deployment
  public void testVariableTemplatedMail() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("gender", "male");
    vars.put("html", "<![CDATA[<html><body>Hello ${gender == 'male' ? 'Mr' : 'Ms' }. <b>Kermit</b><body></html>]]");
    runtimeService.startProcessInstanceByKey("variableTemplatedMail", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    assertEmailSend(messages.get(0), true, "Test", "Mr. <b>Kermit</b>", "activiti@localhost", Arrays.asList("kermit@activiti.org"), null);
  }

  @Deployment
  public void testTextMailWithFileAttachment() throws Exception {
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    runtimeService.startProcessInstanceByKey("textMailWithFileAttachment", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    MimeMultipart mm = (MimeMultipart) message.getMimeMessage().getContent();
    assertEquals(2, mm.getCount());
    String attachmentFileName = mm.getBodyPart(1).getDataHandler().getName();
    assertEquals(new AttachmentsBean().getFile().getName(), attachmentFileName);
  }

  @Deployment
  public void testTextMailWithFileAttachments() throws Exception {
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    runtimeService.startProcessInstanceByKey("textMailWithFileAttachments", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    MimeMultipart mm = (MimeMultipart) message.getMimeMessage().getContent();
    File[] files = new AttachmentsBean().getFiles();
    assertEquals(1 + files.length, mm.getCount());
    for (int i = 0; i < files.length; i++) {
      String attachmentFileName = mm.getBodyPart(1 + i).getDataHandler().getName();
      assertEquals(files[i].getName(), attachmentFileName);
    }
  }

  @Deployment
  public void testTextMailWithFileAttachmentsByPath() throws Exception {
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    runtimeService.startProcessInstanceByKey("textMailWithFileAttachmentsByPath", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    MimeMultipart mm = (MimeMultipart) message.getMimeMessage().getContent();
    File[] files = new AttachmentsBean().getFiles();
    assertEquals(1 + files.length, mm.getCount());
    for (int i = 0; i < files.length; i++) {
      String attachmentFileName = mm.getBodyPart(1 + i).getDataHandler().getName();
      assertEquals(files[i].getName(), attachmentFileName);
    }
  }

  @Deployment
  public void testTextMailWithDataSourceAttachment() throws Exception {
    String fileName = "file-name-to-be-displayed";
    String fileContent = "This is the file content";
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    vars.put("fileContent", fileContent);
    vars.put("fileName", fileName);
    runtimeService.startProcessInstanceByKey("textMailWithDataSourceAttachment", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    MimeMultipart mm = (MimeMultipart) message.getMimeMessage().getContent();
    assertEquals(2, mm.getCount());
    String attachmentFileName = mm.getBodyPart(1).getDataHandler().getName();
    assertEquals(fileName, attachmentFileName);
  }

  @Deployment
  public void testTextMailWithNotExistingFileAttachment() throws Exception {
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    runtimeService.startProcessInstanceByKey("textMailWithNotExistingFileAttachment", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    assertFalse(message.getMimeMessage().getContent() instanceof MimeMultipart);
  }

  @Deployment
  public void testHtmlMailWithFileAttachment() throws Exception {
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("attachmentsBean", new AttachmentsBean());
    vars.put("gender", "male");
    runtimeService.startProcessInstanceByKey("htmlMailWithFileAttachment", vars);

    List<WiserMessage> messages = wiser.getMessages();
    assertEquals(1, messages.size());
    WiserMessage message = messages.get(0);
    MimeMultipart mm = (MimeMultipart) message.getMimeMessage().getContent();
    assertEquals(2, mm.getCount());
    String attachmentFileName = mm.getBodyPart(1).getDataHandler().getName();
    assertEquals(new AttachmentsBean().getFile().getName(), attachmentFileName);
  }

  @Deployment
  public void testInvalidAddress() throws Exception {
    try {
      runtimeService.startProcessInstanceByKey("invalidAddress").getId();
      fail("An Invalid email address should not execute");
    } catch (ActivitiException e) {
      // fine
    } catch (Exception e) {
      fail("Only an ActivitiException is expected here but not: " + e);
    }
  }

  @Deployment
  public void testInvalidAddressWithoutException() throws Exception {
    String piId = runtimeService.startProcessInstanceByKey("invalidAddressWithoutException").getId();
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertNotNull(historyService.createHistoricVariableInstanceQuery().processInstanceId(piId).variableName("emailError").singleResult());
    }
  }

  @Deployment
  public void testInvalidAddressWithoutExceptionVariableName() throws Exception {
    String piId = runtimeService.startProcessInstanceByKey("invalidAddressWithoutException").getId();
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertNull(historyService.createHistoricVariableInstanceQuery().processInstanceId(piId).variableName("emailError").singleResult());
    }
  }

  // Helper 

  public static void assertEmailSend(WiserMessage emailMessage, boolean htmlMail, String subject, String message,
                                     String from, List<String> to, List<String> cc) throws IOException {
    try {
      MimeMessage mimeMessage = emailMessage.getMimeMessage();
      System.out.println(mimeMessage.getContentType());
      if (htmlMail) {
        assertTrue(mimeMessage.getContentType().contains("multipart/mixed"));
      } else {
        assertTrue(mimeMessage.getContentType().contains("text/plain"));
      }

      assertEquals(subject, mimeMessage.getHeader("Subject", null));
      assertEquals(from, mimeMessage.getHeader("From", null));
      assertTrue(getMessage(mimeMessage).contains(message));

      for (String t : to) {
        assertTrue(mimeMessage.getHeader("To", null).contains(t));
      }

      if (cc != null) {
        for (String c : cc) {
          assertTrue(mimeMessage.getHeader("Cc", null).contains(c));
        }
      }

    } catch (MessagingException e) {
      fail(e.getMessage());
    }

  }

  public static String getMessage(MimeMessage mimeMessage) throws MessagingException, IOException {
    DataHandler dataHandler = mimeMessage.getDataHandler();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    dataHandler.writeTo(baos);
    baos.flush();
    return baos.toString();
  }

}
