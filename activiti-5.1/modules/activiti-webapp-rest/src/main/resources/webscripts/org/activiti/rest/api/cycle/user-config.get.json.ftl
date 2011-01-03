<#if authenticationException??>
	<#import "cycle.lib.ftl" as cycleLib/>
	<@cycleLib.printAuthenticationException authenticationException/>
<#else>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "userConfig": [<#list userConfig?keys as configClassName>
    {
      "configClassName": "${configClassName}",
      "configs": [<#list userConfig[configClassName] as configs>
      	{
      		<#list configs?keys as property>"${property}": "${configs[property]!''}"<#if property_has_next>,
      		</#if></#list>
      	}<#if configs_has_next>,</#if></#list>
      ]
    }<#if configClassName_has_next>,</#if></#list>
  ]
}
</#escape>
</#if>