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

import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.test.Deployment;
import org.subethamail.wiser.WiserMessage;


/**
 * @author Joram Barrez
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
		vars.put("text", "Hello ${gender == 'male' ? 'Mr' : 'Ms' }. Kermit");
		runtimeService.startProcessInstanceByKey("variableTemplatedMail", vars);
    
    List<WiserMessage> messages = wiser.getMessages();
		assertEquals(1, messages.size());
    assertEmailSend(messages.get(0), true, "Test", "Mr. <b>Kermit</b>", "activiti@localhost", Arrays.asList("kermit@activiti.org"), null);
  }
  
  // Helper 
  
  public static void assertEmailSend(WiserMessage emailMessage, boolean htmlMail, String subject, String message, 
          String from, List<String> to, List<String> cc) throws IOException {
    try {
      MimeMessage mimeMessage = emailMessage.getMimeMessage();
      
      if (htmlMail) {
        assertTrue(mimeMessage.getContentType().contains("multipart/mixed"));
      } else {
        assertTrue(mimeMessage.getContentType().contains("text/plain"));
      }
      
      assertEquals(subject, mimeMessage.getHeader("Subject", null));
      assertEquals("\"" + from + "\" <" +from.toString() + ">" , mimeMessage.getHeader("From", null));
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
