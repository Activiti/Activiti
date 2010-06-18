<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list definitions as definition>
    {
      "id": "${definition.id}",
      "key": "${definition.key}",
      "version": ${definition.version?string},
      "name": <#if definition.name??>"${definition.name}"<#else>null</#if>
    }<#if definition_has_next>,</#if>
    </#list>
  ]
}
</#escape>