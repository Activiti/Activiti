<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

[<#list links as link><@printLink link/><#if link_has_next>,</#if></#list>]

<#macro printLink link>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "artifact": {
  	<@printArtifact link.targetArtifact/>,
  	"targetElementId": "${link.targetElementId!''}",
  	"targetElementName": "${link.targetElementName!''}",
  	"linkType": "${link.linkType!''}",
  	"comment": "${link.comment!''}"
  }
}
</#escape>
</#macro>

<#macro printArtifact artifact>
<#escape x as jsonUtils.encodeJSONString(x)>
	"targetConnectorId": "${artifact.connectorId!''}",
  	"targetArtifactId": "${artifact.nodeId!''}",
	"targetArtifactRevision": "${artifact.artifactType.revision!''}",
	"targetContentType": "${artifact.artifactType.name!''}",
	"label": "${artifact.metadata.name!''}"
</#escape>
</#macro>

</#if>