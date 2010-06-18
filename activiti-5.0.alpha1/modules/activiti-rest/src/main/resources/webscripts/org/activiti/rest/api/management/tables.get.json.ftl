<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list tables?keys as tableName>
    {
      "tableName": "${tableName}",
      "noOfResults": ${tables[tableName]}
    }<#if tableName_has_next>,</#if>
    </#list>
  ]
}
</#escape>
