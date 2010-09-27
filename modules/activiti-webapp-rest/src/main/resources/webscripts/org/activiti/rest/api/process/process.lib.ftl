<#macro printProcessDefinitionList processDefinitions>
[
  <#list processDefinitions as processDefinition><@printProcessDefinition processDefinition/><#if processDefinition_has_next>,</#if></#list>
]
</#macro>

<#macro printProcessDefinition processDefinition>
{
  "id": "${processDefinition.id?js_string}",
  "key": "${processDefinition.key?js_string}",
  "version": ${processDefinition.version?string},
  "name": <#if processDefinition.name??>"${processDefinition.name?js_string}"<#else>null</#if>,
  "resourceName": "${processDefinition.resourceName?js_string}",
  "deploymentId": "${processDefinition.deploymentId?js_string}",
  "startFormResourceKey": <#if processDefinition.startFormResourceKey??>"${processDefinition.startFormResourceKey?js_string}"<#else>null</#if>
}
</#macro>