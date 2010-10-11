<#import "management.lib.ftl" as managementLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list tableNames as tableName>
    {
      "tableName": "${tableName?js_string}",
      "total": ${tables[tableName]?js_string}
    }<#if tableName_has_next>,</#if>
    </#list>
  ]
}
</#escape>
