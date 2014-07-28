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

import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Tim Stephenson
 */
public class MailActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  private static final Logger LOG = LoggerFactory.getLogger(MailActivityBehavior.class);
  
  protected Expression to;
  protected Expression from;
  protected Expression cc;
  protected Expression bcc;
  protected Expression subject;
  protected Expression text;
	protected Expression textVar;
  protected Expression html;
  protected Expression htmlVar;
  protected Expression charset;
  protected Expression ignoreException;
  protected Expression exceptionVariableName;

  public void execute(ActivityExecution execution) {
    
    boolean doIgnoreException = Boolean.parseBoolean(getStringFromField(ignoreException, execution));
    String exceptionVariable = getStringFromField(exceptionVariableName, execution);
    Email email = null;
    try {
      String toStr = getStringFromField(to, execution);
      String fromStr = getStringFromField(from, execution);
      String ccStr = getStringFromField(cc, execution);
      String bccStr = getStringFromField(bcc, execution);
      String subjectStr = getStringFromField(subject, execution);
  		String textStr = textVar == null ? getStringFromField(text, execution)
  				: getStringFromField(getExpression(execution, textVar), execution);
  		String htmlStr = htmlVar == null ? getStringFromField(html, execution)
  				: getStringFromField(getExpression(execution, htmlVar), execution);
      String charSetStr = getStringFromField(charset, execution);
  
      email = createEmail(textStr, htmlStr);
  
      addTo(email, toStr);
      setFrom(email, fromStr);
      addCc(email, ccStr);
      addBcc(email, bccStr);
      setSubject(email, subjectStr);
      setMailServerProperties(email);
      setCharset(email, charSetStr);
  
      email.send();
      
    } catch (ActivitiException e) {
      handleException(execution, e.getMessage(), e, doIgnoreException, exceptionVariable);
    } catch (EmailException e) {
      handleException(execution, "Could not send e-mail in execution " + execution.getId(), e, doIgnoreException, exceptionVariable);
    }
    
    leave(execution);
  }

  protected Email createEmail(String text, String html) {
    if (html != null) {
      return createHtmlEmail(text, html);
    } else if (text != null) {
      return createTextOnlyEmail(text);
    } else {
      throw new ActivitiIllegalArgumentException("'html' or 'text' is required to be defined when using the mail activity");
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

    String mailSessionJndi = processEngineConfiguration.getMailSesionJndi();
    if (mailSessionJndi != null) {
      try {
        email.setMailSessionFromJNDI(mailSessionJndi);
      } catch (NamingException e) {
        throw new ActivitiException("Could not send email: Incorrect JNDI configuration", e);
      }
    } else {
      String host = processEngineConfiguration.getMailServerHost();
      if (host == null) {
        throw new ActivitiException("Could not send email: no SMTP host is configured");
      }
      email.setHostName(host);

      int port = processEngineConfiguration.getMailServerPort();
      email.setSmtpPort(port);

      email.setSSL(processEngineConfiguration.getMailServerUseSSL());
      email.setTLS(processEngineConfiguration.getMailServerUseTLS());

      String user = processEngineConfiguration.getMailServerUsername();
      String password = processEngineConfiguration.getMailServerPassword();
      if (user != null && password != null) {
        email.setAuthentication(user, password);
      }
    }
  }
  
  protected void setCharset(Email email, String charSetStr) {
    if (charset != null) {
      email.setCharset(charSetStr);
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
    if (expression != null) {
      Object value = expression.getValue(execution);
      if(value != null) {
        return value.toString();
      }
    }
    return null;
  }

  protected Expression getExpression(ActivityExecution execution, Expression var) {
    String variable = (String) execution.getVariable(var.getExpressionText());
    return Context.getProcessEngineConfiguration().getExpressionManager()
        .createExpression(variable);
  }

  protected void handleException(ActivityExecution execution, String msg, Exception e, boolean doIgnoreException, String exceptionVariable) {
    if (doIgnoreException) {
      LOG.info("Ignoring email send error: " + msg, e);
      if (exceptionVariable != null && exceptionVariable.length() > 0) {
        execution.setVariable(exceptionVariable, msg);
      }
    } else {
      if (e instanceof ActivitiException) {
        throw (ActivitiException) e;
      } else {
        throw new ActivitiException(msg, e);
      }
    }
  }
}
