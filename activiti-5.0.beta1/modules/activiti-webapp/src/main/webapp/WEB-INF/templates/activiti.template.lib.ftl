<#function globalConfig key default>
   <#if config.global.flags??>
      <#assign values = config.global.flags.childrenMap[key]>
      <#if values?? && values?is_sequence>
         <#return values[0].value>
      </#if>
   </#if>
   <#return default>
</#function>

<#--
   JavaScript minimisation via YUI Compressor.
-->
<#macro script type src>
  <script type="${type}" src="${DEBUG?string(src, src?replace(".js", "-min.js"))}"></script>
</#macro>

<#--
   Stylesheets gathered and rendered using @import to workaround IEBug KB262161
-->
<#assign templateStylesheets = []>

<#macro link rel type href>
  <#assign templateStylesheets = templateStylesheets + [href]>
</#macro>

<#macro renderStylesheets>
  <style type="text/css" media="screen">
    <#list templateStylesheets as href>
    @import "${href}";
    </#list>
  </style>
</#macro>

<#--
   Template "templateHeader" macro.
   Includes preloaded YUI assets and essential site-wide libraries.
-->
<#macro templateHeader doctype="strict">
  <#if doctype = "strict">
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
  <#else>
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  </#if>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

    <#assign DEBUG = (globalConfig("client-debug", "false") = "true")>
    <#assign resource = url.context + "/res">


    <title>${msg("application.name")} :: ${page.title}</title>
    <meta http-equiv="X-UA-Compatible" content="Edge" />

    <!-- Shortcut Icons -->
    <link rel="shortcut icon" href="${resource}/res/favicon.ico" type="image/vnd.microsoft.icon" />
    <link rel="icon" href="${resource}/favicon.ico" type="image/vnd.microsoft.icon" />

    <!-- YUI, Bubbling & Log4JS -->
    <@link rel="stylesheet" type="text/css" href="${resource}/yui/yui-2.8.1-activiti.css" />
    <#if DEBUG>
    <script type="text/javascript" src="${resource}/js/log4javascript.v1.4.1.js"></script>
    <script type="text/javascript" src="${resource}/yui/yui-2.8.1-activiti-debug.js"></script>
    <script type="text/javascript">//<![CDATA[
    YAHOO.util.Event.throwErrors = true;
    //]]></script>
    <#else>
    <script type="text/javascript" src="${resource}/yui/yui-2.8.1-activiti-min.js"></script>
    </#if>
    <@script type="text/javascript" src="${resource}/yui/bubbling.v2.1.js"></@script>
    <script type="text/javascript" src="${url.context}/service/messages.js?locale=${locale}"></script>
    <script type="text/javascript" src="${resource}/js/activiti-core.js"></script>
    <script type="text/javascript" src="${resource}/js/activiti-form.js"></script>
    <script type="text/javascript" src="${resource}/js/activiti-widget.js"></script>
    <script type="text/javascript" src="${resource}/js/activiti-service.js"></script>
    <script type="text/javascript">//<![CDATA[
      Activiti.service.Ajax.displayFailureMessage = function(msgKey, response) {
        var msg = Activiti.i18n.getMessage(msgKey);
        msg = (msg != msgKey) ? msg : (response.json ? response.json.message : response.serverResponse.responseText);
        Activiti.widget.PopupManager.displayError(Activiti.i18n.getMessage("label.failure"), msg);
      };
      Activiti.service.Ajax.displaySuccessMessage = function(msgKey, response) {
        var msg = Activiti.i18n.getMessage(msgKey);
        msg = (msg != msgKey) ? msg : (response.json ? response.json.message : null);
        if (msg) {
          Activiti.widget.PopupManager.displayMessage({
            text: msg
          });
        }
      };
    //]]></script>
    <script type="text/javascript" src="${resource}/js/activiti-app.js"></script>

    <!-- Activiti constants and user info -->
    <script type="text/javascript">//<![CDATA[
      Activiti.constants = Activiti.constants || {};
      Activiti.constants.DEBUG = ${DEBUG?string};
      Activiti.constants.URL_CONTEXT = "${url.context}/";
      Activiti.service.REST_ENPOINT = "activiti-rest-endpoint";
      Activiti.service.REST_PROXY_URI = window.location.protocol + "//" + window.location.host + "${url.context}/proxy/" + Activiti.service.REST_ENPOINT +"/";
      Activiti.service.REST_PROXY_URI_RELATIVE = "${url.context}/proxy/" + Activiti.service.REST_ENPOINT + "/";
      Activiti.constants.USERNAME = "${user.name!""}";
      Activiti.constants.GROUPS = { securityRole: {}, assignment: {}};
    <#if user.properties.securityRoleGroups??>
      <#list user.properties.securityRoleGroups?keys as groupId>
        Activiti.constants.GROUPS.securityRole["${groupId}"] = "${user.properties.securityRoleGroups[groupId]}";
      </#list>
    </#if>
    <#if user.properties.assignmentGroups??>
      <#list user.properties.assignmentGroups?keys as groupId>
        Activiti.constants.GROUPS.assignment["${groupId}"] = "${user.properties.assignmentGroups[groupId]}";
      </#list>
    </#if>
    //]]></script>

    <!-- Template Assets -->
    <#nested>
    <@link rel="stylesheet" type="text/css" href="${resource}/css/activiti-core.css" />
    <@link rel="stylesheet" type="text/css" href="${resource}/css/activiti-app.css" />
    <@renderStylesheets />

    <!-- Component Assets -->
    ${head}

  </head>
</#macro>

<#--
   Template "templateBody" macro.
   Pulls in main template body.
-->
<#macro templateBody>
  <body id="Activiti" class="yui-skin-sam">
    <div id="ActiviInitializer" style="display: none;"></div>
    <div id="sticky-wrapper">
      <div id="sticky-body">
        <#nested>
      </div>
      <div id="sticky-push"></div>
    </div>
</#macro>


<#--
   Template "templateFooter" macro.
   Pulls in template footer.
-->
<#macro templateFooter>
    <div id="sticky-footer">
      <#nested>
    </div>
  </body>
</html>
</#macro>
