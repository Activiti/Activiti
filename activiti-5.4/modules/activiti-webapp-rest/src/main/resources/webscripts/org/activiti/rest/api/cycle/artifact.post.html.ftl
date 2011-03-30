<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

{
  "nodeId" : "${artifact.nodeId!''}",
  "connectorId" : "${artifact.connectorId!''}",
  "vFolderId" : "${vFolderId!''}",
  "label" : "${artifact.metadata.name!''}",
  "openLinks" : [
  <#list links as link>{
    "${link.actionId}" : "${link.url!''}"
  }<#if link_has_next>,</#if></#list>
  ]
}

</#if>