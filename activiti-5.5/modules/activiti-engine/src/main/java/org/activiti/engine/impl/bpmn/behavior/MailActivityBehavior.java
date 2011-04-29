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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class MailActivityBehavior extends AbstractBpmnActivityBehavior {

  private Expression to;
  private Expression from;
  private Expression cc;
  private Expression bcc;
  private Expression subject;
  private Expression text;
  private Expression html;

  public void execute(ActivityExecution execution) {
    String toStr = getStringFromField(to, execution);
    String fromStr = getStringFromField(from, execution);
    String ccStr = getStringFromField(cc, execution);
    String bccStr = getStringFromField(bcc, execution);
    String subjectStr = getStringFromField(subject, execution);
    String textStr = getStringFromField(text, execution);
    String htmlStr = getStringFromField(html, execution);

    Email email = createEmail(textStr, htmlStr);

    addTo(email, toStr);
    setFrom(email, fromStr);
    addCc(email, ccStr);
    addBcc(email, bccStr);
    setSubject(email, subjectStr);
    setMailServerProperties(email);

    try {
      email.send();
    } catch (EmailException e) {
      throw new ActivitiException("Could not send e-mail", e);
    }
    leave(execution);
  }

  protected Email createEmail(String text, String html) {
    if (html != null) {
      return createHtmlEmail(text, html);
    } else if (text != null) {
      return createTextOnlyEmail(text);
    } else {
      throw new ActivitiException("'html' or 'text' is required to be defined when using the mail activity");
    }
  }

  protected HtmlEmail createHtmlEmail(String text, String html) {
    HtmlEmail email = new HtmlEmail();
    try {
      email.setHtmlMsg(html);
      if (text != null) { // for email clients that don't support html
        email.setTextMsg(text);
      }
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create HTML email", e);
    }
  }

  protected SimpleEmail createTextOnlyEmail(String text) {
    SimpleEmail email = new SimpleEmail();
    try {
      email.setMsg(text);
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create text-only email", e);
    }
  }

  protected void addTo(Email email, String to) {
    String[] tos = splitAndTrim(to);
    if (tos != null) {
      for (String t : tos) {
        try {
          email.addTo(t);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + t + " as recipient", e);
        }
      }
    } else {
      throw new ActivitiException("No recipient could be found for sending email");
    }
  }

  protected void setFrom(Email email, String from) {
    String fromAddres = null;

    if (from != null) {
      fromAddres = from;
    } else { // use default configured from address in process engine config
      fromAddres = Context.getProcessEngineConfiguration().getMailServerDefaultFrom();
    }

    try {
      email.setFrom(fromAddres);
    } catch (EmailException e) {
      throw new ActivitiException("Could not set " + from + " as from address in email", e);
    }
  }

  protected void addCc(Email email, String cc) {
    String[] ccs = splitAndTrim(cc);
    if (ccs != null) {
      for (String c : ccs) {
        try {
          email.addCc(c);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + c + " as cc recipient", e);
        }
      }
    }
  }

  protected void addBcc(Email email, String bcc) {
    String[] bccs = splitAndTrim(bcc);
    if (bccs != null) {
      for (String b : bccs) {
        try {
          email.addBcc(b);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + b + " as bcc recipient", e);
        }
      }
    }
  }

  protected void setSubject(Email email, String subject) {
    email.setSubject(subject != null ? subject : "");
  }

  protected void setMailServerProperties(Email email) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    String host = processEngineConfiguration.getMailServerHost();
    if (host == null) {
      throw new ActivitiException("Could not send email: no SMTP host is configured");
    }
    email.setHostName(host);

    int port = processEngineConfiguration.getMailServerPort();
    email.setSmtpPort(port);

    String user = processEngineConfiguration.getMailServerUsername();
    String password = processEngineConfiguration.getMailServerPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }
  }

  protected String[] splitAndTrim(String str) {
    if (str != null) {
      String[] splittedStrings = str.split(",");
      for (int i = 0; i < splittedStrings.length; i++) {
        splittedStrings[i] = splittedStrings[i].trim();
      }
      return splittedStrings;
    }
    return null;
  }

  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if(expression != null) {
      Object value = expression.getValue(execution);
      if(value != null) {
        return value.toString();
      }
    }
    return null;
  }

}
