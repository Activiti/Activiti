<#escape x as jsonUtils.encodeJSONString(x)>
{
  "success": <#if result??>${result?string}<#else>false</#if>
}
</#escape>