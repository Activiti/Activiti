<#import "management.lib.ftl" as managementLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  <@managementLib.printJob job/>
  <#if stacktrace??>, "stacktrace": "${stacktrace}"</#if>
}
</#escape>
