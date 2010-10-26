<#macro printProcessDefinitionList processDefinitions>
[
  <#list processDefinitions as processDefinition><@printProcessDefinition processDefinition/><#if processDefinition_has_next>,</#if></#list>
]
</#macro>

<#macro printProcessDefinition processDefinition>
{
  "id": "${processDefinition.id}",
  "key": "${processDefinition.key}",
  "name": <#if processDefinition.name??>"${processDefinition.name}"<#else>null</#if>,
  "version": ${processDefinition.version},
  "deploymentId": "${processDefinition.deploymentId}",
  "resourceName": "${processDefinition.resourceName}",
  "startFormResourceKey": <#if processDefinition.startFormResourceKey??>"${processDefinition.startFormResourceKey}"<#else>null</#if>
}
</#macro>