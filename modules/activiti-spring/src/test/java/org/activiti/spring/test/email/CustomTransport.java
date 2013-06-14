package org.activiti.spring.test.email;

import java.io.IOException;

import javax.mail.*;

import org.apache.log4j.Logger;

/**
 * @author Hariprasath Manivannan
 */
public class CustomTransport extends Transport {

  static Logger logger = Logger.getLogger(CustomTransport.class.getName());
  
  public CustomTransport(Session smtpSession, URLName urlName) {
    super(smtpSession, urlName);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses) throws MessagingException {
    try {
      logger.info(message.getContent());
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Take the message and write it somewhere
    // e.g.: a logger or an OutputStream message.writeTo(...);
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