<#escape x as jsonUtils.encodeJSONString(x)>
{
  "name": <#if processEngineInfo.name??>"${processEngineInfo.name?js_string}"<#else>null</#if>,
  "resourceUrl": <#if processEngineInfo.resourceUrl??>"${processEngineInfo.resourceUrl?js_string}"<#else>null</#if>,
  "exception": <#if processEngineInfo.exception??>"${processEngineInfo.exception?js_string}"<#else>null</#if>,
  "version": "${version?js_string}"
}
</#escape>