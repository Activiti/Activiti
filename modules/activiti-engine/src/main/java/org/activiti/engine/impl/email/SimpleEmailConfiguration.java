package org.activiti.engine.impl.email;

import org.activiti.engine.ActivitiException;
import org.apache.commons.mail.Email;

/**
 * @author Hariprasath Manivannan
 */
public class SimpleEmailConfiguration extends AbstractEmailConfiguration {

  protected String mailServerHost = "localhost";
  protected String mailServerUsername; // by default no name and password are
  // provided, which
  protected String mailServerPassword; // means no authentication for mail
  // server
  protected int mailServerPort = 25;
  protected boolean useSSL = false;
  protected boolean useTLS = false;

  public String getMailServerHost() {
    return mailServerHost;
  }

  public void setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
  }

  public String getMailServerUsername() {
    return mailServerUsername;
  }

  public void setMailServerUsername(String mailServerUsername) {
    this.mailServerUsername = mailServerUsername;
  }

  public String getMailServerPassword() {
    return mailServerPassword;
  }

  public void setMailServerPassword(String mailServerPassword) {
    this.mailServerPassword = mailServerPassword;
  }

  public int getMailServerPort() {
    return mailServerPort;
  }

  public void setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
  }

  public boolean isUseSSL() {
    return useSSL;
  }

  public void setUseSSL(boolean useSSL) {
    this.useSSL = useSSL;
  }

  public boolean isUseTLS() {
    return useTLS;
  }

  public void setUseTLS(boolean useTLS) {
    this.useTLS = useTLS;
  }

  public boolean getMailServerUseSSL() {
    return useSSL;
  }

  public boolean getMailServerUseTLS() {
    return useTLS;
  }

  @Override
  public void apply(Email email) {
    String host = getMailServerHost();
    if (host == null) {
      throw new ActivitiException("Could not send email: no SMTP host is configured");
    }
    email.setHostName(host);

    int port = getMailServerPort();
    email.setSmtpPort(port);

    email.setSSL(getMailServerUseSSL());
    email.setTLS(getMailServerUseTLS());

    String user = getMailServerUsername();
    String password = getMailServerPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }

  }

}
