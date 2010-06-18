<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list groups as group>
    {
      "id": "${group.id!""}",
      "name": "${group.name!""}",
      "type": "${group.type!""}"
    }<#if group_has_next>,</#if>
    </#list>
  ]
}
</#escape>