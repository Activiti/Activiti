<#escape x as jsonUtils.encodeJSONString(x)>

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
  "diagramResourceName": <#if processDefinition.diagramResourceName??>"${processDefinition.diagramResourceName}"<#else>null</#if>,
  "startFormResourceKey": <#if processDefinition.startFormResourceKey??>"${processDefinition.startFormResourceKey}"<#else>null</#if>
}
</#macro>


<#macro printProcessInstance processInstance>
{
  "id": "${processInstance.id}",
  "processDefinitionId": "${processInstance.processDefinitionId}",
  "activityNames": [<#list processInstance.findActiveActivityIds() as name>"${name}"<#if name_has_next>, </#if></#list>],
  "ended": ${processInstance.ended?string}
}
</#macro>

</#escape>