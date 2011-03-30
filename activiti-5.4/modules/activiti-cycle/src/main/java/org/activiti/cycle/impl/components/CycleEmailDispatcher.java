package org.activiti.cycle.impl.components;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;

/**
 * {@link CycleComponent} for asynchronous email-dispatching (extremely basic
 * implementation)
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class CycleEmailDispatcher {

  private static int QUEUE_SIZE = 100;

  Logger logger = Logger.getLogger(CycleEmailDispatcher.class.getName());

  private final BlockingQueue<EmailDto> emailQueue = new ArrayBlockingQueue<EmailDto>(QUEUE_SIZE);

  private class EmailDto {
    private String from, address, subject, message;
    public String toString() {
      return "Email from '"+from+"' to '" + address + "' with subject '" + subject + "'";
    }
  }

  public void sendEmail(String from, String address, String subject, String message) {
    EmailDto dto = new EmailDto();
    dto.address = address;
    dto.from = from;
    dto.subject = subject;
    dto.message = message;
    sendEmail(dto);
  }

  private void sendEmail(EmailDto email) {
    // if the queue is full, the email is simply not added.
    emailQueue.offer(email);
    startDispatchment();
  }

  public synchronized void startDispatchment() {
    if (!emailDispatcherThread.isAlive()) {
      try {
        emailDispatcherThread.start();
      } catch (Exception e) {
        emailDispatcherThread = new EmailDispatcherThread();
        emailDispatcherThread.start();
      }
    }
  }

  public synchronized void stopDispatchment() {
    if (!emailDispatcherThread.isInterrupted())
      emailDispatcherThread.interrupt();
  }

  private Thread emailDispatcherThread = new EmailDispatcherThread();

  private class EmailDispatcherThread extends Thread {

    public void run() {
      while (!isInterrupted()) {
        EmailDto mailDto = null;
        try {
          mailDto = emailQueue.take();
          Email email = new HtmlEmail();
          email.setFrom(mailDto.from);
          email.setMsg(mailDto.message);
          email.setSubject(mailDto.subject);
          email.addTo(mailDto.address);
          setMailServerProperties(email);
          email.send();
        } catch (InterruptedException e) {
          // just terminate
          return;
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Could not send " + mailDto, e);
          // TODO: retry?
        }
      }
    }
  }

  /* copied from MailActivityBehaviour in engine */
  protected void setMailServerProperties(Email email) {
    // for the moment, simply reuse activiti-engine mailconfiguration
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine()).getProcessEngineConfiguration();

    String host = processEngineConfiguration.getMailServerHost();
    email.setHostName(host);

    int port = processEngineConfiguration.getMailServerPort();
    email.setSmtpPort(port);

    String user = processEngineConfiguration.getMailServerUsername();
    String password = processEngineConfiguration.getMailServerPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }
  }/* end copied from MailActivityBehaviour in engine */

}
