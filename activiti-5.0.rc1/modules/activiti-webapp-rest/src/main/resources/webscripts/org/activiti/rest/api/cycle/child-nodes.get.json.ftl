[<#list folders as folder><@printFolder folder/><#if folder_has_next>,</#if></#list>
<#if (folders?size > 0) && (files?size > 0)>,</#if>
<#list files as file><@printFile file/><#if file_has_next>,</#if></#list>]

<#macro printFolder folder>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${folder.metadata.name}",
  "connectorId": "${folder.connectorId}",
  "artifactId": "${folder.nodeId}",
  "folder": "true"
}
</#escape>
</#macro>

<#macro printFile file>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${file.metadata.name}",
  "connectorId": "${file.connectorId}",
  "artifactId": "${file.nodeId}",
  "expanded": "true",
  "file": "true",
  "contentType": "${file.artifactType.mimeType.contentType}"
}
</#escape>
</#macro>