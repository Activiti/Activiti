<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list tags as tag>
	"${tag}"<#if tag_has_next>,</#if>
	</#list>
]
</#escape>