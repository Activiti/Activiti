<#escape x as jsonUtils.encodeJSONString(x)>
{
  "id": "${instance.id}",
  "processDefinitionId": "${instance.processDefinitionId}",
  "activityNames": [<#list instance.activityNames as name>"${name}"<#if name_has_next>, </#if></#list>],
  "ended": ${instance.ended?string}
}
</#escape>