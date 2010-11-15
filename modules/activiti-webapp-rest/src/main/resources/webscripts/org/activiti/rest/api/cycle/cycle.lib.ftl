<#escape x as jsonUtils.encodeJSONString(x)>
<#macro printAuthenticationException authenticationException>
{
	"authenticationError": "${authenticationException.message?string}",
	"reposInError": [<#list authenticationException.connectors?keys as key>
		{
			"${key}": "${authenticationException.connectors[key]}"
		}<#if key_has_next>,</#if></#list>]
}
</#macro>
</#escape>