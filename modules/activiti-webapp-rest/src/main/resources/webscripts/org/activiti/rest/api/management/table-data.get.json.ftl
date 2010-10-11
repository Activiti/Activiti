<#import "../activiti.lib.ftl" as restLib>
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#list tablePage.rows as row>
    {
      <#list row?keys as columnName>
      "${columnName?js_string}":<#if row[columnName]?is_sequence>${row[columnName]?size?c}<#elseif row[columnName]?is_boolean>${row[columnName]?string}<#elseif row[columnName]?is_number>${row[columnName]?c}<#elseif row[columnName]?is_date>"${row[columnName]?string}"<#else>"${row[columnName]?js_string}"</#if><#if columnName_has_next>,</#if><#if firstColumnName??><#assign firstColumnName=columnName/></#if>
      </#list>
    }<#if row_has_next>,</#if>
    </#list>
  ],
  <@restLib.printPagination/>
}
</#escape>