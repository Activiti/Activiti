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
package org.activiti.explorer.ui.login;

import java.io.UnsupportedEncodingException;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.springframework.web.util.HtmlUtils;

import com.vaadin.ui.LoginForm;


/**
 * @author Joram Barrez
 */
public class ExplorerLoginForm extends LoginForm {
  
  private static final long serialVersionUID = 1L;
  
  protected String usernameCaption;
  protected String passwordCaption;
  protected String submitCaption;
  
  protected static final String STYLE_LOGIN_FIELD = "login-field";
  protected static final String STYLE_LOGIN_FIELD_CAPTION = "login-field-caption";
  protected static final String STYLE_LOGIN_BUTTON = "login-button";
  
  public ExplorerLoginForm() {
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    usernameCaption = HtmlUtils.htmlEscape(i18nManager.getMessage(Messages.LOGIN_USERNAME));
    passwordCaption = HtmlUtils.htmlEscape(i18nManager.getMessage(Messages.LOGIN_PASSWORD));
    submitCaption = HtmlUtils.htmlEscape(i18nManager.getMessage(Messages.LOGIN_BUTTON));
  }
  
  // Hack-alert !! See explanation at http://vaadin.com/book/-/page/components.loginform.html
  protected byte[] getLoginHTML() {
    // Application URI needed for submitting form
    String appUri = getWindow().getName() + "/";

    String x, h, b; // XML header, HTML head and body
    
    x = "<!DOCTYPE html PUBLIC \"-//W3C//DTD "
      + "XHTML 1.0 Transitional//EN\" "
      + "\"http://www.w3.org/TR/xhtml1/"
      + "DTD/xhtml1-transitional.dtd\">\n";
    
    h = "<head><script type='text/javascript'>"
      + "var setTarget = function() {"
      + "  var uri = '" + appUri + "loginHandler';"
      + "  var f = document.getElementById('loginf');"
      + "  document.forms[0].action = uri;"
      + "  document.forms[0].username.focus();"
      + "};"
      + ""
      + "var styles = window.parent.document.styleSheets;"
      + "for(var j = 0; j < styles.length; j++) {\n"
      + "  if(styles[j].href) {"
      + "    var stylesheet = document.createElement('link');\n"
      + "    stylesheet.setAttribute('rel', 'stylesheet');\n"
      + "    stylesheet.setAttribute('type', 'text/css');\n"
      + "    stylesheet.setAttribute('href', styles[j].href);\n"
      + "    document.getElementsByTagName('head')[0]"
      + "                .appendChild(stylesheet);\n"
      + "  }"
      + "}\n"
      + "function submitOnEnter(e) {"
      + "  var keycode = e.keyCode || e.which;"
      + "  if (keycode == 13) {document.forms[0].submit();}"
      + "}\n"
      + "</script>"
      + "</head>";
    
    b = "<body onload='setTarget();'"
      + "  class='" + ExplorerLayout.STYLE_LOGIN_PAGE + "'>" 
      + "<div>"
      + "<iframe name='logintarget' style='width:0;height:0;"
      + "border:0;margin:0;padding:0;'></iframe>"
      + "<form id='loginf' target='logintarget'"
      + "      onkeypress='submitOnEnter(event)'"
      + "      method='post'>"
      + "<table width='100%'>"
      + "<tr><td class='" + STYLE_LOGIN_FIELD_CAPTION + "'>" + usernameCaption + "</td>"
      + "<td width='80%'><input class='" + STYLE_LOGIN_FIELD +"' type='text' name='username'></td></tr>"
      + "<tr><td class='" + STYLE_LOGIN_FIELD_CAPTION + "'>" + passwordCaption + "</td>"
      + "    <td><input class='" + STYLE_LOGIN_FIELD + "'"
      + "          type='password'"
      + "          name='password'></td></tr>"
      + "</table>"
      + "<div>"
      + "<div onclick='document.forms[0].submit();'"
      + "     tabindex='0' class='" + STYLE_LOGIN_BUTTON + "' role='button'>"
      + "<span>" + submitCaption + "</span>"
      + "</span></div></div></form></div></body>";
    
    String encoding = "UTF-8";
    try {
		return (x + "<html>" + h + b + "</html>").getBytes(encoding);
	} catch (UnsupportedEncodingException e) {
		// This should never happen
		throw new java.lang.RuntimeException("Unsupported encoding: " + encoding);
	}
  }

}
