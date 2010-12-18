<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

[<#list links as link><@printLink link/><#if link_has_next>,</#if></#list>]

<#macro printLink link>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "artifact": {
  	<@printArtifact link.sourceArtifact/>,
  	"sourceElementId": "${link.sourceElementId!''}",
  	"sourceElementName": "${link.sourceElementName!''}",
  	"linkType": "${link.linkType!''}",
  	"comment": "${link.comment!''}"
  }
}
</#escape>
</#macro>

<#macro printArtifact artifact>
<#escape x as jsonUtils.encodeJSONString(x)>
	"sourceConnectorId": "${artifact.connectorId!''}",
  	"sourceArtifactId": "${artifact.nodeId!''}",
	"sourceArtifactRevision": "${artifact.artifactType.revision!''}",
	"sourceContentType": "${artifact.artifactType.name!''}",
	"label": "${artifact.metadata.name!''}"
</#escape>
</#macro>

</#if>