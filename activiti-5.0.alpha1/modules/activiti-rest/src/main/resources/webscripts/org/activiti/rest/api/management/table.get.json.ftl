<#escape x as jsonUtils.encodeJSONString(x)>
{
  "metadata": {
    "tableName": "${table.tableName}",
    "columnNames": [<#list table.columnNames as columnName>"${columnName}"<#if columnName_has_next>,</#if></#list>],
    "columnNames": [<#list table.columnTypes as columnType>"${columnType}"<#if columnType_has_next>,</#if></#list>]
  },
  "data": [
    <#list table.rows as row>
    {
      <#list row?keys as columnName>
      "${columnName}": "${row[columnName]}"<#if columnName_has_next>,</#if><#if firstColumnName??><#assign firstColumnName=columnName/></#if>
      </#list>
    }<#if row_has_next>,</#if>
    </#list>
  ],
  "totalRecords": ${table.noOfResults!0},
  "startIndex": ${table.offset!0},
  "sort": "${firstColumnName!""}",
  "dir": "asc",
  "pageSize": ${table.noOfResults!0}
}
</#escape>