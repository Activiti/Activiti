<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "configs": {<#list configs?keys as key>
      "${configs[key]}": "${key}"<#if key_has_next>,</#if></#list>
  }
}
</#escape>
</#if>
