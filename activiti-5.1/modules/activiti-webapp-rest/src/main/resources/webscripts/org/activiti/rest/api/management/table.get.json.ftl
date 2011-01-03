<#escape x as jsonUtils.encodeJSONString(x)>
{
  "tableName": "${tableMetaData.tableName}",
  "columnNames": [<#list tableMetaData.columnNames as columnName>"${columnName}"<#if columnName_has_next>,</#if></#list>],
  "columnTypes": [<#list tableMetaData.columnTypes as columnType>"${columnType}"<#if columnType_has_next>,</#if></#list>]
}
</#escape>