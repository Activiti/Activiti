<#macro printProcessDefinitionList processDefinitions>
[
  <#list processDefinitions as processDefinition><@printProcessDefinition processDefinition/><#if processDefinition_has_next>,</#if></#list>
]
</#macro>

<#macro printProcessDefinition processDefinition>
{
  "id": "${processDefinition.id}",
  "key": "${processDefinition.key}",
  "version": ${processDefinition.version?string},
  "name": <#if processDefinition.name??>"${processDefinition.name}"<#else>null</#if>
}
</#macro>