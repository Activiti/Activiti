<#escape x as jsonUtils.encodeJSONString(x)>
{
  "id": "${instance.id}",
  "processDefinitionId": "${instance.processDefinitionId?js_string}",
  "activityNames": [<#list instance.findActiveActivityIds() as name>"${name?js_string}"<#if name_has_next>, </#if></#list>],
  "ended": ${instance.ended?string}
}
</#escape>