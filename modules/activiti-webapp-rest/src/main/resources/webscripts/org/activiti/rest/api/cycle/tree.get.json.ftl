<#escape x as jsonUtils.encodeJSONString(x)>
[<#list folders as folder><@printFolder folder/><#if folder_has_next>,</#if></#list>
<#if (folders?size > 0) && (files?size > 0)>,</#if>
<#list files as file><@printFile file/><#if file_has_next>,</#if></#list>]
</#escape>

<#macro printFolder folder>
{
  "label": "${folder.metadata.name}",
  "id": "${folder.id}",
  "folder": "true"
}
</#macro>

<#macro printFile file>
{
  "label": "${file.metadata.name}",
  "id": "${file.id}",
  "expanded": "true",
  "file": "true",
  "contentType": "${file.artifactType.contentType.getName()}"
}
</#macro>