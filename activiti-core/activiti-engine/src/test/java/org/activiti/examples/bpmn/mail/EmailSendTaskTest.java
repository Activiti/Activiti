/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.examples.bpmn.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 */
public class EmailSendTaskTest extends PluggableActivitiTestCase {

    /* Wiser is a fake email server for unit testing */
    private Wiser wiser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        boolean serverUpAndRunning = false;
        while (!serverUpAndRunning) {
            wiser = new Wiser();
            wiser.setPort(5025);

            try {
                wiser.start();
                serverUpAndRunning = true;
            } catch (RuntimeException e) { // Fix for slow port-closing Jenkins
                if (e.getMessage().toLowerCase().contains("BindException")) {
                    Thread.sleep(250L);
                }
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        wiser.stop();
        super.tearDown();
    }

    @Deployment
    public void testSendEmail() throws Exception {

        String from = "ordershipping@activiti.org";
        boolean male = true;
        String recipientName = "John Doe";
        String recipient = "johndoe@alfresco.com";
        Date now = new Date();
        String orderId = "123456";

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("sender", from);
        vars.put("recipient", recipient);
        vars.put("recipientName", recipientName);
        vars.put("male", male);
        vars.put("now", now);
        vars.put("orderId", orderId);

        runtimeService.startProcessInstanceByKey("sendMailExample", vars);

        List<WiserMessage> messages = wiser.getMessages();
        assertThat(messages).hasSize(1);

        WiserMessage message = messages.get(0);
        MimeMessage mimeMessage = message.getMimeMessage();

        assertThat(mimeMessage.getHeader("Subject", null)).isEqualTo("Your order " + orderId + " has been shipped");
        assertThat(mimeMessage.getHeader("From", null)).isEqualTo(from);
        assertThat(mimeMessage.getHeader("To", null)).contains(recipient);
    }

}
