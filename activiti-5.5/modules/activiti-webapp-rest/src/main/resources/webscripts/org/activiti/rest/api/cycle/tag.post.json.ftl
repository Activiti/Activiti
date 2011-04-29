<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "tag": {
  	"connectorId": "${connectorId}",
  	"repositoryNodeId": "${repositoryNodeId}",
  	"tagName": "${tagName}",
  	"alias": "${alias}"
  }
}
</#escape>

</#if>