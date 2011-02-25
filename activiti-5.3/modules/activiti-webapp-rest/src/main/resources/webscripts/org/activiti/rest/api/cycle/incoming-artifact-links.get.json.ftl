<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>
<#import "../activiti.lib.ftl" as activitiLib>
<#import "link.lib.ftl" as linkLib>

{
  "data": <@linkLib.printLinkList links/>,
  <@activitiLib.printPagination/>
}

</#if>