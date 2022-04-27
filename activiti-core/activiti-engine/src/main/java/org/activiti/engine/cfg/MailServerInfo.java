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


package org.activiti.engine.cfg;


public class MailServerInfo {

  protected String mailServerDefaultFrom;
  protected String mailServerHost;
  protected int mailServerPort;
  protected String mailServerUsername;
  protected String mailServerPassword;
  protected boolean mailServerUseSSL;
  protected boolean mailServerUseTLS;

  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  public void setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
  }

  public String getMailServerHost() {
    return mailServerHost;
  }

  public void setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
  }

  public int getMailServerPort() {
    return mailServerPort;
  }

  public void setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
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

  public boolean isMailServerUseSSL() {
    return mailServerUseSSL;
  }

  public void setMailServerUseSSL(boolean mailServerUseSSL) {
    this.mailServerUseSSL = mailServerUseSSL;
  }

  public boolean isMailServerUseTLS() {
    return mailServerUseTLS;
  }

  public void setMailServerUseTLS(boolean mailServerUseTLS) {
    this.mailServerUseTLS = mailServerUseTLS;
  }
}
