<#escape x as jsonUtils.encodeJSONString(x)>
<#macro printLinkList links>
[
  <#list links as link><@printLink link/><#if link_has_next>,</#if></#list>
]
</#macro>

<#macro printLink link>{
  "id": "${link.id}",
  "sourceArtifact": <@printArtifact link.sourceArtifact/>,
  "sourceElementId": "${link.sourceElementId!''}",
  "sourceElementName": "${link.sourceElementName!''}",
  "targetArtifact": <@printArtifact link.targetArtifact/>,
  "targetElementId": "${link.targetElementId!''}",
  "targetElementName": "${link.targetElementName!''}",
  "comment": "${link.comment!''}",
  "linkType": "${link.linkType!''}"	
}</#macro>

<#macro printArtifact artifact>{
  "connectorId": "${artifact.connectorId!''}",
  "artifactId": "${artifact.nodeId!''}",
  "artifactRevision": "${artifact.metadata.revision!''}",
  "contentType": "${artifact.artifactType.name!''}",
  "label": "${artifact.metadata.name!''}"
}</#macro>
</#escape>  