<#escape x as jsonUtils.encodeJSONString(x)>
<#macro printAuthenticationException authenticationException>
{
	"authenticationError": "${authenticationException.message?string}<#if authenticationException.cause??>: ${authenticationException.cause.message!''}</#if>",
	"repoInError": "${authenticationException.connectorId?string}"
}
</#macro>
</#escape>