<#import "management.lib.ftl" as managementLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  <@managementLib.printJob job/>,
  <#if stacktrace??>"stacktrace": "${stacktrace?js_string}"</#if>
}
</#escape>
