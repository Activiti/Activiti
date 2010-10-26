<#import "management.lib.ftl" as managementLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list tableNames as tableName>
    {
      "tableName": "${tableName}",
      "total": ${tables[tableName]}
    }<#if tableName_has_next>,</#if>
    </#list>
  ]
}
</#escape>
