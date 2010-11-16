<#escape x as jsonUtils.encodeJSONString(x)>
{
	"tags": [
		<#list tags?keys as key>
		{
			"id": "${key}",
			"alias": "${tags[key]}"
		}<#if key_has_next>,</#if>
		</#list>
	]
}
</#escape>