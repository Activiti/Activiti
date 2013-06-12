package org.activiti.engine.impl.email;

import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;
import org.apache.commons.mail.Email;

/**
 * @author Hariprasath Manivannan
 */
public class JndiEmailConfiguration extends AbstractEmailConfiguration {

  /**
   * A String that correspond to the JNDI lookup name for the Mail Session
   */
  protected String mailSessionJndi;

  public String getMailSessionJndi() {
    return mailSessionJndi;
  }

  public void setMailSessionJndi(String mailSessionJndi) {
    this.mailSessionJndi = mailSessionJndi;
  }

  @Override
  public void apply(Email email) {
    try {
      email.setMailSessionFromJNDI(mailSessionJndi);
    } catch (NamingException e) {
      throw new ActivitiException("Could not send email: JNDI configured issue " + e.getLocalizedMessage());
    }
  }

}
