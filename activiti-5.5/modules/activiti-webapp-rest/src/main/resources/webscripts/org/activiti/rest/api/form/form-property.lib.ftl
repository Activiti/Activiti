<#escape x as jsonUtils.encodeJSONString(x)>

<#macro printFormPropertyList propertyList>
[
  <#list propertyList as property>
  {
    <@printFormProperty property/>
  }<#if property_has_next>,</#if>
  </#list>
]
</#macro>
  
<#macro printFormProperty property>
  "id": "${property.id}",
  "name": <#if property.name??>"${property.name}"<#else>null</#if>,
  "value": <#if property.value??>"${property.value}"<#else>null</#if>,
  "type": <#if property.formType??>"${property.formType}"<#else>null</#if>,
  "required": <#if property.required??>"${property.required?string("true", "false")}"<#else>null</#if>,
  "readable": <#if property.readable??>"${property.readable?string("true", "false")}"<#else>null</#if>,
  "writable": <#if property.writable??>"${property.writable?string("true", "false")}"<#else>null</#if>
</#macro>

</#escape>