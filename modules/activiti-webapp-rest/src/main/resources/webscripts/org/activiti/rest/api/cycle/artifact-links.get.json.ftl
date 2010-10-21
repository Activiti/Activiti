<#escape x as jsonUtils.encodeJSONString(x)>
[<#list links as link><@printLink link/><#if link_has_next>,</#if></#list>]
</#escape>

<#macro printLink link>
{
  <@printArtifact link.targetArtifact/>,
  "targetElementId": "${link.targetElementId}",
  "targetElementName": "${link.targetElementName}"
}
</#macro>

<#macro printArtifact artifact>
	"targetConnectorId": "${artifact.connectorId}",
  	"targetArtifactId": "${artifact.originalNodeId}",
	"targetArtifactRevision": "${artifact.artifactType.revision}",
	"targetContentType": "${artifact.artifactType.mimeType.contentType}",
	"label": "${artifact.metadata.name}"
</#macro>
