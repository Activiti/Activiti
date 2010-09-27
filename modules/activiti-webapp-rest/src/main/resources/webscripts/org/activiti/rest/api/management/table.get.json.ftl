{
  "tableName": "${tableMetaData.tableName?js_string}",
  "columnNames": [<#list tableMetaData.columnNames as columnName>"${columnName?js_string}"<#if columnName_has_next>,</#if></#list>],
  "columnTypes": [<#list tableMetaData.columnTypes as columnType>"${columnType?js_string}"<#if columnType_has_next>,</#if></#list>]
}