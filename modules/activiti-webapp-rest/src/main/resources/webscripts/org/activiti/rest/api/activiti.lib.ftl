<#macro printPagination>
  "total": ${(total!0)?c},
  "start": ${(start!0)?c},
  <#if sort??>"sort": "${sort}",</#if>
  <#if order??>"order": "${order}",</#if>
  "size": ${(size!0)?c}
</#macro>