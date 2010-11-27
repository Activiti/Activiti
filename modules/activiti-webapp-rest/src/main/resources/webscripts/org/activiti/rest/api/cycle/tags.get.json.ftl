<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list tags as tag>
	"${tag}"<#if tag_has_next>,</#if>
	</#list>
]
</#escape>

</#if>