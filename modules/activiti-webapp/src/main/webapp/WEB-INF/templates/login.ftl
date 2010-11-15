<#include "activiti.template.lib.ftl" />
<#assign successUrl=(url.args["url"]!url.url)?html />
<#assign failureUrl="/login?error=true" />
<@templateHeader/>
<@templateBody>
  <div id="header">
    <div class="activiti-component">
      <div class="application-info">
        <img src="${url.context}/res/images/logo.png"/>
      </div>
    </div>
  </div>
  <div id="content">
    <div id="login">
      <form accept-charset="UTF-8" method="post" action="${url.context}/dologin">
        <#if !successUrl?contains(failureUrl)>
          <input name="success" type="hidden" value="${successUrl}" />
        </#if>
        <input name="failure" type="hidden" value="${url.context}${failureUrl}&amp;url=${successUrl?url}" />
        <#if url.args["error"]??>
        <div class="section">
          <div id="login-error" class="status-error"></div>
        </div>
        <#else>
        <div class="section">
          <div id="login-browser-warning" class="status-error"></div>
        </div>
        </#if>
        <div class="section">
          <label for="username">User Name:</label>
          <input id="username" name="username" type="text" />
          <br/>
        </div>
        <div class="section">
          <label for="password">Password:</label>
          <input id="password" name="password" type="password" />
          <input id="submit" type="submit" value="Login"/>
          <br/>
        </div>
      </form>
    </div>
    <script type="text/javascript">
      YAHOO.util.Dom.get("username").focus();
      YAHOO.util.Selector.query("label[for=username]", document, true).innerHTML = Activiti.i18n.getMessage("label.username") + ":";
      YAHOO.util.Selector.query("label[for=password]", document, true).innerHTML = Activiti.i18n.getMessage("label.password") + ":";
      YAHOO.util.Dom.get("submit").value = Activiti.i18n.getMessage("button.login");
      var loginErrorEl = YAHOO.util.Dom.get("login-error");
      if (loginErrorEl) {
        loginErrorEl.innerHTML = Activiti.i18n.getMessage("message.login.error");
      }
      else if (YAHOO.env.ua.ie > 0 && YAHOO.env.ua.ie < 7) {
        var loginBrowserWarningEl = YAHOO.util.Dom.get("login-browser-warning");
        if (loginBrowserWarningEl) {
          loginBrowserWarningEl.innerHTML = Activiti.i18n.getMessage("message.login.browser-warning");
        }
      }
    </script>
  </div>
</@>
<@templateFooter/>
