<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "nodeId" : "${artifact.nodeId!''}",
  "connectorId" : "${artifact.connectorId!''}",
  "vFolderId" : "${vFolderId!''}",
  "label" : "${artifact.metadata.name!''}"
}
</#escape>

</#if>