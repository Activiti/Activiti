/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.test.email;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MockEmailTransport extends Transport {

  private static Logger logger = LoggerFactory.getLogger(MockEmailTransport.class);

  public MockEmailTransport(Session smtpSession, URLName urlName) {
    super(smtpSession, urlName);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses) throws MessagingException {
    try {
      logger.info(message.getContent().toString());
    } catch (IOException ex) {
      logger.error("Error occured while sending email" + ex);
    }
  }

  @Override
  public void connect() throws MessagingException {
  }

  @Override
  public void connect(String host, int port, String username, String password) throws MessagingException {
  }

  @Override
  public void connect(String host, String username, String password) throws MessagingException {
  }

  @Override
  public void close() {
  }
}
