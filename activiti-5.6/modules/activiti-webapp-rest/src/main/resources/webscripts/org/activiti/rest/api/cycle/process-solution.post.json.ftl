<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${folder.label}",
  "connectorId": "${folder.connectorId}",
  "nodeId": "${folder.nodeId}",
  "folder": "true"
}
</#escape>

</#if>