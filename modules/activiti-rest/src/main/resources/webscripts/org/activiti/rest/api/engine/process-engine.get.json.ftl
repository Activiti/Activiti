<#escape x as jsonUtils.encodeJSONString(x)>
{
  "name": <#if processEngineInfo.name??>"${processEngineInfo.name}"<#else>null</#if>,
  "resourceUrl": <#if processEngineInfo.resourceUrl??>"${processEngineInfo.resourceUrl}"<#else>null</#if>,
  "exception": <#if processEngineInfo.exception??>"${processEngineInfo.exception}"<#else>null</#if>,
  "version": "${version}"
}
</#escape>