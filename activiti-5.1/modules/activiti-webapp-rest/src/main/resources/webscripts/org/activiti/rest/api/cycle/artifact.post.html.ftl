<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<p>success: <#if result??>${result?string}<#else>false</#if></p>

</#if>