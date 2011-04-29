<#escape x as jsonUtils.encodeJSONString(x)>

<#macro printProcessEngineInfo processEngineInfo>
{
  "name": <#if processEngineInfo.name??>"${processEngineInfo.name}"<#else>null</#if>,
  "resourceUrl": <#if processEngineInfo.resourceUrl??>"${processEngineInfo.resourceUrl}"<#else>null</#if>,
  "exception": <#if processEngineInfo.exception??>"${processEngineInfo.exception}"<#else>null</#if>,
  "version": "${version}"
}
</#macro>

</#escape>