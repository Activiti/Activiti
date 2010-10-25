<#escape x as jsonUtils.encodeJSONString(x)>
[<#list links as link><@printLink link/><#if link_has_next>,</#if></#list>]
</#escape>

<#macro printLink link>
{
  "artifact": {
  	<@printArtifact link.targetArtifact/>,
  	"targetElementId": "${link.targetElementId}",
  	"targetElementName": "${link.targetElementName}",
  	"linkType": "${link.linkType}",
  	"comment": "${link.comment}"
  }
}
</#macro>

<#macro printArtifact artifact>
	"targetConnectorId": "${artifact.connectorId}",
  	"targetArtifactId": "${artifact.nodeId}",
	"targetArtifactRevision": "${artifact.artifactType.revision}",
	"targetContentType": "${artifact.artifactType.mimeType.contentType}",
	"label": "${artifact.metadata.name}"
</#macro>
