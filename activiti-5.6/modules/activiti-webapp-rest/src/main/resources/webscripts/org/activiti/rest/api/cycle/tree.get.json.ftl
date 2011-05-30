<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>
	[<#list tree as node><@printNode node/><#if node_has_next>,</#if></#list>]
</#if>

<#macro printNode node>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "label": "${node.label}",
  "connectorId": "${node.connectorId}",
  "nodeId": "${node.nodeId}",
  "vFolderId": "${node.vFolderId!''}",
  "vFolderType": "${node.type!''}",<#if (node.expanded??) && (node.expanded = "true")>
  "expanded": "${node.expanded}",
  </#if>
  <#if node.folder??>"folder": "true"<#if (node.children??) && (node.children?size > 0)>,
  "children": [
    <#list node.children as child><@printNode child/><#if child_has_next>,
    </#if></#list>]</#if>
  <#elseif node.file??>"file": "true",
  "labelStyle": "${node.labelStyle}"
  </#if>
}
</#escape>
</#macro>
