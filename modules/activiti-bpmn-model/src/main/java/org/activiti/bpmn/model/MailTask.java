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
package org.activiti.bpmn.model;

/**
 * @author Tijs Rademakers
 */
public class MailTask extends Task {

  protected String to;
  protected String subject;
  protected String text;
  protected String html;
  protected String from;
  protected String cc;
  protected String bcc;
  protected String charset;

  public String getTo() {
    return to;
  }
  public void setTo(String to) {
    this.to = to;
  }
  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public String getHtml() {
    return html;
  }
  public void setHtml(String html) {
    this.html = html;
  }
  public String getFrom() {
    return from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public String getCc() {
    return cc;
  }
  public void setCc(String cc) {
    this.cc = cc;
  }
  public String getBcc() {
    return bcc;
  }
  public void setBcc(String bcc) {
    this.bcc = bcc;
  }
  public String getCharset() {
    return charset;
  }
  public void setCharset(String charset) {
    this.charset = charset;
  }
}
