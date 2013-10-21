package org.activiti.spring.test.email;

import java.io.IOException;

import javax.mail.*;

import org.apache.log4j.Logger;

/**
 * @author Hariprasath Manivannan
 */
public class MockEmailTransport extends Transport {

  static Logger logger = Logger.getLogger(MockEmailTransport.class.getName());
  
  public MockEmailTransport(Session smtpSession, URLName urlName) {
    super(smtpSession, urlName);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses) throws MessagingException {
    try {
      logger.info(message.getContent());
    } catch (IOException ex) {
      logger.error("Error occured while sending email" + ex);
    }
  }

  @Override
  public void connect() throws MessagingException {}

  @Override
  public void connect(String host, int port, String username, String password) throws MessagingException {}

  @Override
  public void connect(String host, String username, String password) throws MessagingException {}

  @Override
  public void close() {}
}