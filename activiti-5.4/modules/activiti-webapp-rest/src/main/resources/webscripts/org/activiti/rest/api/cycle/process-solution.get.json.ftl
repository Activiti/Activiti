<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${processSolution.label}",
  "id": "${processSolution.id}", 
  "state": "${processSolution.state}",
  "folders": [<#list folders as folder>
  				<@printFolder folder />
  				<#if folder_has_next>,</#if>
  			  </#list>]
}
</#escape>

</#if>


<#macro printFolder folder>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "id": "${folder.id!''}",
  "label": "${folder.label!''}", 
  "type": "${folder.type!''}",
  "processSolutionId": "${folder.processSolutionId!''}",
  "referencedNodeId": "${folder.referencedNodeId!''}",
  "referencedConnectorId": "${folder.connectorId!''}",
  "nodeId": "${folder.processSolutionId!''}/${folder.id!''}",
  "connectorId": "ps-${folder.processSolutionId!''}"
}
</#escape>
</#macro>