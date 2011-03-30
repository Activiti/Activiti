<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>
	[<#list folders as folder><@printFolder folder/><#if folder_has_next>,</#if></#list>
	<#if (folders?size > 0) && (files?size > 0)>,</#if>
	<#list files as file><@printFile file/><#if file_has_next>,</#if></#list>]
</#if>

<#macro printFolder folder>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${folder.label!''}",
  "connectorId": "${folder.connectorId}",
  "nodeId": "${folder.nodeId}",
  "vFolderType": "${folder.type!''}",
  "vFolderId": "${folder.vFolderId!''}",
  "folder": "true"
}
</#escape>
</#macro>

<#macro printFile file>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${file.label!''}",
  "connectorId": "${file.connectorId}",
  "nodeId": "${file.nodeId}",
  "expanded": "true",
  "file": "true",
  "labelStyle": "${file.labelStyle}",
  "vFolderId": "${file.vFolderId!''}",
  "vFolderType": "${file.type!''}"
}
</#escape>
</#macro>